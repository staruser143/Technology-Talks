import os
import warnings
warnings.filterwarnings("ignore", category=UserWarning)

from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_core.documents import Document

print("⏳ Scanning folder and indexing files with metadata citations...")

documents_chunks = []
text_splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
source_dir = "source_documents"

# Loop through files manually to have precise control over metadata calculation
for filename in os.listdir(source_dir):
    file_path = os.path.join(source_dir, filename)
    if not os.path.isfile(file_path):
        continue
        
    ext = os.path.splitext(filename)[1].lower()
    
    # 1. HANDLE PDF FILES (Auto-tracks pages)
    if ext == ".pdf":
        loader = PyPDFLoader(file_path)
        pdf_docs = loader.load()
        chunks = text_splitter.split_documents(pdf_docs)
        for chunk in chunks:
            # Standardize source path to just the filename
            chunk.metadata["source_file"] = filename
            chunk.metadata["location"] = f"Page {chunk.metadata.get('page', 0) + 1}"
            documents_chunks.append(chunk)
            
    # 2. HANDLE TXT FILES (Tracks exact lines)
    elif ext == ".txt":
        with open(file_path, "r", encoding="utf-8") as f:
            lines = f.readlines()
            
        # Group lines together into small logical paragraph chunks manually to track lines
        current_chunk_text = ""
        start_line = 1
        
        for idx, line in enumerate(lines):
            if current_chunk_text == "":
                start_line = idx + 1
            current_chunk_text += line
            
            # When the text chunk gets large enough, save it as a Document
            if len(current_chunk_text) >= 400 or idx == len(lines) - 1:
                doc = Document(
                    page_content=current_chunk_text,
                    metadata={
                        "source_file": filename,
                        "location": f"Lines {start_line}-{idx + 1}"
                    }
                )
                current_chunk_text = ""
                documents_chunks.append(doc)

# 3. Save everything to Vector DB
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
if os.path.exists("chroma_db"):
    import shutil
    shutil.rmtree("chroma_db")

db = Chroma.from_documents(documents_chunks, embeddings, persist_directory="chroma_db")
print(f"✅ Success! Generated {len(documents_chunks)} chunks with active citation tags.")
