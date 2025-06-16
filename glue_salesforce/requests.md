```python
import base64, hashlib, os

code_verifier = base64.urlsafe_b64encode(os.urandom(40)).rstrip(b'=').decode('utf-8')
code_challenge = base64.urlsafe_b64encode(
    hashlib.sha256(code_verifier.encode('utf-8')).digest()
).rstrip(b'=').decode('utf-8')
```

code_verifier:D7P5TvbQBRa-UdEfzXFIYVDRI4hJM8ME8Juv2wIe_1u94YXIaMz8MLFzjemobxTsG51EGhhEbuzUVGGc2CsnUg
code_challenge: toZetD4K01g8l0yMbAGUAYcAQHBOOf7LBlkUTvqItbs


https://orgfarm-3365db1462-dev-ed.develop.my.salesforce.com
 
Here’s the username for your Developer Edition:
jaadukanna.sri554@agentforce.com


```
https://login.salesforce.com/services/oauth2/authorize
?response_type=code
&client_id=3MVG9dAEux2v1sLt871OYCE_QiiQAbR_Q79SDEiVzGsYorBIFWvJx7JT77ZuV6qQ4vGJltA2HOKNBRw.XeWgo
&redirect_uri=https://login.salesforce.com/services/oauth2/success
&code_challenge=toZetD4K01g8l0yMbAGUAYcAQHBOOf7LBlkUTvqItbs
&code_challenge_method=S256
&scope=api%20refresh_token
```
```
https://login.salesforce.com/services/oauth2/success?code=aPrxhi6GmsfyknmwI2iVb0AHBMuSHJs7xiCwouWNhnyvSuxoY9GJ82zYLZk7B9J5wWZthgTuHw%3D%3D
```

