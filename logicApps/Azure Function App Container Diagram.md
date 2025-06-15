Here’s a **diagram** illustrating an **Azure Function App** as a container hosting **multiple Azure Functions**, each with its own trigger and logic:

![Azure Function App Hosting Multiple Functions](blob:https://m365.cloud.microsoft/5b0633ca-f1cf-4262-85c3-c1e9a3ab7650)

### 🧩 Diagram Highlights:
- **Function App** acts as a shared environment.
- Each **Function** has:
  - A unique **trigger** (e.g., HTTP, Timer, Queue).
  - Its own **business logic**.
- All functions share:
  - **Runtime**
  - **App settings**
  - **Scaling behavior**

