```python
import base64, hashlib, os

code_verifier = base64.urlsafe_b64encode(os.urandom(40)).rstrip(b'=').decode('utf-8')
code_challenge = base64.urlsafe_b64encode(
    hashlib.sha256(code_verifier.encode('utf-8')).digest()
).rstrip(b'=').decode('utf-8')
```

Code Challenge: 6nqBesf1vbhigohc-OIlpHnYo90FLM4AnRMgU6jINeY
Code Verifier: 65r8o4ecrgkdrxyssbru1p9ah0q40v7rfxa6nwugbibnn2o44b3juzlg48c0mdi8xaj97uwd18i40xxkjxpij0t7vxw60cetwi5t38cht79k2emjsaxceldalyzjv76k

3MVG9dAEux2v1sLt871OYCE_QiiQAbR_Q79SDEiVzGsYorBIFWvJx7JT77ZuV6qQ4vGJltA2HOKNBRw.XeWgo
DFC774BB067E01B2FB26E3DDEB55BB064CBEB798B9668AFBB43158CA231BB759



https://orgfarm-3365db1462-dev-ed.develop.my.salesforce.com
 
Here’s the username for your Developer Edition:
jaadukanna.sri554@agentforce.com


```
https://login.salesforce.com/services/oauth2/authorize
?response_type=code
&client_id=3MVG9dAEux2v1sLt871OYCE_QiiQAbR_Q79SDEiVzGsYorBIFWvJx7JT77ZuV6qQ4vGJltA2HOKNBRw.XeWgo
&redirect_uri=https://login.salesforce.com/services/oauth2/success
&code_challenge=6nqBesf1vbhigohc-OIlpHnYo90FLM4AnRMgU6jINeY
&code_challenge_method=S256
&scope=api%20refresh_token
```

aPrxhi6GmsfyknmwI2iVb0AHBN8jQXh7W7p894DYn65UMgGg24vpKlurexhrQRDBO81r6.iWhg%3D%3D
```
https://login.salesforce.com/services/oauth2/success?code=aPrxhi6GmsfyknmwI2iVb0AHBMuSHJs7xiCwouWNhnyvSuxoY9GJ82zYLZk7B9J5wWZthgTuHw%3D%3D
```

