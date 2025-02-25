Yes, that's exactly right! If you're writing a NestJS service to consume events from Azure Event Grid, you can create an event subscription with a webhook endpoint that targets the endpoint of your NestJS service. Here are the high-level steps to achieve this:

1. **Set Up Your NestJS Service**:
   - Define a controller in your NestJS application to handle incoming events. The controller should have an endpoint that can receive HTTP POST requests from Event Grid.

   Here's an example controller in NestJS:

   ```typescript
   import { Controller, Post, Body } from '@nestjs/common';

   @Controller('events')
   export class EventsController {
     @Post('event-grid')
     handleEvent(@Body() event: any) {
       // Process the event here
       console.log('Received event:', event);
     }
   }
   ```

2. **Deploy Your NestJS Service**:
   - Ensure your NestJS service is deployed and publicly accessible. You can use services like Azure App Service, Azure Kubernetes Service, or any other hosting platform.

3. **Create an Event Subscription with Webhook Endpoint**:
   - In the Azure portal, navigate to your Event Grid Topic and create an event subscription.
   - Choose "Webhook" as the endpoint type and specify the URL of your NestJS service's endpoint.
   
   Example URL: `https://your-service-url/events/event-grid`

4. **Event Handling and Validation**:
   - Azure Event Grid sends a validation event when you create a subscription. Your NestJS service needs to handle this validation event to confirm the subscription.
   
   Update your controller to handle the validation event:

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

By following these steps, you can successfully set up your NestJS service to receive events from Azure Event Grid via a webhook endpoint