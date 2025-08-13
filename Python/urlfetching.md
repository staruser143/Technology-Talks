Adding **URL summarization** support to your app allows users to input a web link (e.g., a news article), and your app will:

1. 🕷️ Scrape the webpage  
2. 🧹 Extract the main article text (not ads, menus, etc.)  
3. ✍️ Summarize it using your AI model

Let’s walk through how to do this **step by step** in Python, with Streamlit integration.

---

### ✅ Step 1: Install Required Libraries

You’ll need:
- `requests` – to fetch the webpage
- `beautifulsoup4` – to parse HTML
- `trafilatura` – excellent for extracting clean article text (recommended)
  - Or `newspaper3k` as an alternative

Install them:

```bash
pip install requests beautifulsoup4 trafilatura
```

> 🔁 Alternative: `pip install newspaper3k` (but it can be fragile on some sites)

We'll use **`trafilatura`** — it's fast, accurate, and handles paywalls, comments, and ads well.

---

### ✅ Step 2: Create a Function to Extract Text from URL

```python
import requests
from trafilatura import fetch_url, extract

def extract_text_from_url(url):
    """
    Extract main article text from a URL using trafilatura.
    Returns cleaned text or an error message.
    """
    try:
        # Validate URL
        if not url.startswith("http://") and not url.startswith("https://"):
            return "❌ Invalid URL. Please include http:// or https://"

        # Fetch and extract content
        downloaded = fetch_url(url)
        if downloaded is None:
            return "❌ Failed to download the page. Check the URL or internet connection."

        text = extract(downloaded, include_comments=False, include_tables=True, include_formatting=False)
        
        if text is None or len(text.strip()) < 100:
            return "❌ Could not extract meaningful text. The page may be empty or not article-based."

        return text.strip()

    except Exception as e:
        return f"❌ Error: {str(e)}"
```

---

### ✅ Step 3: Update Your Streamlit UI

Add a new tab or input field for URL:

```python
# In your app.py, add a tab for URL input
tab1, tab2, tab3 = st.tabs(["📝 Paste Text", "📁 Upload PDF", "🌐 Summarize URL"])

input_text = ""

with tab3:
    url = st.text_input("Enter Article URL:", placeholder="https://example.com/news-story")
    if st.button("Fetch & Extract Text", key="fetch_url"):
        if not url:
            st.warning("Please enter a URL.")
        else:
            with st.spinner("📡 Fetching article..."):
                input_text = extract_text_from_url(url)
            
            # Display result
            if input_text.startswith("❌"):
                st.error(input_text)
            else:
                st.session_state.fetched_text = input_text  # Save to session state
                st.success(f"✅ Extracted {len(input_text.split())} words.")
                with st.expander("📄 View Extracted Text"):
                    st.write(input_text)

# Allow fetched text to be summarized
if 'fetched_text' in st.session_state:
    input_text = st.session_state.fetched_text
```

> 🔁 We use `st.session_state` so the text persists after switching tabs or clicking "Generate Summary".

---

### ✅ Step 4: Reuse Your Existing Summarizer

Now, when the user clicks **"Generate Summary"**, it will use the latest `input_text`, whether from:
- Text box
- PDF upload
- URL fetch

So your **"Generate Summary" button** logic stays the same:

```python
if st.button("✨ Generate Summary"):
    if not input_text:
        st.warning("No text to summarize. Enter text, upload a PDF, or provide a URL.")
    else:
        with st.spinner("🧠 Summarizing..."):
            summary = summarize_long_document(input_text)  # Your map-reduce function
        st.subheader("✅ Summary")
        st.write(summary)
```

---

### ✅ Example: Test With a Real News Article

Try with:
```
https://www.bbc.com/news/world-us-canada-68920592
```

It should:
1. Fetch the article
2. Extract clean text (title + body)
3. Summarize it

---

### 🔒 Optional: Add Rate Limiting & Safety

To avoid abuse or spamming:

```python
import time

# Add delay between requests
time.sleep(1)
```

And validate domains:
```python
from urllib.parse import urlparse

domain = urlparse(url).netloc
allowed_domains = ["bbc.com", "nytimes.com", "reuters.com", "example.com"]
if not any(d in domain for d in allowed_domains):
    st.warning("⚠️ Only trusted domains allowed in demo mode.")
```

---

### 🆚 Alternative: Use `newspaper3k`

If you prefer `newspaper3k`:

```python
from newspaper import Article

def extract_text_from_url(url):
    try:
        article = Article(url)
        article.download()
        article.parse()
        return article.text.strip()
    except Exception as e:
        return f"❌ Failed: {str(e)}"
```

But `newspaper3k` can fail on many modern sites due to JavaScript-heavy content.

✅ **Stick with `trafilatura`** — it’s more robust.

---

### ✅ Features You Can Add

| Feature | How |
|-------|-----|
| 🔤 Show article title | `extract(..., with_metadata=True)['title']` |
| 🕒 Read estimated time | `len(text.split()) / 200` ≈ minutes |
| 📥 Auto-fetch on paste | Detect URL in text input |
| 🔍 Preview image | Use `og:image` meta tag |
| 🧹 Clean ads/scripts | `trafilatura` already does this |

---

### ✅ Final Notes

- ❌ Doesn’t work with **JavaScript-heavy sites** (e.g., React apps) unless you use `selenium` or `playwright`
- ✅ Works great with **news, blogs, Wikipedia, Medium**
- ⚠️ Some sites block scrapers — respect `robots.txt` and rate limits
- ✅ For production: use caching (`st.cache_data`) to avoid refetching

---

### ✅ Add Caching (Optional)

```python
@st.cache_data(ttl=3600)  # Cache for 1 hour
def fetch_url_cached(url):
    return extract_text_from_url(url)
```

Then call `fetch_url_cached(url)` instead.

---

### 🎉 You're Done!

You now have a **fully functional URL summarizer** that can:
- Extract clean article text from links
- Handle errors gracefully
- Plug into your existing summarization pipeline

---

Let me know if you want:
- Support for **YouTube video transcripts** (via `pytube` + Whisper)
- Summarizing **Twitter/X threads**
- A **browser extension** version

You're building a powerful AI-powered research assistant! 🚀🧠