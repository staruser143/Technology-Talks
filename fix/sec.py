import os
import sys
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_community.document_loaders import WebBaseLoader
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

# 1. Setup API Key — load from environment variable, never hardcode
if not os.environ.get("GOOGLE_API_KEY"):
    sys.exit("Error: GOOGLE_API_KEY environment variable is not set.")

# 2. Load the Web Content
# WebBaseLoader scrapes the URL and converts it into a "Document" object
loader = WebBaseLoader("https://example.com/some-article")
docs = loader.load()

# 3. Define the LLM (Gemini 2.5 Flash)
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", temperature=0)

# 4. Create a Summarization Prompt
prompt = ChatPromptTemplate.from_template("""
    You are a professional research assistant. 
    Summarize the following web page content into 3 concise bullet points.
    
    Content: {content}
""")

# 5. Build the Chain
chain = prompt | llm | StrOutputParser()

# 6. Run it
# We pass the 'page_content' from the first document loaded
result = chain.invoke({"content": docs[0].page_content})

print("--- WEB SUMMARY ---")
print(result)
