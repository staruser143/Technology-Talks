if st.button("Generate Summary"):
    if not input_text:
        st.warning("Please enter some text to summarize.")
    else:
        with st.spinner("Summarizing..."):
            summary = summarize_text(
                input_text,
                max_summary_pct=0.4,  # Summary <= 40% of input length
                min_length=min_len
            )
        st.subheader("📝 Summary")
        st.write(summary)