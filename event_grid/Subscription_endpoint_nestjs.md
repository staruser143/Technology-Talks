Yes, you should create a subscription with a webhook endpoint targeting your NestJS service. By doing this, your NestJS service can directly receive and process events from Azure Event Grid. Here are the steps in more detail:

1. **Define a Controller in Your NestJS Service**:
   - Create a controller that handles HTTP POST requests, which will be the endpoint for Event Grid events.

   ```typescript
   import { Controller, Post, Body, Res } from '@nestjs/common';
   import { Response } from 'express';

   @Controller('events')
   export class EventsController {
     @Post('event-grid')
     handleEvent(@Body() event: any, @Res() res: Response) {
       if (event[0] && event[0].data && event[0].data.validationCode) {
         // Respond to validation event
         return res.status(200).json({ validationResponse: event[0].data.validationCode });
       }
       // Process other events
       console.log('Received event:', event);
       res.status(200).send();
     }
   }
   ```

2. **Deploy Your NestJS Service**:
   - Ensure your NestJS service is deployed and publicly accessible. You can use Azure App Service, Azure Kubernetes Service, or any other hosting platform.

3. **Create an Event Subscription with Webhook Endpoint**:
   - In the Azure portal, navigate to your Event Grid Topic and create a new event subscription.
   - Select "Webhook" as the endpoint type and provide the URL of your NestJS service's endpoint.

   Example URL: `https://your-service-url/events/event-grid`

4. **Validate the Subscription**:
   - When you create the subscription, Azure Event Grid sends a validation event to your webhook endpoint. Your service must handle this validation event and respond with the validation code.

   The provided controller example includes code to handle the validation event by checking for a `validationCode` in the incoming event and responding appropriately.

By following these steps, you can effectively set up your NestJS service as an Event Grid consumer using a webhook endpoint. If you have any further questions or need additional assistance, feel free to ask!