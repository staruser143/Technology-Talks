from pypdf import PdfReader
import os

def extract_text_from_pdf(pdf_file):
    """
    Extract text from an uploaded PDF file.
    pdf_file: Uploaded file object from Streamlit
    """
    try:
        reader = PdfReader(pdf_file)
        text = ""
        for page in reader.pages:
            page_text = page.extract_text()
            if page_text:
                text += page_text
        return text.strip()
    except Exception as e:
        return f"Error reading PDF: {str(e)}"