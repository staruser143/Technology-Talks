from langchain_google_genai import ChatGoogleGenerativeAI
import os

# Set your key (or use a .env file)
os.environ["GOOGLE_API_KEY"] = "YOUR_KEY_HERE"

# Initialize the 2026-standard model
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash")

# Test it
try:
    response = llm.invoke("Confirming connection. Reply with 'System Online'.")
    print(response.content)
except Exception as e:
    print(f"Connection failed: {e}")
