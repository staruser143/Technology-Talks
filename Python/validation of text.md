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