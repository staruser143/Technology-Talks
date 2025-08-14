Great question! Once you've started an **`ngrok` tunnel** in Google Colab (or any Python environment), it’s important to know how to **properly close or terminate it**, especially if you're restarting your app or want to stop the public URL for security or resource reasons.

---

### ✅ How to Close an `ngrok` Tunnel in Google Colab

There are **two main things** to clean up:
1. The **ngrok tunnel**
2. The **background Streamlit process**

Here’s how to do both.

---

### 🔚 Method 1: Close `ngrok` Tunnel (Recommended)

If you used `pyngrok`, you can **list and disconnect** active tunnels:

```python
from pyngrok import ngrok

# Get active tunnels
tunnels = ngrok.get_tunnels()
for tunnel in tunnels:
    print(f"Closing tunnel: {tunnel.public_url}")
    ngrok.disconnect(tunnel.public_url)

# Optional: Kill ngrok process
ngrok.kill()
```

> ✅ This stops all `ngrok` tunnels and frees the process.

---

### 🧹 Method 2: Kill All `ngrok` and Streamlit Processes

Sometimes processes keep running in the background even after a cell finishes.

Run this to **clean up everything**:

```python
import os
import signal
import subprocess

# Kill ngrok
try:
    subprocess.run(["pkill", "-f", "ngrok"])
    print("✅ ngrok process killed")
except Exception as e:
    print(f"Error killing ngrok: {e}")

# Kill Streamlit
try:
    subprocess.run(["pkill", "-f", "streamlit"])
    print("✅ Streamlit process killed")
except Exception as e:
    print(f"Error killing Streamlit: {e}")
```

This ensures:
- No leftover `ngrok` tunnels
- No hanging `streamlit` servers
- Fresh start next time

---

### 🔄 Why You Should Terminate Before Restarting

If you **don’t close the old tunnel**:
- You might get a **"port already in use"** error
- Old tunnels stay active (security risk)
- Multiple tunnels consume resources

✅ Always clean up before starting a new one!

---

### ✅ Best Practice: Use a Cleanup Cell

Add this as a **"Cleanup" cell** at the top of your notebook:

```python
# 🔧 Cleanup: Stop ngrok and Streamlit
from pyngrok import ngrok
import subprocess

# Stop ngrok tunnels
ngrok.kill()

# Stop Streamlit
subprocess.run(["pkill", "-f", "streamlit"], shell=False, stderr=subprocess.DEVNULL)
print("🧹 Cleaned up previous sessions")
```

Run this **before** starting a new app.

---

### 📝 Example: Full Restart Flow

```python
# 1. Cleanup
from pyngrok import ngrok
import subprocess
ngrok.kill()
subprocess.run(["pkill", "-f", "streamlit"], shell=False)

# 2. Reinstall (if needed)
!pip install streamlit pyngrok -q

# 3. Save app
%%writefile app.py
import streamlit as st
st.title("New Session!")

# 4. Restart tunnel
from pyngrok import ngrok
import threading
import os

def run_app():
    os.system("streamlit run app.py --server.port=8501")

threading.Thread(target=run_app).start()
public_url = ngrok.connect(8501)
print(f"🌟 New app at: {public_url}")
```

---

### ✅ Summary

| Task | Command |
|------|--------|
| List tunnels | `ngrok.get_tunnels()` |
| Close one | `ngrok.disconnect(url)` |
| Close all | `ngrok.kill()` |
| Kill Streamlit | `pkill -f streamlit` |
| Full cleanup | Use a cleanup cell |

---

Now you can safely **start, stop, and restart** your Streamlit + ngrok setup in Colab without conflicts.

Let me know if you want a **one-click "Restart App" button** in Streamlit! 🚀