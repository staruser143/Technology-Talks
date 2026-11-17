I am reviewing this practice question for the AWS Certified Generative AI Developer Professional exam. 

<question>

A developer is implementing a real-time conversational web application that streams tokens from Amazon Bedrock (converse_stream) to browser clients with sub-second time-to-first-token (TTFT). Because the frontend requires bi-directional communication for mid-stream user interruptions, the team selects an Amazon API Gateway WebSocket API backed by AWS Lambda. Which implementation detail is required to deliver incremental tokens to the connected client?

</question>

Possible Answers:
A. Buffer all streamed tokens in an Amazon SQS FIFO queue and configure the browser client to long-poll the queue using temporary IAM credentials.
B. Return the raw EventStream object directly in the Lambda handle's HTTP 200 response body so the API gateway automatically chunks the frames over the Web socket.
C. Retrieve the client's connectionId from the WebSocket $default route event , iterate over Bedrock stream generator and push each chunk via the apigatewaymanagementapi post_to_connection operation.
D. Configure the WebSocket route integration to invoke bedrock-runtime:ConverseStream  directly via AWS Service Proxy without a Lambda function 

Help me master this topic:
1.Why the correct answer is correct 
2. Typical mistakes or distractors candidates choose, and why they are wrong
3. Which AWS concepts are being tested here
4. A simple mental model for this scenario
5. A similar practice question. do not give the answer yet.

Search online for official AWS documentation and list the relevant sources you used. Be concise in your response.
