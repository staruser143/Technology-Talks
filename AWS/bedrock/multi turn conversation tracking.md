To maintain multi-turn conversation state across distributed, stateless API calls with low read latency and automated session expiration, you should decouple your application state from both the client and the compute instances.
A production architecture utilizes a dedicated state store with built-in Time-to-Live (TTL) and lightweight reference-based request routing.
1. Storage Architecture Options
Depending on your performance thresholds and infrastructure stack, three primary patterns deliver sub-10ms or sub-millisecond read latency with automated lifecycle management:
| Architecture Pattern | Read Latency | Session Expiration (TTL) | Best Suited For |
|---|---|---|---|
| Amazon Bedrock AgentCore Memory | Low (managed) | Built-in via eventExpiryDuration (up to 365 days) | Serverless GenAI agents needing automatic short-term context tracking and long-term semantic distillation. |
| In-Memory Cache (Redis / ElastiCache / Dragonfly) | Sub-millisecond | Native key-level EXPIRE / SETEX commands | High-throughput, real-time conversational streaming and token pre-computation. |
| Distributed NoSQL (Amazon DynamoDB) | Single-digit ms (Sub-ms with DAX) | Native DynamoDB TTL attribute (zero-cost background purge) | Serverless microservices, strict persistence guarantees, and payload offloading. |
2. Request Flow & Architectural Mechanics
[ Client (Web / Mobile) ]
           │
           │  Sends only: { sessionId, actorId, newMessage }
           ▼
[ API Gateway / Load Balancer ]
           │
           ▼
[ Stateless API Workers / Lambdas ]
           │
           ├──► 1. Fast State Read (Fetch history by sessionId) ──► [ Low-Latency State Store ]
           │                                                      (Redis / DynamoDB / AgentCore)
           │                                                                 │
           ├──► 2. Reverse-Chronological Token Pruning[span_8](start_span)[span_8](end_span)[span_9](start_span)[span_9](end_span)         │
           │                                                                 │
           ├──► 3. Invoke Model (Stateless Converse / Invoke API)[span_10](start_span)[span_10](end_span)  │
           │                                                                 │
           └──► 4. Append New Turns + Touch TTL ─────────────────────────────┘

A. Edge-to-Backend Layer: Reference by Key Only
Never require client applications to submit the full conversational history back and forth.
 * The client passes only the sessionId, actorId, and the latest newMessage.
 * This prevents client-side prompt manipulation, eliminates network transmission overhead over mobile/web connections, and keeps ingress payloads small.
B. Automated Session Expiration (TTL)
 * DynamoDB TTL Pattern: Store a timestamp attribute representing the epoch expiration time (ttl_epoch = current_time + session_timeout_seconds). DynamoDB automatically scans and purges expired items in the background within 48 hours without consuming read or write throughput.
 * Redis In-Memory TTL: When reading or updating a session turn, execute EXPIRE session:{sessionId} {ttl_seconds} or SETEX to reset the expiration window (sliding session expiration).
 * AgentCore Memory Pattern: Configure the managed eventExpiryDuration parameter, which automatically expires raw short-term interaction events while optionally allowing persistent user preferences or summaries to remain.
C. Reverse-Chronological Token Pruning
Even with low-latency reads, retrieving and injecting an unbounded conversation array into downstream foundation models leads to context window overflow (ModelContextWindowExceededException) and inflated input token costs.
 * Inspect the retrieved turns in reverse chronological order.
 * Calculate token counts using a fast local tokenizer (e.g., tiktoken).
 * Keep only the latest N turns that fit into your allocated input token budget before dispatching the payload to the model.
3. Concrete Implementation: DynamoDB with TTL & State Pruning
This pattern handles concurrent distributed API instances reading and updating the conversation state with automated session expiration:
import time
import boto3
import tiktoken
from botocore.exceptions import ClientError

dynamodb = boto3.resource("dynamodb", region_name="us-east-1")
table = dynamodb.Table("ConversationSessionState")

SESSION_TIMEOUT_SECONDS = 3600  # 1 hour inactivity timeout
MAX_HISTORY_TOKEN_BUDGET = 3000
ENCODING = tiktoken.get_encoding("cl100k_base")

def get_pruned_session_history(session_id: str) -> list:
    """Fetches session state with single-digit ms latency and prunes to token budget."""
    try:
        response = table.get_item(
            Key={"session_id": session_id},
            ConsistentRead=False  # Eventual consistency for lowest read latency
        )
    except ClientError as e:
        raise RuntimeError(f"Database read failure: {e}")

    item = response.get("Item")
    if not item:
        return []

    # Check expiration in case DynamoDB background purge hasn't swept it yet
    now = int(time.time())
    if item.get("ttl_epoch", 0) < now:
        return []

    raw_history = item.get("history", [])

    # Token-aware pruning in reverse chronological order
    pruned_messages = []
    cumulative_tokens = 0

    for message in reversed(raw_history):
        msg_text = message.get("content", "")
        token_count = len(ENCODING.encode(msg_text))
        
        if cumulative_tokens + token_count > MAX_HISTORY_TOKEN_BUDGET:
            break
            
        cumulative_tokens += token_count
        pruned_messages.insert(0, message)

    return pruned_messages

def append_session_turns(session_id: str, actor_id: str, new_user_msg: str, new_assistant_msg: str):
    """Appends new turns and slides the TTL expiration window forward."""
    now = int(time.time())
    new_ttl = now + SESSION_TIMEOUT_SECONDS

    new_turns = [
        {"role": "user", "content": new_user_msg, "actor": actor_id, "timestamp": now},
        {"role": "assistant", "content": new_assistant_msg, "actor": "model", "timestamp": now}
    ]

    table.update_item(
        Key={"session_id": session_id},
        UpdateExpression=(
            "SET history = list_append(if_not_exists(history, :empty_list), :turns), "
            "ttl_epoch = :ttl, "
            "last_active = :now"
        ),
        ExpressionAttributeValues={
            ":turns": new_turns,
            ":empty_list": [],
            ":ttl": new_ttl,
            ":now": now
        }
    )

4. Handling Concurrency and Scaling Pitfalls
 * State Machine Limits (The 256 KB Boundary): If using workflow orchestrators like AWS Step Functions across distributed workers, never pass full conversational arrays directly inside execution inputs. Offload conversation arrays to the state store and pass only the session_id pointer across task transitions.
 * Handling Concurrent Turns: If multiple background workers or user tabs can post simultaneously to the same session, enforce Optimistic Concurrency Control (OCC) using DynamoDB conditional checks (ConditionExpression="version = :expected_version") or Redis atomic LPUSH operations to prevent lost updates.
 * Eventual Consistency vs. Read Latency: In high-speed distributed setups, read from the cache/database with standard eventual consistency to optimize read speed, but enforce strong reads (ConsistentRead=True in DynamoDB) if an immediate turn depends synchronously on a just-written tool execution.
