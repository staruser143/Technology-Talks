from transformers import AutoTokenizer

tokenizer = AutoTokenizer.from_pretrained("facebook/bart-large-cnn")

def count_tokens(text):
    return len(tokenizer.encode(text))