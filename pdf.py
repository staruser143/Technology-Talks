import os
import warnings
warnings.filterwarnings("ignore", category=UserWarning)

from langchain_community.document_loaders import DirectoryLoader, TextLoader, PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma

print("⏳ Scanning folder and indexing multi-format files (PDF & TXT)...")

# 1. Map file extensions to their specific LangChain loaders
loaders = {
    ".txt": TextLoader,
    ".pdf": PyPDFLoader
}

# 2. Helper function to dynamically choose the loader based on file extension
def create_loader(file_path):
    ext = os.path.splitext(file_path)[1].lower()
    if ext in loaders:
        return loaders[ext](file_path)
    return None

# 3. Load ALL files in the 'source_documents' directory using our multi-loader function
loader = DirectoryLoader(
    "source_documents", 
    glob="**/*.*",  # Match all files
    loader_cls=create_loader
)

documents = loader.load()
print(f"📄 Found and loaded {len(documents)} document(s) from your directory.")

# 4. Split the text into searchable paragraph chunks
text_splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
chunks = text_splitter.split_documents(documents)

# 5. Initialize free local embeddings framework
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

# 6. Clear out old database directory to prevent mixing old data
if os.path.exists("chroma_db"):
    import shutil
    shutil.rmtree("chroma_db")

# 7. Rebuild the persistent local vector database folder
db = Chroma.from_documents(chunks, embeddings, persist_directory="chroma_db")

print("✅ Success! Your multi-format database is ready inside 'chroma_db/'.")
