Great question! 🎯

When you're dealing with **long documents** (like research papers, books, or long articles), they often exceed the **maximum input length** of transformer models (e.g., BART or T5 can only handle ~1024 tokens). To summarize such documents, you need a strategy called:

---

## 🔗 **Map-Reduce Summarization**
> Also known as: **"Summarize chunks, then summarize the summaries"**

### ✅ How It Works:
1. **Split** the long document into smaller chunks (within model limits).
2. **Summarize each chunk individually** (the "Map" step).
3. **Combine all mini-summaries into one text**.
4. **Summarize the combined summaries** into a final concise summary (the "Reduce" step).

This mimics how humans digest long content — section by section, then overall.

---

## ✅ Step-by-Step Implementation in Python

We’ll extend your existing summarizer app using:
- `transformers` for summarization
- `tiktoken` or simple splitting for chunking
- Streamlit-friendly logic

---

### ✅ Step 1: Install `tiktoken` (Optional but Recommended)

For accurate token counting (used by OpenAI and good for alignment):

```bash
pip install tiktoken
```

Or use word/sentence splitting if you prefer not to install it.

---

### ✅ Step 2: Add Chunking Function

```python
import tiktoken

def split_text_into_chunks(text, max_tokens=900, model_name="gpt-3.5-turbo"):
    """
    Splits text into chunks of max_tokens using tiktoken tokenizer.
    """
    # Initialize tokenizer
    try:
        encoding = tiktoken.encoding_for_model(model_name)
    except KeyError:
        encoding = tiktoken.get_encoding("cl100k_base")  # Default for most models

    tokens = encoding.encode(text)
    chunks = []
    start = 0

    while start < len(tokens):
        end = start + max_tokens
        chunk_tokens = tokens[start:end]
        chunk_text = encoding.decode(chunk_tokens)
        chunks.append(chunk_text)
        start = end

    return chunks
```

> 🔹 Why 900? Leave room for summary generation (model max is 1024 → keep under that).

---

### ✅ Step 3: Summarize Each Chunk (Map Step)

```python
from transformers import pipeline

# Load summarizer once at startup
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

def summarize_chunk(chunk_text, min_length=30, max_length=150):
    """
    Summarizes a single chunk of text.
    """
    if len(chunk_text.strip()) < 50:
        return ""  # Skip very short chunks

    try:
        summary = summarizer(
            chunk_text,
            max_length=max_length,
            min_length=min_length,
            do_sample=False,
            truncation=True
        )
        return summary[0]['summary_text']
    except Exception as e:
        print(f"Error summarizing chunk: {e}")
        return ""
```

---

### ✅ Step 4: Combine and Reduce (Final Summary)

```python
def summarize_long_document(text, chunk_max_tokens=900, chunk_min_length=30, final_max_length=200):
    """
    Full map-reduce summarization for long texts.
    """
    # Step 1: Split into chunks
    chunks = split_text_into_chunks(text, max_tokens=chunk_max_tokens)
    st.write(f"📄 Split into {len(chunks)} chunks.")

    if len(chunks) == 1:
        # Short enough to summarize directly
        return summarize_chunk(text, min_length=chunk_min_length, max_length=final_max_length)

    # Step 2: Summarize each chunk (Map)
    mini_summaries = []
    for i, chunk in enumerate(chunks):
        with st.spinner(f"Summarizing chunk {i+1}/{len(chunks)}..."):
            summary = summarize_chunk(chunk, min_length=chunk_min_length, max_length=150)
            if summary:
                mini_summaries.append(summary)

    if not mini_summaries:
        return "❌ Could not generate any summaries from chunks."

    # Step 3: Combine summaries
    combined_summaries = " ".join(mini_summaries)

    # Step 4: Final summary (Reduce)
    st.write(f"🧠 Generating final summary from {len(mini_summaries)} mini-summaries...")
    final_summary = summarize_chunk(
        combined_summaries,
        min_length=50,
        max_length=final_max_length
    )

    return final_summary
```

---

### ✅ Step 5: Update Your Streamlit UI

Replace your old `summarize_text()` call with this:

```python
if st.button("✨ Generate Summary"):
    if not input_text.strip():
        st.warning("Please enter or upload text.")
    else:
        with st.spinner("🔍 Processing long document..."):
            final_summary = summarize_long_document(
                input_text,
                chunk_max_tokens=900,
                chunk_min_length=30,
                final_max_length=200
            )
        st.subheader("✅ Final Summary")
        st.write(final_summary)
```

---

## 🧠 Example Flow

Input: 5,000-word article  
→ Split into 6 chunks (~900 tokens each)  
→ Summarize each → 6 mini-summaries (~150 tokens each)  
→ Combine → ~900-token summary text  
→ Final summary → ~150 words, capturing the whole doc

---

## ✅ Tips for Better Results

| Tip | Why |
|-----|-----|
| Use overlap between chunks | Prevents cutting sentences in half. Add 50–100 token overlap. |
| Prefer sentence boundaries | Split on `.`, `?`, `!` instead of raw tokens. |
| Filter out low-content chunks | Skip summaries of tables, references, or boilerplate. |
| Use better models | Try `google-t5/t5-base` or `pszemraj/long-t5-tglobal-base-16384` for longer inputs. |

---

### 🔁 Optional: Overlap-Aware Chunking (Improved)

```python
def split_text_on_sentences(text, max_tokens=900):
    import nltk
    nltk.download('punkt', quiet=True)
    from nltk.tokenize import sent_tokenize
    import tiktoken

    encoding = tiktoken.get_encoding("cl100k_base")
    sentences = sent_tokenize(text)
    chunks = []
    current_chunk = []
    current_tokens = 0

    for sent in sentences:
        sent_tokens = len(encoding.encode(sent))
        if current_tokens + sent_tokens > max_tokens and current_chunk:
            chunks.append(" ".join(current_chunk))
            # Overlap: carry last 1 sentence to next chunk
            current_chunk = [current_chunk[-1]] if len(current_chunk) > 1 else []
            current_tokens = len(encoding.encode(current_chunk[0])) if current_chunk else 0
        current_chunk.append(sent)
        current_tokens += sent_tokens

    if current_chunk:
        chunks.append(" ".join(current_chunk))

    return chunks
```

Then use this instead of `split_text_into_chunks`.

---

## 🚀 Advanced Option: Use a Model That Handles Long Text

Try models designed for long inputs:
```python
summarizer = pipeline(
    "summarization",
    model="pszemraj/long-t5-tglobal-base-16384-book-summary"
)
```
Supports up to **16,384 tokens** — great for long docs!

Install:
```bash
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
```

---

## ✅ Summary

You now have a **robust way to summarize long documents** using **map-reduce**:

1. ✂️ Split long text into chunks
2. 🗺️ Summarize each chunk
3. 🔁 Combine and summarize again
4. ✅ Deliver a concise, global summary

This pattern is used by tools like **ChatGPT**, **Claude**, and **AI research assistants**.

---

Let me know if you want:
- Support for **overlap + sentence-aware splitting**
- A **downloadable version** of the full app
- Integration with **LangChain** for advanced pipelines

You're building something powerful — keep going! 💪🚀