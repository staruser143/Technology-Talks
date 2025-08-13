The error you're seeing:

```
Error during summarization: index out of range in self
```

is a **common and frustrating issue** when using Hugging Face's `transformers` summarization pipeline. It usually means:

> ❌ The input text is either **too long**, **too short**, or **contains content that causes tokenization issues** — and the model fails to generate any output, so `summary[0]` doesn't exist.

Let’s **diagnose and fix** this step by step.

---

### 🔍 Root Causes

1. **Input is too long** → exceeds model’s max input length (e.g., BART supports 1024 tokens).
2. **Input is too short** (e.g., 1–2 words) → no valid summary can be generated.
3. **Empty or whitespace-only input** → after PDF extraction, sometimes only spaces/newlines remain.
4. **Model returns empty list** → `summary = []`, so `summary[0]` raises `IndexError`.

---

### ✅ Fix 1: Add Input Validation & Truncation

Update your `summarize_text()` function to:
- Check for empty/short input
- Truncate long texts to model limit
- Handle the case where no summary is returned

#### ✅ Updated `summarize_text()` Function

```python
def summarize_text(text, max_summary_pct=0.4, min_length=30, do_sample=False):
    """
    Summarizes text safely with validation and truncation.
    """
    # --- Step 1: Clean and validate input ---
    text = text.strip()
    if not text:
        return "❌ Input text is empty. Please provide valid content."

    # --- Step 2: Tokenize and truncate if too long ---
    # Use the actual tokenizer for accurate length
    from transformers import AutoTokenizer
    tokenizer = AutoTokenizer.from_pretrained("facebook/bart-large-cnn")

    tokens = tokenizer.encode(text, truncation=False)
    max_input_tokens = 1024  # BART's max

    if len(tokens) > max_input_tokens:
        st.warning(f"Text too long ({len(tokens)} tokens). Truncating to {max_input_tokens} tokens.")
        tokens = tokens[:max_input_tokens]
        text = tokenizer.decode(tokens, skip_special_tokens=True)

    if len(tokens) < 10:
        return "❌ Input too short to summarize. Please provide more text."

    # --- Step 3: Dynamic max_length ---
    dynamic_max = min(int(len(tokens) * max_summary_pct), 512)
    dynamic_max = max(min_length, min(512, dynamic_max))  # Cap at 512
    min_length = min(5, dynamic_max)  # min_length must be < max_length

    # --- Step 4: Summarize ---
    try:
        from transformers import pipeline
        summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

        summary = summarizer(
            text,
            max_length=dynamic_max,
            min_length=min_length,
            do_sample=do_sample,
            truncation=True  # Important!
        )

        # --- Step 5: Safety check ---
        if not summary or len(summary) == 0:
            return "❌ No summary was generated. Check input text."

        return summary[0]['summary_text']

    except Exception as e:
        # Catch common errors
        if "index out of range" in str(e):
            return "❌ Failed to generate summary: Input may be invalid or too long/short."
        elif "token indices" in str(e):
            return "❌ Tokenization error: Input contains unsupported characters."
        else:
            return f"❌ Error: {str(e)}"
```

---

### ✅ Fix 2: Ensure PDF Text Isn’t Junk

Sometimes `pypdf` extracts:
- Only whitespace
- Garbage characters
- Empty strings

Add validation **after PDF extraction**:

```python
with tab2:
    pdf_file = st.file_uploader("Upload a PDF", type="pdf")
    if pdf_file is not None:
        with st.spinner("Extracting text from PDF..."):
            input_text = extract_text_from_pdf(pdf_file)

        # --- Validate extracted text ---
        if not input_text or len(input_text.strip()) == 0:
            st.error("❌ Could not extract readable text from PDF. Is it scanned or password-protected?")
        elif len(input_text.split()) < 10:
            st.warning("❌ Extracted text is too short. Try a text-based PDF (not scanned).")
        else:
            st.success(f"✅ Extracted {len(input_text.split())} words.")
            with st.expander("📄 View Extracted Text"):
                st.write(input_text)
```

---

### ✅ Fix 3: Use `truncation=True` Always

Make sure you pass `truncation=True` to the pipeline:

```python
summary = summarizer(text, max_length=..., min_length=..., truncation=True)
```

Otherwise, long inputs can cause crashes.

---

### ✅ Fix 4: Alternative: Use a More Robust Model

Try using `google-t5/t5-small` — it’s smaller and more forgiving:

```python
summarizer = pipeline("summarization", model="google-t5/t5-small")
```

But note: `t5-small` needs a prefix like `"summarize: "`:

```python
summary = summarizer("summarize: " + text, max_length=150, min_length=30, truncation=True)
```

---

### ✅ Final Tip: Test with a Simple String

Add this test to debug:

```python
# Test summarization
if st.checkbox("Run test summary"):
    test_text = "Artificial intelligence is a wonderful field that enables machines to learn from data and make decisions. It is widely used in healthcare, finance, and technology."
    st.write("Test summary:", summarize_text(test_text))
```

If this fails, the issue is in your environment or model download.

---

### ✅ Summary: Checklist to Avoid the Error

| ✅ Do This | Why |
|---------|------|
| Trim and clean input | Remove whitespace-only text |
| Truncate long texts to 1024 tokens | BART can't handle more |
| Ensure at least 20+ words | Avoid too-short input |
| Use `truncation=True` | Prevent overflow |
| Check `if summary:` before accessing `[0]` | Avoid `IndexError` |
| Handle exceptions gracefully | Show user-friendly message |

---

### 🎉 You’re All Set!

With these fixes, your app should **no longer crash** with "index out of range" and instead give **helpful feedback**.

Let me know:
- If you're still getting the error
- What kind of PDF/text you're testing with
- Whether you want to add **chunked summarization** for long documents

You're very close to a rock-solid summarizer! 💪🚀