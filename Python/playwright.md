Thank you for sharing the details — this is a **critical issue**, and the output you're seeing (gibberish like `"thea: and to of fille int- is de for’i..."`) clearly shows that:

> ❌ The text extracted from the URL is **corrupted or not properly decoded**  
> ❌ The summarizer is summarizing **garbage**, so the output is meaningless

Let’s fix this **step by step**.

---

## 🔍 Root Cause: Why You’re Getting Gibberish

The URL you shared:
```
https://www.geeksforgeeks.org/git/how-to-create-branch-in-git/
```

is a **modern JavaScript-heavy website**.

### 🧨 Problem:
- `trafilatura` (and similar tools) rely on **HTML parsing**.
- But **GeeksforGeeks loads content dynamically via JavaScript**, so:
  - The raw HTML you download has **no real article text**
  - Or it contains **minified/obfuscated content**
  - Or `trafilatura` fails and returns **partial binary or corrupted text**

➡️ This leads to **gibberish extraction**, which then gets summarized into nonsense.

---

## ✅ Solution: Use a Tool That Executes JavaScript

To extract text from **JavaScript-rendered sites**, you need a **headless browser** that can:
- Load the page
- Execute JavaScript
- Wait for content to appear
- Then extract clean text

### ✅ Recommended: Use `playwright` or `selenium`

We’ll use **`playwright`** — faster and more modern.

---

### ✅ Step 1: Install Required Packages

In Google Colab or your environment:

```bash
!pip install playwright trafilatura[web] PyPDF2 nltk tiktoken
!playwright install-deps
!playwright install chromium
```

---

### ✅ Step 2: Replace `extract_text_from_url` with Playwright Version

Replace your current URL extraction function with this:

```python
from playwright.sync_api import sync_playwright
import time

def extract_text_from_url(url):
    """
    Extract clean text from JavaScript-heavy sites using Playwright.
    """
    if not url.startswith(("http://", "https://")):
        return "❌ Invalid URL. Include http:// or https://"

    try:
        with sync_playwright() as p:
            # Launch browser in non-headless mode (optional: set headless=True for speed)
            browser = p.chromium.launch(headless=True)
            context = browser.new_context(
                viewport={'width': 1280, 'height': 1080},
                user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            )
            page = context.new_page()

            # Navigate and wait for network to settle
            page.goto(url, timeout=60000)
            page.wait_for_load_state("networkidle")  # Wait until no network requests
            time.sleep(2)  # Extra wait for dynamic content

            # Extract main content
            # GeeksforGeeks stores article in <div class="content">
            content = page.eval_on_selector("article, .content, .post-body", "el => el.innerText")

            browser.close()

            if not content or len(content.strip()) < 200:
                return "❌ Could not extract meaningful text. Page may be empty or blocked."

            # Clean up extra whitespace
            content = ' '.join(content.split())
            return content.strip()

    except Exception as e:
        return f"❌ Extraction failed: {str(e)}"
```

---

### ✅ Step 3: Test with Your URL

Try it:

```python
url = "https://www.geeksforgeeks.org/git/how-to-create-branch-in-git/"
text = extract_text_from_url(url)
print(text[:500])  # Print first 500 chars
```

✅ You should now see clean text like:
> "In Git, a branch is a lightweight movable pointer to a commit. The default branch is usually called master or main. Creating a new branch allows you to work on a feature or bug fix without affecting the main codebase..."

---

### ✅ Step 4: Optional – Fallback to `trafilatura` for Static Sites

You can combine both methods:

```python
def extract_text_from_url(url):
    # First try Playwright
    text = extract_with_playwright(url)
    if text.startswith("❌") or len(text) < 300:
        st.warning("Playwright failed, trying trafilatura...")
        text = extract_with_trafilatura(url)
    return text
```

---

## 🛠 Why `trafilatura` Failed

| Tool | Works On | Fails On |
|------|--------|---------|
| `trafilatura`, `newspaper3k` | Static HTML sites (blogs, news) | JS-heavy sites (GeeksforGeeks, React apps) |
| `playwright`, `selenium` | ✅ All sites (static + dynamic) | Slightly slower, needs browser |

---

## ✅ Pro Tips

### 1. **Target the Right HTML Element**
GeeksforGeeks uses:
```html
<div class="content"> ... </div>
```
So we used:
```js
page.eval_on_selector(".content", "el => el.innerText")
```

Use browser DevTools (`F12`) to find the right selector.

### 2. **Add Delay for Dynamic Content**
```python
time.sleep(2)
```
or use:
```python
page.wait_for_selector(".content")
```

### 3. **Set Realistic User-Agent**
Avoid being blocked:
```python
user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
```

---

## ✅ Final Output You Should Get

After fixing:
- ✅ Clean text extraction
- ✅ Real sentences
- ✅ Meaningful chunk summaries
- ✅ Accurate final summary

Example summary:
> "In Git, branches are used to create isolated environments for developing new features or fixing bugs. The command `git branch <branch-name>` creates a new branch, and `git checkout <branch-name>` switches to it. Alternatively, `git switch -c <branch-name>` creates and switches in one step. Branches help maintain a clean main branch and support collaborative development."

---

## ✅ Summary

| Issue | Fix |
|------|-----|
| Gibberish text | Site uses JavaScript — `trafilatura` can't handle it |
| Wrong content | Use `playwright` to render JS |
| Bad summaries | Garbage in → garbage out. Fix extraction first |
| Works on news, not GeeksforGeeks | Dynamic vs static sites need different tools |

---

✅ You’re not doing anything wrong — you just hit a common wall with modern websites.

Now you have the **right tool (Playwright)** to extract clean text from **any website**, including GeeksforGeeks, Medium, or React-based blogs.

Let me know if you want:
- A full updated `app.py` with Playwright
- How to run it efficiently in Colab
- Or how to cache results to avoid re-fetching

You're very close to a bulletproof summarizer! 🚀🧠