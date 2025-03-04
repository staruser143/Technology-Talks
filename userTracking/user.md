## Track and Persist Quote App Details before(Anonymous) and after User Registration
* When the user hasn't registered yet but can still start a quote application, we need a strategy to track and persist the quote application details both **before and after registration**.
* This requires a combination of **anonymous tracking** (before registration) and **user-linked tracking** (after registration).

## Below are strategies to achieve this using **ReactJS (frontend)** and **NestJS (backend)**:

---

### **1. Anonymous Tracking (Before Registration)**

#### **Strategy: Use a Temporary Quote ID**
- Generate a unique **temporary quote ID** (e.g., UUID) on the frontend when the user starts the quote process.
- Persist the quote details in the backend using this temporary ID.
- Once the user registers, link the temporary quote ID to the newly created user account.

#### **Frontend (ReactJS)**
1. Generate a temporary quote ID when the user starts the quote process:
   ```javascript
   const generateTemporaryQuoteId = () => {
     return 'temp-quote-' + crypto.randomUUID(); // or use a library like `uuid`
   };

   const startQuoteProcess = () => {
     const temporaryQuoteId = generateTemporaryQuoteId();
     localStorage.setItem('temporaryQuoteId', temporaryQuoteId); // Store in localStorage
     // Send the temporaryQuoteId to the backend to initialize the quote
     fetch('/api/quote/start', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ temporaryQuoteId }),
     });
   };
   ```

2. Use the temporary quote ID for all subsequent API calls:
   ```javascript
   const saveQuoteDetails = (details) => {
     const temporaryQuoteId = localStorage.getItem('temporaryQuoteId');
     fetch('/api/quote/save-details', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ temporaryQuoteId, details }),
     });
   };
   ```

#### **Backend (NestJS)**
1. Create a temporary quote entity in the database:
   ```typescript
   @Entity()
   export class TemporaryQuote {
     @PrimaryGeneratedColumn('uuid')
     id: string;

     @Column('jsonb')
     details: any;

     @Column({ nullable: true })
     userId: string; // Will be populated after registration
   }
   ```

2. Handle the `startQuote` and `saveQuoteDetails` endpoints:
   ```typescript
   @Post('/quote/start')
   async startQuote(@Body() body: { temporaryQuoteId: string }) {
     const quote = new TemporaryQuote();
     quote.id = body.temporaryQuoteId;
     await this.quoteRepository.save(quote);
     return { success: true };
   }

   @Post('/quote/save-details')
   async saveQuoteDetails(@Body() body: { temporaryQuoteId: string; details: any }) {
     const quote = await this.quoteRepository.findOne({ where: { id: body.temporaryQuoteId } });
     if (!quote) throw new NotFoundException('Quote not found');
     quote.details = body.details;
     await this.quoteRepository.save(quote);
     return { success: true };
   }
   ```

---

### **2. Linking the Quote to a User (After Registration)**

#### **Strategy: Associate Temporary Quote ID with User**
- When the user registers, send the temporary quote ID along with the registration details.
- Update the temporary quote entity in the database to link it to the new user ID.

#### **Frontend (ReactJS)**
1. Include the temporary quote ID in the registration request:
   ```javascript
   const registerUser = (userDetails) => {
     const temporaryQuoteId = localStorage.getItem('temporaryQuoteId');
     fetch('/api/auth/register', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ ...userDetails, temporaryQuoteId }),
     }).then(() => {
       localStorage.removeItem('temporaryQuoteId'); // Clear the temporary ID
     });
   };
   ```

#### **Backend (NestJS)**
1. Update the temporary quote with the user ID after registration:
   ```typescript
   @Post('/auth/register')
   async registerUser(@Body() body: { temporaryQuoteId: string; userDetails: any }) {
     const user = new User();
     Object.assign(user, body.userDetails);
     await this.userRepository.save(user);

     if (body.temporaryQuoteId) {
       const quote = await this.quoteRepository.findOne({ where: { id: body.temporaryQuoteId } });
       if (quote) {
         quote.userId = user.id;
         await this.quoteRepository.save(quote);
       }
     }

     return { success: true, userId: user.id };
   }
   ```

---

### **3. Handling Quote Continuation After Registration**

#### **Strategy: Use User ID for Quote Persistence**
- After registration, all quote-related actions should use the user ID instead of the temporary quote ID.
- Fetch the existing quote details using the user ID and allow the user to continue.

#### **Frontend (ReactJS)**
1. Fetch the user's quote details after login:
   ```javascript
   const fetchQuoteDetails = (userId) => {
     fetch(`/api/quote/details?userId=${userId}`)
       .then(response => response.json())
       .then(data => {
         // Populate the UI with the existing quote details
       });
   };
   ```

#### **Backend (NestJS)**
1. Fetch the quote details using the user ID:
   ```typescript
   @Get('/quote/details')
   async getQuoteDetails(@Query('userId') userId: string) {
     const quote = await this.quoteRepository.findOne({ where: { userId } });
     if (!quote) throw new NotFoundException('Quote not found');
     return quote.details;
   }
   ```

---

### **4. Event Sourcing for Quote Persistence**

#### **Strategy: Use Events to Track Quote Changes**
- Use event sourcing to track all changes to the quote, both before and after registration.
- Events can include:
  - `QuoteStarted` (anonymous or user-linked)
  - `QuoteDetailsUpdated`
  - `QuoteLinkedToUser` (after registration)

#### **Backend (NestJS)**
1. Emit events for each action:
   ```typescript
   @Post('/quote/save-details')
   async saveQuoteDetails(@Body() body: { temporaryQuoteId: string; details: any }) {
     const quote = await this.quoteRepository.findOne({ where: { id: body.temporaryQuoteId } });
     if (!quote) throw new NotFoundException('Quote not found');
     quote.details = body.details;
     await this.quoteRepository.save(quote);

     // Emit event
     this.eventBus.publish(new QuoteDetailsUpdatedEvent(quote.id, body.details));
     return { success: true };
   }
   ```

2. Use an event store to persist events:
   ```typescript
   @Entity()
   export class QuoteEvent {
     @PrimaryGeneratedColumn('uuid')
     id: string;

     @Column()
     eventType: string;

     @Column('jsonb')
     payload: any;

     @Column()
     timestamp: Date;
   }
   ```

---

### **5. Summary of Workflow**

1. **Before Registration:**
   - Generate a temporary quote ID.
   - Persist quote details using the temporary ID.
   - Use event sourcing to track changes.

2. **After Registration:**
   - Link the temporary quote ID to the user ID.
   - Fetch and continue the quote using the user ID.
   - Use event sourcing to maintain a complete history.

3. **Event Sourcing:**
   - Emit events for all quote-related actions.
   - Persist events in an event store for audit and replay.

By combining **anonymous tracking** with **user-linked persistence** and **event sourcing**, you can ensure a seamless experience for users who start a quote before registering. This approach also provides a robust audit trail and flexibility for future changes.
