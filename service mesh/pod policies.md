Great question! When you want to **control which pods can talk to other pods** in Kubernetes — especially in a **service mesh** environment — you need to set up **authorization policies**.

There are **two main layers** where you can enforce this:

---

## ✅ 1. Kubernetes NetworkPolicy (L3/L4 — Network Layer)

> Controls **which pods can communicate over IP/port** — like a firewall.

### 🔐 Use Case:
- “Only frontend pods can talk to backend pods on port 8080.”
- “No pod in ‘dev’ namespace can talk to ‘prod’ database.”

### 🛠️ Example: Allow frontend → backend

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
meta
  name: allow-frontend-to-backend
  namespace: myapp
spec:
  podSelector:
    matchLabels:
      app: backend  # 👈 This policy applies TO backend pods
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend  # 👈 Only frontend pods can connect
    ports:
    - protocol: TCP
      port: 8080
```

→ Apply with: `kubectl apply -f networkpolicy.yaml`

> ✅ Works without service mesh.  
> ❌ Only controls IP/port — not HTTP paths, methods, or service identity.

---

## ✅ 2. Service Mesh Authorization Policies (L7 — Application Layer)

> Controls **which service identities can call which services, endpoints, or HTTP methods** — using mTLS identity.

This is where **service mesh (Istio, Linkerd, Consul)** shines.

---

## 🎯 Let’s Focus on Service Mesh Authorization

We’ll show examples for **Istio** (most common) and **Linkerd**.

---

# 🔐 ISTIO: AuthorizationPolicy (Recommended for Fine-Grained Control)

Istio uses `AuthorizationPolicy` CRD to control access based on:

- ✅ Source identity (SPIFFE ID — derived from Service Account)
- ✅ Destination service/port
- ✅ HTTP method, path, headers
- ✅ Request context (JWT, etc.)

---

## ✅ Example 1: Allow “frontend” to call “backend” (any path)

```yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-frontend-to-backend
  namespace: myapp
spec:
  selector:
    matchLabels:
      app: backend  # 👈 Applies to backend pods
  action: ALLOW
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/myapp/sa/frontend-sa"]  # 👈 frontend’s identity
    to:
    - operation:
        methods: ["GET", "POST"]
```

> 💡 Istio uses **SPIFFE identity format**:  
> `cluster.local/ns/<namespace>/sa/<serviceaccount>`

→ So make sure your frontend pods are running with SA `frontend-sa`.

---

## ✅ Example 2: Allow Only GET /api/profile

```yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
meta
  name: allow-frontend-get-profile
  namespace: myapp
spec:
  selector:
    matchLabels:
      app: backend
  action: ALLOW
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/myapp/sa/frontend-sa"]
    to:
    - operation:
        methods: ["GET"]
        paths: ["/api/profile"]
```

→ Now frontend can only call `GET /api/profile` — nothing else.

---

## ✅ Example 3: Deny All by Default + Explicit Allow

First, deny all traffic to backend:

```yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
meta
  name: deny-all
  namespace: myapp
spec:
  selector:
    matchLabels:
      app: backend
  action: DENY
  rules:
  - to:
    - operation:
        methods: ["*"]
```

Then layer on specific ALLOW policies (like above).

→ This implements **zero-trust / default-deny** model.

---

## ✅ Example 4: Allow Multiple Sources

```yaml
rules:
- from:
  - source:
      principals:
      - "cluster.local/ns/myapp/sa/frontend-sa"
      - "cluster.local/ns/myapp/sa/admin-sa"
  to:
  - operation:
      methods: ["POST"]
      paths: ["/api/admin/*"]
```

---

## 🚀 Apply & Test

```bash
kubectl apply -f authz-policy.yaml
```

→ Takes effect immediately — no pod restart needed.

Test:
```bash
# Exec into frontend pod and curl backend
kubectl exec -it <frontend-pod> -- curl http://backend/api/profile -v
# → Should work

# Exec into random pod (without permission) and try:
kubectl exec -it <other-pod> -- curl http://backend/api/profile -v
# → Should return 403 Forbidden
```

---

# 🔐 LINKERD: Authorization via Service Profiles + Server Policy (Newer Feature)

> ⚠️ Linkerd’s authorization is less mature than Istio’s — but improving.

Linkerd 2.12+ supports **Server Authorization Policies** — allows you to restrict which *clients* (by service account) can access a *server*.

---

## ✅ Example: Allow Only “frontend” to Access “backend”

First, define a `Server` for your backend port:

```yaml
apiVersion: policy.linkerd.io/v1beta1
kind: Server
metadata:
  name: backend-http
  namespace: myapp
spec:
  podSelector:
    matchLabels:
      app: backend
  port: 8080
  proxyProtocol: HTTP
```

Then define a `ServerAuthorization`:

```yaml
apiVersion: policy.linkerd.io/v1beta1
kind: ServerAuthorization
meta
  name: frontend-to-backend
  namespace: myapp
spec:
  server:
    name: backend-http
  client:
    meshTLS:
      serviceAccounts:
      - namespace: myapp
        name: frontend-sa  # 👈 Only this SA can access
```

→ Apply both YAMLs.

> ✅ Simple and secure.  
> ❌ No HTTP path/method filtering yet (as of Linkerd 2.14).

---

## 🔄 How to Set Service Account for Pods

Make sure your frontend pods run with the right SA:

```yaml
# frontend-deployment.yaml
spec:
  template:
    spec:
      serviceAccountName: frontend-sa  # ← Critical!
      containers:
      - name: frontend
        image: my-frontend:latest
```

If SA doesn’t exist:

```yaml
apiVersion: v1
kind: ServiceAccount
meta
  name: frontend-sa
  namespace: myapp
```

→ Istio/Linkerd will use this SA to derive the service identity.

---

# 🧩 Bonus: Combine with Kubernetes RBAC & NetworkPolicy

For **defense in depth**, layer:

1. **NetworkPolicy** — “Only pods in namespace X can reach port Y.” (L3/L4)
2. **Service Mesh AuthZ** — “Only service A can call /api/endpoint.” (L7)
3. **Kubernetes RBAC** — “Pod can’t read secrets or list pods.” (Control plane)

---

## ✅ Summary: How to Set Policies for Pod-to-Pod Access

| Layer | Tool | Controls | Example Use Case |
|-------|------|----------|------------------|
| Network | `NetworkPolicy` | IP/Port (L3/L4) | “Only frontend pods can reach backend on 8080” |
| Service Mesh | `AuthorizationPolicy` (Istio) | Identity + HTTP method/path (L7) | “Only frontend can POST /order” |
| Service Mesh | `ServerAuthorization` (Linkerd) | Identity → Port (L7, limited) | “Only frontend-sa can access backend:8080” |
| Platform | RBAC + ServiceAccount | Access to Kubernetes API | “Pod can’t list secrets” |

---

## ✅ Pro Tips

1. **Always start with default-deny** → then allow explicitly.
2. **Use SPIFFE identities** — they’re portable and secure.
3. **Test with `curl` from different pods** to verify policies.
4. **Monitor Envoy access logs** — look for 403s to debug denials.
5. **Use `istioctl authz check <pod>`** to debug Istio policies.

---

## 🚀 Final Answer

> To allow a pod to invoke one or more pods:
>
> 1. ✅ Use **Kubernetes NetworkPolicy** for basic IP/port control.
> 2. ✅ Use **service mesh AuthorizationPolicy** (Istio) or **ServerAuthorization** (Linkerd) for fine-grained, identity-based L7 control.
> 3. ✅ Assign **Service Accounts** to pods — they become the service identity.
> 4. ✅ Layer policies for defense in depth.

Example Istio policy:
```yaml
# Allow frontend-sa to call backend’s /api/*
- from:
    source:
      principals: ["cluster.local/ns/myapp/sa/frontend-sa"]
  to:
    operation:
      paths: ["/api/*"]
```

Apply → test → monitor → you’re secure! 🔐

Let me know if you want help writing a policy for your specific use case — just share your app structure and I’ll give you the exact YAML.