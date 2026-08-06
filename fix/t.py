from langchain_google_genai import ChatGoogleGenerativeAI
import os
import sys

# Load API key from environment variable (set via .env file or shell export)
if not os.environ.get("GOOGLE_API_KEY"):
    sys.exit("Error: GOOGLE_API_KEY environment variable is not set.")

# Initialize the 2026-standard model
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash")

# Test it
try:
    response = llm.invoke("Confirming connection. Reply with 'System Online'.")
    print(response.content)
except Exception as e:
    print(f"Connection failed: {e}")
