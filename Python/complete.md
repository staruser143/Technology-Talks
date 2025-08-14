Absolutely! Here's a **fully updated `app.py`** that includes:

✅ Support for:
- 📝 Text input  
- 📁 PDF upload  
- 🌐 URL summarization  

✅ Uses the **`long-t5-tglobal-base-16384`** model for **high-quality, long-form summaries**  
✅ Implements **map-reduce summarization with chunking**  
✅ Shows **individual chunk summaries** and a **final comprehensive summary**  
✅ Includes **error handling**, **progress feedback**, and **clean UI**

---

### ✅ Prerequisites

Install required packages:

```bash
pip install streamlit transformers torch pypdf trafilatura nltk tiktoken
```

> 💡 For best performance, run this on a **GPU-enabled environment** (Google Colab, RunPod, etc.). On CPU, inference may be slow (~30–60 sec).

---

### ✅ Full `app.py`

```python
# app.py - Advanced AI Summarizer with Long-T5, PDF, URL, and Chunk Summaries

import streamlit as st
from transformers import pipeline, AutoTokenizer
from pypdf import PdfReader
from trafilatura import fetch_url, extract
import nltk
import tiktoken
import torch

# --- Download NLTK data ---
nltk.download('punkt', quiet=True)

# --- Initialize session state ---
if 'fetched_text' not in st.session_state:
    st.session_state.fetched_text = ""
if 'chunk_summaries' not in st.session_state:
    st.session_state.chunk_summaries = []

# --- Load Model and Tokenizer ---
@st.cache_resource
def load_summarizer():
    st.write("🚀 Loading Long-T5 model (pszemraj/long-t5-tglobal-base-16384-book-summary)...")
    try:
        model_name = "pszemraj/long-t5-tglobal-base-16384-book-summary"
        tokenizer = AutoTokenizer.from_pretrained(model_name)
        summarizer = pipeline(
            "summarization",
            model=model_name,
            tokenizer=tokenizer,
            device=0 if torch.cuda.is_available() else -1,  # Use GPU if available
            torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
        )
        st.success("✅ Model loaded!")
        return summarizer, tokenizer
    except Exception as e:
        st.error(f"❌ Failed to load model: {e}")
        return None, None

summarizer, tokenizer = load_summarizer()

if summarizer is None:
    st.stop()

# --- Text Splitting with Sentence Awareness ---
def split_text_into_chunks(text, max_tokens=1500, overlap_sentences=1):
    try:
        enc = tiktoken.get_encoding("cl100k_base")
    except:
        enc = tiktoken.encoding_for_model("gpt-3.5-turbo")

    sentences = nltk.sent_tokenize(text)
    chunks = []
    current_chunk = []
    current_length = 0

    for sent in sentences:
        sent_tokens = len(enc.encode(sent))
        if current_length + sent_tokens > max_tokens and current_chunk:
            chunks.append(" ".join(current_chunk))
            # Overlap: carry last 'overlap_sentences' to next chunk
            start = max(0, len(current_chunk) - overlap_sentences)
            current_chunk = current_chunk[start:]
            current_chunk.append(sent)
            # Recalculate length
            current_length = len(enc.encode(" ".join(current_chunk)))
        else:
            current_chunk.append(sent)
            current_length += sent_tokens

    if current_chunk:
        chunks.append(" ".join(current_chunk))

    return chunks

# --- Summarize a Single Chunk ---
def summarize_chunk(text, min_len=100, max_len=200):
    if len(text.strip()) < 50:
        return ""
    try:
        summary = summarizer(
            text,
            min_length=min_len,
            max_length=max_len,
            do_sample=False,
            truncation=True
        )
        return summary[0]['summary_text']
    except Exception as e:
        st.warning(f"Chunk summarization failed: {str(e)}")
        return ""

# --- Extract Text from PDF ---
def extract_text_from_pdf(pdf_file):
    try:
        reader = PdfReader(pdf_file)
        text = ""
        for page in reader.pages:
            content = page.extract_text()
            if content:
                text += content
        return text.strip()
    except Exception as e:
        return f"❌ Error reading PDF: {str(e)}"

# --- Extract Text from URL ---
def extract_text_from_url(url):
    if not url.startswith(("http://", "https://")):
        return "❌ Invalid URL. Include http:// or https://"
    try:
        downloaded = fetch_url(url)
        if downloaded is None:
            return "❌ Failed to download. Check URL or connection."
        text = extract(downloaded, include_comments=False, include_tables=True, no_fallback=False)
        if not text or len(text.strip()) < 100:
            return "❌ No readable article content found."
        return text.strip()
    except Exception as e:
        return f"❌ Error: {str(e)}"

# --- Map-Reduce Summarization with Chunk Feedback ---
def summarize_with_details(text, chunk_max_tokens=1500):
    chunks = split_text_into_chunks(text, max_tokens=chunk_max_tokens, overlap_sentences=1)
    st.write(f"📄 Split into **{len(chunks)} chunks** for detailed processing.")

    if len(chunks) == 1:
        with st.spinner("🧠 Generating single-pass summary..."):
            final = summarize_chunk(text, min_len=150, max_len=600)
        return [], final

    # --- Step 1: Summarize Each Chunk ---
    chunk_summaries = []
    st.subheader("🔍 Chunk Summaries")
    summary_placeholder = st.empty()

    for i, chunk in enumerate(chunks):
        with st.spinner(f"Summarizing chunk {i+1}/{len(chunks)}..."):
            summary = summarize_chunk(chunk, min_len=80, max_len=180)
            chunk_summaries.append(summary)
            # Display in real-time
            summary_placeholder.markdown(f"**Chunk {i+1}:** {summary}\n\n---\n")

    # --- Step 2: Final Summary ---
    combined = " ".join([s for s in chunk_summaries if s])
    st.write("🧠 Generating final comprehensive summary...")
    final_summary = summarize_chunk(combined, min_len=200, max_len=600)

    return chunk_summaries, final_summary

# --- Streamlit UI ---
st.set_page_config(page_title="📚 Advanced AI Summarizer", layout="wide")
st.title("📚 Advanced AI Summarizer")
st.markdown("Summarize text, PDFs, or URLs with **detailed chunk summaries** and a **final comprehensive summary**.")

tab1, tab2, tab3 = st.tabs(["📝 Paste Text", "📁 Upload PDF", "🌐 Summarize URL"])

input_text = ""

# --- Tab 1: Paste Text ---
with tab1:
    input_text = st.text_area("Enter your text", height=300, placeholder="Paste long articles, essays, or reports...")

# --- Tab 2: Upload PDF ---
with tab2:
    pdf_file = st.file_uploader("Upload a PDF", type="pdf")
    if pdf_file is not None:
        with st.spinner("📄 Extracting text from PDF..."):
            input_text = extract_text_from_pdf(pdf_file)
        if input_text.startswith("❌"):
            st.error(input_text)
        elif len(input_text) < 100:
            st.warning("⚠️ Extracted text is too short.")
        else:
            st.success(f"✅ Extracted {len(input_text.split()):,} words.")
            with st.expander("📄 View Extracted Text"):
                st.write(input_text)

# --- Tab 3: URL Input ---
with tab3:
    url = st.text_input("Enter Article URL", placeholder="https://example.com/article")
    if st.button("🔍 Fetch Article", key="fetch"):
        if not url:
            st.warning("Please enter a URL.")
        else:
            with st.spinner("📡 Fetching and cleaning article..."):
                input_text = extract_text_from_url(url)
            if input_text.startswith("❌"):
                st.error(input_text)
            else:
                st.session_state.fetched_text = input_text
                st.success(f"✅ Fetched {len(input_text.split()):,} words.")
                with st.expander("📄 View Extracted Text"):
                    st.write(input_text)

# --- Use session state for URL text ---
if 'fetched_text' in st.session_state and st.session_state.fetched_text and not input_text:
    input_text = st.session_state.fetched_text

# --- Generate Summary Button ---
if st.button("✨ Generate Comprehensive Summary"):
    if not input_text or len(input_text.strip()) < 100:
        st.warning("Please provide more text (at least 100 characters).")
    else:
        st.session_state.chunk_summaries = []  # Reset
        with st.spinner("🔄 Processing document..."):
            chunk_summaries, final_summary = summarize_with_details(input_text, chunk_max_tokens=1500)
            st.session_state.chunk_summaries = chunk_summaries

        # --- Display Final Summary ---
        st.markdown("---")
        st.subheader("🎯 Final Comprehensive Summary")
        st.write(final_summary)

        # --- Optional: Download ---
        st.download_button(
            label="📥 Download Final Summary",
            data=final_summary,
            file_name="final_summary.txt",
            mime="text/plain"
        )

# --- Display Previous Chunk Summaries ---
if 'chunk_summaries' in st.session_state and st.session_state.chunk_summaries:
    st.markdown("---")
    st.subheader("🗂️ Individual Chunk Summaries (for transparency)")
    for i, s in enumerate(st.session_state.chunk_summaries):
        if s:
            st.markdown(f"**Chunk {i+1}:** {s}")
```

---

### ✅ Features

| Feature | Benefit |
|-------|--------|
| 🔍 Shows chunk summaries | Transparency in how summary was built |
| 🧠 Final comprehensive summary | Covers entire document |
| 📁 PDF + 🌐 URL support | Full flexibility |
| 🧩 Overlap + sentence-aware | No broken context |
| 🚀 GPU support | Faster on CUDA-enabled devices |
| 🧰 Session state | Preserves fetched text |

---

### 🚀 Run It

```bash
streamlit run app.py
```

> ⚠️ First run will download the model (~2.5 GB). After that, it’s cached.

---

### 💡 Tips

- Run on **Google Colab** if you don’t have GPU:  
  [https://colab.research.google.com](https://colab.research.google.com)
- Use **Streamlit Community Cloud** to deploy — add `requirements.txt`
- For **faster CPU inference**, try `google-t5/t5-base` (but less quality)

---

Let me know if you want:
- A **lightweight version** for CPU
- Integration with **Llama 3** or **Mistral**
- Support for **YouTube videos** or **podcasts**

You now have a **professional-grade summarizer** that rivals commercial tools — well done! 🎉🚀