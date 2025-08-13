import nltk
from nltk.tokenize import sent_tokenize

# Download required NLTK data (only once)
nltk.download('punkt')

# Example text
text = """
Natural language processing (NLP) is a subfield of linguistics, computer science, and artificial intelligence. 
It concerns the interaction between computers and human language. 
One major application is machine translation. Google Translate uses advanced NLP models.
"""

# Safe sentence splitting
sentences = sent_tokenize(text)
for i, sent in enumerate(sentences):
    print(f"{i+1}: {sent}")