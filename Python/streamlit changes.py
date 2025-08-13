# app.py - Main App Code (Updated with PDF Support)

import streamlit as st
from pypdf import PdfReader

# --- Summarization Function (from earlier) ---
def summarize_text(text, max_summary_pct=0.4, min_length=30, do_sample=False):
    if len(text.strip()) == 0:
        return "No text to summarize."

    input_length = len(text.split())
    dynamic_max = min(int(input_length * max_summary_pct), 512)
    dynamic_max = max(min_length + 10, dynamic_max)

    try:
        summary = summarizer(text, max_length=dynamic_max, min_length=min_length, do_sample=do_sample)
        return summary[0]['summary_text']
    except Exception as e:
        return f"Error during summarization: {str(e)}"

# --- PDF Extraction Function ---
def extract_text_from_pdf(pdf_file):
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

# --- Streamlit UI ---
st.set_page_config(page_title="📄 AI Text & PDF Summarizer", layout="centered")
st.title("📄 AI Text & PDF Summarizer")
st.write("Upload a PDF or paste text below to generate a concise summary.")

# Tabs for Text Input vs PDF Upload
tab1, tab2 = st.tabs(["📝 Paste Text", "📁 Upload PDF"])

input_text = ""

with tab1:
    input_text = st.text_area("Enter your text here:", height=300, placeholder="Paste your article, essay, or news story...")

with tab2:
    pdf_file = st.file_uploader("Upload a PDF", type="pdf")
    if pdf_file is not None:
        with st.spinner("Extracting text from PDF..."):
            input_text = extract_text_from_pdf(pdf_file)
        if input_text.startswith("Error"):
            st.error(input_text)
        else:
            st.success(f"✅ Extracted {len(input_text.split())} words from PDF.")
            with st.expander("View Extracted Text"):
                st.write(input_text)

# Summary Settings
st.sidebar.header("📝 Summary Settings")
min_len = st.sidebar.slider("Minimum Summary Length", 10, 100, 30)
use_sampling = st.sidebar.checkbox("Use sampling (more creative)", False)

# Generate Summary Button
if st.button("✨ Generate Summary"):
    if not input_text or len(input_text.strip()) == 0:
        st.warning("Please enter text or upload a PDF to summarize.")
    else:
        with st.spinner("🧠 Summarizing..."):
            summary = summarize_text(
                input_text,
                max_summary_pct=0.4,
                min_length=min_len,
                do_sample=use_sampling
            )
        st.subheader("✅ Summary")
        st.write(summary)

        # Optional: Add download button for summary
        st.download_button(
            label="📥 Download Summary",
            data=summary,
            file_name="summary.txt",
            mime="text/plain"
        )
else:
    st.info("Enter text or upload a PDF, then click 'Generate Summary'.")