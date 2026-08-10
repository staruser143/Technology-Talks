How do we configure the cache control with increased TTL


Good — this is confirmed directly from Anthropic's docs, and it's simpler than the earlier third-party sources suggested (no beta header needed anymore).

**The syntax**

You just add a `ttl` field inside the `cache_control` object on the block you want cached:

```python
system=[
  {
    "type": "text",
    "text": "<your 6,000-token support policy>",
    "cache_control": {"type": "ephemeral", "ttl": "1h"}
  }
]
```

That's it — `ttl` accepts only two values: `"5m"` (the default, used automatically if you omit `ttl` entirely) or `"1h"`. No separate beta header is required anymore; it's just part of the standard `cache_control` object.

**How this fixes the batch-job scenario**

For the overnight batch job with ~8-minute gaps between calls, changing the marker to:

```python
{"type": "text", "text": "<policy>", "cache_control": {"type": "ephemeral", "ttl": "1h"}}
```

means the cache now survives up to an hour of inactivity instead of 5 minutes — comfortably covering the 8-minute gap between tickets, so ticket 2 onward would hit the cache instead of rewriting it every time.

**The cost trade-off to know for the exam**: the 1-hour TTL costs more on the *write* side than the 5-minute default — cache writes are already priced higher than normal input tokens (roughly 1.25x base price for the 5-minute tier), and the 1-hour tier costs more again on top of that (roughly 2x base price). So it's not "free insurance" — it's a deliberate trade: pay a higher write cost per cache refresh, in exchange for a much longer window in which subsequent calls can still hit that cache. This is exactly the kind of trade-off the exam likes testing: the 1-hour TTL is the right choice specifically when your request cadence has real gaps (like this batch job), not as a default you'd reach for on a busy, frequently-hit conversational endpoint where the 5-minute default is already being refreshed constantly and cheaper to maintain.

**One more mechanical detail worth knowing**: if you're mixing TTLs across multiple breakpoints in one request (e.g., a very stable system prompt at 1h and a less-stable-but-still-cacheable tool list at 5m), the 1-hour segments need to come before the 5-minute segments in the request — order matters for how the breakpoints stack.

Want to try a scenario that specifically tests choosing between the two TTL options based on request cadence, or move to a different Domain 2 sub-topic now?