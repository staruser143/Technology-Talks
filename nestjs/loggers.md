To use logging in NestJS, follow these steps:

1. **Install the required package**:
   ```sh
   npm install @nestjs/common
   ```

2. **Use the built-in Logger**:
   - Import the `Logger` from `@nestjs/common`.
   - Use it in your services, controllers, or other parts of the application.

   **Example**:
   ```typescript
   import { Logger, Injectable } from '@nestjs/common';

   @Injectable()
   export class AppService {
     private readonly logger = new Logger(AppService.name);

     getHello(): string {
       this.logger.log('Returning Hello World!');
       return 'Hello World!';
     }
   }
   ```

3. **Customizing Logger**:
   - You can create a custom logger by extending the `Logger` class.
   - Override methods to customize the logging behavior.

   **Example**:
   ```typescript
   import { Logger } from '@nestjs/common';

   export class MyLogger extends Logger {
     log(message: string) {
       // Add custom logic here
       super.log(message);
     }
   }
   ```

4. **Using third-party logging libraries**:
   - You can integrate third-party logging libraries like `winston` or `pino`.
   - Use the `LoggerService` interface to create a custom logger.

   **Example with `winston`**:
   ```typescript
   import { LoggerService, Injectable } from '@nestjs/common';
   import * as winston from 'winston';

   @Injectable()
   export class WinstonLogger implements LoggerService {
     private logger: winston.Logger;

     constructor() {
       this.logger = winston.createLogger({
         transports: [
           new winston.transports.Console(),
           new winston.transports.File({ filename: 'app.log' }),
         ],
       });
     }

     log(message: string) {
       this.logger.info(message);
     }

     error(message: string, trace: string) {
       this.logger.error(message, trace);
     }

     warn(message: string) {
       this.logger.warn(message);
     }

     debug(message: string) {
       this.logger.debug(message);
     }

     verbose(message: string) {
       this.logger.verbose(message);
     }
   }
   ```

These steps will help you integrate logging into your NestJS application.