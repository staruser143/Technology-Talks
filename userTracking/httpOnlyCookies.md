Using **HTTP-only cookies** to store a temporary quote ID is a more secure alternative to `localStorage` because HTTP-only cookies are not accessible via JavaScript, making them immune to **XSS (Cross-Site Scripting)** attacks. Below, I'll explain how to implement this in both the **frontend (ReactJS)** and **backend (NestJS)**.

---

### **1. Frontend (ReactJS)**

Since HTTP-only cookies cannot be set or read directly by JavaScript, the frontend will rely on the backend to set and manage the cookie. The frontend only needs to make API requests, and the backend will handle the cookie operations.

#### **Steps in ReactJS:**
1. **Start Quote Process:**
   - The frontend makes an API call to the backend to start the quote process.
   - The backend sets the temporary quote ID in an HTTP-only cookie.

2. **Save Quote Details:**
   - The frontend sends quote details to the backend.
   - The backend associates the details with the temporary quote ID stored in the cookie.

3. **Retrieve Quote Details:**
   - The frontend makes an API call to fetch the quote details.
   - The backend reads the temporary quote ID from the cookie and returns the associated details.

#### **Example Code:**
```javascript
// Start the quote process
const startQuoteProcess = async () => {
  const response = await fetch('/api/quote/start', {
    method: 'POST',
    credentials: 'include', // Include cookies in the request
  });
  const data = await response.json();
  console.log('Quote started:', data);
};

// Save quote details
const saveQuoteDetails = async (details) => {
  const response = await fetch('/api/quote/save-details', {
    method: 'POST',
    credentials: 'include', // Include cookies in the request
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ details }),
  });
  const data = await response.json();
  console.log('Details saved:', data);
};

// Fetch quote details
const fetchQuoteDetails = async () => {
  const response = await fetch('/api/quote/details', {
    credentials: 'include', // Include cookies in the request
  });
  const data = await response.json();
  console.log('Quote details:', data);
};
```

---

### **2. Backend (NestJS)**

The backend is responsible for:
- Setting the HTTP-only cookie with the temporary quote ID.
- Reading the temporary quote ID from the cookie for subsequent requests.
- Associating quote details with the temporary quote ID.

#### **Steps in NestJS:**
1. **Set HTTP-only Cookie:**
   - When the quote process starts, generate a temporary quote ID and set it in an HTTP-only cookie.

2. **Read Cookie:**
   - For subsequent requests, read the temporary quote ID from the cookie.

3. **Persist Quote Details:**
   - Use the temporary quote ID to save and retrieve quote details.

#### **Example Code:**

##### **Install Dependencies:**
Install the `cookie-parser` middleware to handle cookies in NestJS:
```bash
npm install cookie-parser
```

##### **Enable Cookies in NestJS:**
```typescript
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as cookieParser from 'cookie-parser';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.use(cookieParser()); // Enable cookie parsing
  await app.listen(3000);
}
bootstrap();
```

##### **Set HTTP-only Cookie:**
```typescript
import { Controller, Post, Res } from '@nestjs/common';
import { Response } from 'express';

@Controller('quote')
export class QuoteController {
  @Post('start')
  startQuote(@Res() res: Response) {
    const temporaryQuoteId = 'temp-quote-' + Math.random().toString(36).substring(7); // Generate a temporary ID
    res.cookie('temporaryQuoteId', temporaryQuoteId, {
      httpOnly: true, // HTTP-only cookie
      secure: process.env.NODE_ENV === 'production', // Secure in production
      sameSite: 'strict', // Prevent CSRF
    });
    return res.json({ success: true, temporaryQuoteId });
  }
}
```

##### **Read Cookie and Save Quote Details:**
```typescript
import { Controller, Post, Body, Req } from '@nestjs/common';
import { Request } from 'express';

@Controller('quote')
export class QuoteController {
  @Post('save-details')
  saveQuoteDetails(@Req() req: Request, @Body() body: { details: any }) {
    const temporaryQuoteId = req.cookies.temporaryQuoteId; // Read the cookie
    if (!temporaryQuoteId) {
      throw new Error('Temporary quote ID not found');
    }
    // Save the details associated with the temporary quote ID (e.g., in a database)
    console.log('Saving details for:', temporaryQuoteId, body.details);
    return { success: true };
  }
}
```

##### **Fetch Quote Details:**
```typescript
import { Controller, Get, Req } from '@nestjs/common';
import { Request } from 'express';

@Controller('quote')
export class QuoteController {
  @Get('details')
  getQuoteDetails(@Req() req: Request) {
    const temporaryQuoteId = req.cookies.temporaryQuoteId; // Read the cookie
    if (!temporaryQuoteId) {
      throw new Error('Temporary quote ID not found');
    }
    // Fetch the details associated with the temporary quote ID (e.g., from a database)
    const details = { /* Fetch details from database */ };
    return { success: true, details };
  }
}
```

---

### **3. Key Considerations**

#### **Security:**
- **HTTP-only**: Ensures the cookie cannot be accessed via JavaScript.
- **Secure Flag**: Ensures the cookie is only sent over HTTPS in production.
- **SameSite Attribute**: Prevents CSRF attacks by restricting cookie usage to the same site.

#### **Cookie Expiry:**
- Set an appropriate expiry time for the cookie to ensure it doesn't persist indefinitely.
  ```typescript
  res.cookie('temporaryQuoteId', temporaryQuoteId, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: 24 * 60 * 60 * 1000, // 1 day
  });
  ```

#### **Cross-Origin Requests:**
- If your frontend and backend are on different domains, configure CORS to allow credentials (cookies):
  ```typescript
  app.enableCors({
    origin: 'https://your-frontend-domain.com',
    credentials: true, // Allow cookies
  });
  ```

---

### **4. Summary of Changes**

| **Component** | **Changes** |
|----------------|-------------|
| **Frontend (ReactJS)** | - Use `credentials: 'include'` in `fetch` calls.<br>- Remove `localStorage` usage. |
| **Backend (NestJS)** | - Use `cookie-parser` middleware.<br>- Set HTTP-only cookies using `res.cookie()`.<br>- Read cookies using `req.cookies`. |

By using HTTP-only cookies, you ensure that the temporary quote ID is securely managed by the browser and inaccessible to malicious scripts. This approach provides a robust and secure way to handle temporary data like quote IDs.