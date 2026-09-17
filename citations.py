if user_input := st.chat_input("Ask something about your documents..."):
    with st.chat_message("user"):
        st.write(user_input)
    
    # 1. RAG LOOKUP
    search_results = db.similarity_search(user_input, k=2)
    context_text = "\n".join([doc.page_content for doc in search_results])
    
    # --- NEW: EXTRACT CITATIONS ---
    citations = []
    for doc in search_results:
        source_name = doc.metadata.get("source_file", "Unknown File")
        location = doc.metadata.get("location", "Unknown Location")
        citations.append(f"📄 **{source_name}** ({location})")
    # Remove any duplicate citations if both chunks came from the exact same spot
    citations = list(set(citations))
    # -------------------------------
    
    # 2. SYSTEM INJECTION
    rag_system_prompt = (
        "You are a helpful data assistant. Use ONLY the following pieces of context to answer the question. "
        "If you don't know the answer based on this context, say 'I cannot find that in my database.'\n\n"
        f"--- CONTEXT ---\n{context_text}\n-----------"
    )
    
    clean_history = [msg for msg in active_messages if msg["role"] != "system"]
    payload = [{"role": "system", "content": rag_system_prompt}] + clean_history
    payload.append({"role": "user", "content": user_input})

    with st.chat_message("assistant"):
        stream_completion = client.chat.completions.create(
            model="llama-3.3-70b-versatile", 
            messages=payload,
            stream=True
        )
        
        def generate_chunks():
            for chunk in stream_completion:
                if chunk.choices and chunk.choices[0].delta.content is not None:
                    yield chunk.choices[0].delta.content

        ai_response = st.write_stream(generate_chunks())
        
        # --- NEW: DISPLAY CITATIONS EXPANDER ---
        # If the bot successfully answered using database information, show the evidence
        if "I cannot find that in my database" not in ai_response and citations:
            st.markdown("---")
            with st.expander("🔍 View Sources & Evidence"):
                for citation in citations:
                    st.write(citation)
        # ---------------------------------------
        
    active_messages.append({"role": "user", "content": user_input})
    active_messages.append({"role": "assistant", "content": ai_response})
    save_chat(st.session_state.current_chat, active_messages)
