Here’s a clear, hands-on guide to **LiquidJS** and **MJML**, and exactly **how we use them together** in the notification framework.

---

## 🔤 What is LiquidJS (and why use it here)?

**LiquidJS** is a JavaScript implementation of **Liquid**, a safe, logic-light templating language (popularized by Shopify). It’s ideal for letting **non-developers** author dynamic content while keeping application logic **out of templates**.

**Key features we leverage:**
- **Variables & dot accessors**: `{{ user.firstName }}`
- **Filters** for formatting: `{{ amount | currency: 'INR' }}`
- **Control flow**: `{% if %}{% elsif %}{% endif %}`, `{% for %}`
- **Partials** / includes: `{% include 'footer' %}`
- **Strictness & safety**: `strictVariables`, timeouts, sandboxed filters

**Where we use LiquidJS:**
- Render **channel-specific** content:
  - **Email**: `subject`, `preheader`, and **MJML body** content
  - **SMS**: plain text
  - **Push**: `title` & `body`
- **Localization**: choose strings by `locale`; apply formatting filters
- **Audience-specific variants**: conditionals for premium/free, region-specific disclaimers, etc.

---

## ✉️ What is MJML (and why use it here)?

**MJML** (MailJet Markup Language) is a component-based markup language that **compiles to responsive HTML email** that works across Outlook, Gmail, Apple Mail, etc. HTML email is notoriously brittle—MJML abstracts that complexity.

**Why it’s perfect for email notifications:**
- **Components** (`mj-section`, `mj-column`, `mj-text`, `mj-button`, `mj-image`)
- **Responsive by default** (mobile-first)
- **Inlining and compatibility** handled for you
- Easy to build **content blocks** your content team can edit confidently

**Where we use MJML:**
- Author email layout in MJML (with **Liquid placeholders** embedded)
- Compile to bulletproof **HTML** (and optionally generate a text fallback)

---

## 🔗 How LiquidJS and MJML work together (render pipeline)

> **Order matters.** We **render Liquid first** (to produce final MJML), then **compile MJML to HTML**.

**Why Liquid → MJML → HTML?**
- Liquid controls *what blocks/sections* exist (conditionals, loops)
- MJML needs the final markup to compile responsive HTML accurately

**Pipeline per email:**
1. Fetch **template** (`email.mjml`, `email.text`, `subject`) from CMS/config.
2. **Render Liquid** with your payload → **final MJML** + **subject** + **text**.
3. **Compile MJML → HTML**.
4. (Optional) generate **text fallback** from HTML or use explicit `email.text`.
5. Return `{ subject, html, text }` to the Email worker.

For **SMS**/**Push**: just step 1–2 (Liquid render on plain text/JSON).

---

## 🧩 Authoring Model (CMS-friendly)

In your CMS (e.g., Strapi/Contentful), store one **Template Definition** per `templateKey`, with per-channel & per-locale parts:

```json
{
  "key": "order.confirmation",
  "version": 7,
  "locales": ["en-IN", "ta-IN"],
  "channels": {
    "email": {
      "subject": "Order {{ order.id }} is confirmed",
      "preheader": "Arriving by {{ order.eta | date: '%b %d' }}",
      "mjml": "<mjml>
        <mj-body background-color=\"#f5f6f8\">
          <mj-section>
            <mj-column>
              <mj-text>Hello {{ user.firstName }},</mj-text>
              <mj-text>Your order <b>{{ order.id }}</b> is confirmed.</mj-text>
              {% if order.items and order.items.size > 0 %}
                <mj-text>Items:</mj-text>
                {% for it in order.items %}
                  <mj-text>- {{ it.name }} × {{ it.qty }} — {{ it.price | currency: 'INR' }}</mj-text>
                {% endfor %}
              {% endif %}
              <mj-button href=\"{{ order.trackingUrl }}\">Track Order</mj-button>
              {% include 'email.footer' %}
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>",
      "text": "Hi {{ user.firstName }}, your order {{ order.id }} is confirmed. Track: {{ order.trackingUrl }}"
    },
    "sms": {
      "text": "Order {{ order.id }} confirmed. ETA {{ order.eta | date: '%d-%m-%Y' }}."
    },
    "push": {
      "title": "Order {{ order.id }} confirmed",
      "body": "Arriving by {{ order.eta | date: '%b %d' }}"
    }
  },
  "schema": {
    "required": ["user.firstName","order.id","order.eta"]
  }
}
```

**Notes:**
- We can store **partials/includes** (e.g., `email.footer`) as separate CMS entries by key.
- We keep **JSON Schema** to validate payload completeness before rendering.

---

## 🛠️ Implementation (Node/NestJS)

### 1) Configure LiquidJS (safety + filters)
```ts
import { Liquid } from 'liquidjs';

export function createLiquid() {
  const engine = new Liquid({
    // security & correctness
    strictVariables: true,         // error on missing vars
    strictFilters: true,
    trimTagRight: true,
    trimOutputRight: true,
    greedy: false,                 // conservative whitespace control
    jsTruthy: false
  });

  // Common filters
  engine.registerFilter('currency', (val: any, code = 'INR') => {
    const n = Number(val ?? 0);
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: code }).format(n);
  });

  engine.registerFilter('date', (val: any, fmt = '%Y-%m-%d') => {
    const d = new Date(val);
    // a tiny strftime-like impl:
    const pad = (x: number) => x.toString().padStart(2,'0');
    const map: Record<string,string> = {
      '%Y': d.getFullYear().toString(),
      '%m': pad(d.getMonth()+1),
      '%d': pad(d.getDate()),
      '%b': d.toLocaleString('en-US', { month: 'short' }),
      '%H': pad(d.getHours()),
      '%M': pad(d.getMinutes())
    };
    return Object.keys(map).reduce((s,k)=>s.replaceAll(k,map[k]), fmt);
  });

  // defensive: limit loops/size via render options per call (see below)
  return engine;
}
```

### 2) Render Liquid, then compile MJML
```ts
import mjml2html from 'mjml';
import { createLiquid } from './liquid';

const liquid = createLiquid();

export async function renderEmail(tmpl: { subject: string; preheader?: string; mjml: string; text?: string }, data: any) {
  // 1) Liquid render
  const subject = await liquid.parseAndRender(tmpl.subject, data, { /* render opts like timeout */ });
  const preheader = tmpl.preheader ? await liquid.parseAndRender(tmpl.preheader, data) : undefined;
  const mjml = await liquid.parseAndRender(tmpl.mjml, data);
  const text = tmpl.text ? await liquid.parseAndRender(tmpl.text, data) : undefined;

  // 2) Compile MJML → HTML
  const { html, errors } = mjml2html(mjml, { keepComments: false, minify: true, validationLevel: 'soft' });
  if (errors?.length) {
    // log/report them; often warnings are still OK. For hard errors, throw.
  }

  // 3) Inject preheader (optional small text at top, hidden visually)
  const finalHtml = preheader
    ? html.replace('<body', `<body><div style="display:none;max-height:0;overflow:hidden">${preheader}</div>`)
    : html;

  // 4) Text fallback (prefer explicit text; otherwise derive from HTML using an html-to-text step in workers)
  const finalText = text ?? undefined;

  return { subject, html: finalHtml, text: finalText };
}

export async function renderSms(tmpl: { text: string }, data: any) {
  const text = await liquid.parseAndRender(tmpl.text, data);
  return { text };
}

export async function renderPush(tmpl: { title: string; body: string }, data: any) {
  const title = await liquid.parseAndRender(tmpl.title, data);
  const body = await liquid.parseAndRender(tmpl.body, data);
  return { title, body };
}
```

### 3) Caching & performance
- **Cache template payloads** (per `templateKey/channel/locale`) in Redis (5–10 min).
- **Pre-parse** Liquid templates: `const tpl = liquid.parse(src)` once, then `liquid.render(tpl, data)`—store parsed AST in memory cache for speed.
- **Cache partials**: implement `engine.registerFileSystem(...)` to resolve `{% include 'key' %}` from CMS/Redis.
- Keep **MJML compilation** in the critical path only once per send. If a template is static for a batch, render per user but compile each time (MJML must see final markup). With **heavy throughput**, consider:
  - Smaller MJML (fewer components) or
  - **Pre-building** shared layout as partial + minimal dynamic areas.

---

## 🧪 Validation & Safety

**Before rendering:**
- Validate incoming `payload` against the template’s **JSON Schema**.
- Enforce **required variables** (catch missing `order.id` before rendering).
- For **SMS**, compute **GSM-7 vs UCS-2** and segment count—avoid bill surprises.
- Limit **loop sizes** and **string lengths** in Liquid render context.
- Use `strictVariables: true` to fail fast on typos / missing fields.

**Security:**
- Treat all data as **untrusted**.
- Escape where needed: for HTML contexts, Liquid content is plain text by default—if you interpolate HTML, sanitize it first.
- Never allow templates to call arbitrary functions; only **registered filters**.
- Set **render timeouts** (e.g., 200–500 ms) to avoid pathological inputs.

---

## 🌏 Localization & Personalization

- Pass `locale` via orchestrator; choose the right locale variant of the template from CMS.
- Filters for **numbers**, **currency**, **dates**—backed by `Intl`.
- Pluralization patterns (simple): 
  ```liquid
  {% if count == 1 %}1 item{% else %}{{ count }} items{% endif %}
  ```
  (If you need ICU MessageFormat-level pluralization, add a custom `i18n` filter or pre-resolve strings server-side.)

---

## 🧰 Operational Tips for Email (MJML → HTML)

- **Images & assets**: host on HTTPS CDN; pass `assetBaseUrl` in data, then `{{ assetBaseUrl }}/logo.png`.
- **Tracking**: add a 1×1 pixel `<img>` or use provider-native open tracking (SendGrid/SES/ACS options).
- **Dark mode**: test; MJML-generated HTML is okay but consider colors that adapt.
- **BIMI/DMARC/SPF/DKIM**: deliverability setup is separate but essential.
- **Accessibility**: `alt` text on images, sufficient contrast, semantic structure via `mj-text` and headings.

---

## 📱 SMS & Push specifics

- **SMS limits**: Aim for concise messages; consider 160 GSM-7 or 70 UCS-2 per segment. Add a counter in preview UI.
- **DLT (India)**: Keep a **DLT template ID** mapping by `templateKey`. In SMS job, attach the ID for routing compliance.
- **Push**: Keep titles short; body concise. Handle platform-specific quirks (emoji, truncation). Use Liquid for small variants.

---

## 🔄 Where this plugs into the Orchestrator

1. Orchestrator receives command → resolves `templateKey` + `locale`.
2. Fetch template definition (from cache → CMS/DB).
3. **Liquid render** for the target channel.
4. For email, **MJML compile** → HTML (and subject/preheader).
5. Enqueue **ChannelJob** with rendered content to the appropriate worker.

This was the essence of the `TemplateService` we sketched earlier.

---

## ✅ Best Practices Checklist

- [ ] Separate **business rules** (`NotificationDefinition/ChannelPolicy`) from **content** (templates).
- [ ] **Version** templates; keep an audit trail.
- [ ] Enforce **schema validation** pre-render.
- [ ] Use `strictVariables` and **timeouts** in Liquid.
- [ ] Cache template parts; **pre-parse** Liquid ASTs.
- [ ] Render **Liquid first**, then **MJML**.
- [ ] Provide **preview tooling** (per locale, per user persona); include SMS length meter.
- [ ] Add **screenshot diff tests** (optional) for email regressions.
- [ ] Handle **fallback text** for email; sanitize HTML if interpolating raw.
- [ ] Keep **filters library** small, safe, and well-tested.

---


If you share your **CMS choice** (Strapi vs Contentful vs Git-based), I’ll tailor the data model and adapter code.
