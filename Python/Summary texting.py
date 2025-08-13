from transformers import pipeline
import nltk
from nltk.tokenize import sent_tokenize

# Download required NLTK data (run once)
nltk.download('punkt', quiet=True)

# Initialize the summarizer once
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

def count_tokens(text):
    """
    Estimate token count (roughly 1 token ≈ 4 chars in BART, but we'll use sentence-based estimation).
    Alternatively, use tokenizer directly.
    """
    # Simple approximation: Hugging Face tokenizers split differently, but this is good enough
    return len(text.split())

def summarize_text(text, max_summary_pct=0.5, min_length=30, do_sample=False):
    """
    Summarizes text with dynamic max_length based on input length.
    
    Args:
        text: Input text to summarize
        max_summary_pct: Max summary length as percentage of input (default 50%)
        min_length: Minimum summary length
        do_sample: Whether to use sampling (for creativity)
    """
    if len(text.strip()) == 0:
        return "Please enter valid text to summarize."

    # Estimate input length in tokens (words)
    input_length = count_tokens(text)
    
    # Set max_length to at most X% of input length
    dynamic_max = int(input_length * max_summary_pct)
    
    # But don't exceed model's max (e.g., 1024), and respect user settings
    dynamic_max = min(dynamic_max, 1024)  # BART max is 1024
    dynamic_max = max(min_length + 10, dynamic_max)  # Ensure max > min

    try:
        # Now call summarizer with safe max_length
        summary = summarizer(
            text,
            max_length=dynamic_max,
            min_length=min_length,
            do_sample=do_sample
        )
        return summary[0]['summary_text']
    except Exception as e:
        return f"Error during summarization: {str(e)}"