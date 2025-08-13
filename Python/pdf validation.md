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