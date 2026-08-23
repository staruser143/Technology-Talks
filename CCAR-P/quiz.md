A document-review service builds requests from a system prompt, tool schemas, conversation history, and uploaded files. Some assembled requests unexpectedly exceed the team's input budget before generation starts. What is the most reliable preventive control?

A
Estimate tokens from character count and assume every supported model tokenizes identically.
Your answer
B
Enable prompt caching because cached input no longer occupies context capacity.
C
Raise the output limit, which automatically creates additional room for the supplied input.
D
Count the complete request for its target model, then trim, route, or reject it before generation.
Correct answer




I'm studying for the Anthropic Claude Certified Architect – Professional (CCAR-P) certification. I got the following question on a mock exam.

<question>
 A document-review service builds requests from a system prompt, tool schemas, conversation history, and uploaded files. Some assembled requests unexpectedly exceed the team's input budget before generation starts. What is the most reliable preventive control?
</question>

Possible Answers:

A.Estimate tokens from character count and assume every supported model tokenizes identically.

B. Enable prompt caching because cached input no longer occupies context capacity.

C. Raise the output limit, which automatically creates additional room for the supplied input.

D. Count the complete request for its target model, then trim, route, or reject it before generation



Explain:
1. Why the correct answer is correct
2. Why the other answers are incorrect
3. Which Anthropic concept or service I misunderstand
4. A simple mental model to remember the difference
5. A similar example question. Do not give the answer yet.

Search online for official Anthropic documentation and list the relevant sources you used. Be concise in your response.



