Can we apply a interceptor at the handler level when implementing a command handler to execute a command object received in NestJS

Yes, you can apply an interceptor at the handler level when implementing a command handler in NestJS. Interceptors are a powerful feature in NestJS, allowing you to transform and extend the behavior of method invocations.

Here's a simple example to get you started:

1. **Create an Interceptor**: First, create your custom interceptor. For example:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    console.log('Before handling the request...');

    return next
      .handle()
      .pipe(
        tap(() => console.log('After handling the request...')),
      );
  }
}
```

2. **Apply the Interceptor to the Command Handler**: You can apply the interceptor at the handler level using the `@UseInterceptors` decorator:

```typescript
import { Controller } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { LoggingInterceptor } from './logging.interceptor';
import { UseInterceptors } from '@nestjs/common/decorators';

@CommandHandler(MyCommand)
@UseInterceptors(LoggingInterceptor)
export class MyCommandHandler implements ICommandHandler<MyCommand> {
  async execute(command: MyCommand): Promise<void> {
    // Command handling logic
    console.log('Handling the command...');
  }
}
```

In this example, the `LoggingInterceptor` logs messages before and after handling the command. You can extend the interceptor to include more sophisticated logic, such as error handling, validation, or transformations.

If you have specific requirements or scenarios in mind, feel free to share them, and we can tailor the implementation further!


=============

How does this differ from apply interceptor at Global,Controller and Route Level

Applying interceptors at different levels in NestJS affects the scope and granularity of the interception. Here's a breakdown of how they differ:

### Global Level
When applied globally, the interceptor affects all routes across the entire application. This can be useful for cross-cutting concerns like logging, metrics, or global error handling.

- **Pros**:
  - Centralized management for concerns that span across the entire app.
  - Reduces the need for repetitive code.
- **Cons**:
  - Applies to every route, which might be overkill for certain specific needs.

```typescript
import { APP_INTERCEPTOR } from '@nestjs/core';

@Module({
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
  ],
})
export class AppModule {}
```

### Controller Level
When applied at the controller level, the interceptor affects all routes within the specific controller. This is useful for concerns that are relevant to a particular set of endpoints.

- **Pros**:
  - Scoped to the specific controller, ensuring relevance.
  - Less overhead compared to a global interceptor for localized concerns.
- **Cons**:
  - May still be too broad if only a few routes in the controller need it.

```typescript
import { Controller, UseInterceptors } from '@nestjs/common';

@Controller('users')
@UseInterceptors(LoggingInterceptor)
export class UserController {
  // All routes in this controller will use the interceptor
}
```

### Route Level
When applied at the route level, the interceptor affects only the specified route. This is ideal for highly specific concerns where only a particular endpoint needs interception.

- **Pros**:
  - Fine-grained control over which routes the interceptor applies to.
  - Minimizes unnecessary overhead.
- **Cons**:
  - Requires more configuration if many routes need the same interceptor.

```typescript
import { Controller, Get, UseInterceptors } from '@nestjs/common';

@Controller('users')
export class UserController {
  @Get()
  @UseInterceptors(LoggingInterceptor)
  findAll() {
    // Only this route will use the interceptor
  }
}
```

### Handler Level
As you mentioned earlier, applying an interceptor at the command handler level in a CQRS pattern allows you to intercept command handling specifically.

- **Pros**:
  - Directly targets command handling logic.
  - Useful for concerns specific to command execution.
- **Cons**:
  - Only applies to commands, not other types of routes.

```typescript
@CommandHandler(MyCommand)
@UseInterceptors(LoggingInterceptor)
export class MyCommandHandler implements ICommandHandler<MyCommand> {
  async execute(command: MyCommand): Promise<void> {
    console.log('Handling the command...');
  }
}
```

### Summary
- **Global Level**: All routes in the application.
- **Controller Level**: All routes within a specific controller.
- **Route Level**: Specific route(s) within a controller.
- **Handler Level**: Specific command handler in CQRS pattern.

This flexibility allows you to choose the most appropriate level based on your application's requirements. If you have any specific scenarios in mind or further questions, feel free to share!


================

In the example for applying at Global level, is it at module level or across all modules? Also what is the APP_INTERCEPTOR referred here used for?

When you apply an interceptor at the global level, it affects all routes across all modules in the entire application. This ensures that the interceptor's behavior is consistent throughout the app, regardless of the module or controller handling the request.

### `APP_INTERCEPTOR`
The `APP_INTERCEPTOR` token is a special provider in NestJS that allows you to register an interceptor globally. By configuring it in the `AppModule` (or another module's providers array), NestJS will automatically apply the interceptor to every route in the application.

Here's a refresher on how it's done:

```typescript
import { Module } from '@nestjs/common';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { LoggingInterceptor } from './logging.interceptor';

@Module({
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
  ],
})
export class AppModule {}
```

### Why Use `APP_INTERCEPTOR`
Using `APP_INTERCEPTOR` for global interceptors is beneficial for:
- **Cross-Cutting Concerns**: Applying logging, metrics, authentication, and error handling across all routes.
- **Centralized Configuration**: Manage and update the interceptor logic in a single place.
- **Reduced Boilerplate**: Avoid redundant use of `@UseInterceptors` at controller and route levels.

If you have more questions or need further clarifications, feel free to ask!

======================

Can we apply interceptors at module level for one or few modules alone

NestJS does not provide a direct way to apply interceptors at the module level by default. However, you can achieve this by manually applying interceptors to all controllers within a module or by using a custom decorator. Here are two approaches:

### Approach 1: Apply Interceptors to All Controllers in a Module

You can apply interceptors to all controllers within a module by using the `@UseInterceptors` decorator in each controller. Here's an example:

```typescript
import { Module } from '@nestjs/common';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { LoggingInterceptor } from './logging.interceptor';
import { UserController } from './user.controller';
import { ProductController } from './product.controller';

@Module({
  controllers: [UserController, ProductController],
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
  ],
})
export class SomeModule {}
```

### Approach 2: Use a Custom Decorator

You can create a custom decorator to apply interceptors to all controllers within a module. This approach requires a bit more setup but allows for cleaner code.

1. **Create a Custom Decorator**:

```typescript
import { UseInterceptors, applyDecorators } from '@nestjs/common';
import { LoggingInterceptor } from './logging.interceptor';

export function ModuleInterceptors(...interceptors: any[]) {
  return applyDecorators(
    ...interceptors.map((interceptor) => UseInterceptors(interceptor)),
  );
}
```

2. **Apply the Custom Decorator to Controllers**:

```typescript
import { Controller } from '@nestjs/common';
import { ModuleInterceptors } from './module-interceptors.decorator';
import { LoggingInterceptor } from './logging.interceptor';

@Controller('users')
@ModuleInterceptors(LoggingInterceptor)
export class UserController {
  // All routes in this controller will use the interceptor
}

@Controller('products')
@ModuleInterceptors(LoggingInterceptor)
export class ProductController {
  // All routes in this controller will use the interceptor
}
```

In this example, the `ModuleInterceptors` custom decorator can be applied to any controller within a module to apply the specified interceptors.

While there's no out-of-the-box support for applying interceptors at the module level, these approaches help you achieve similar functionality with some customization. If you have specific scenarios or additional requirements, feel free to share, and we can explore further customization!


===========

Does NestJS provide default or built-in interceptors

NestJS does not provide default or built-in interceptors out of the box. However, it offers a flexible and extensible framework that allows you to create custom interceptors tailored to your application's needs.

### Common Use Cases for Custom Interceptors

1. **Logging**: Track incoming requests and outgoing responses.
2. **Metrics**: Collect performance data like response times.
3. **Caching**: Cache responses to improve performance.
4. **Validation**: Validate data before passing it to controllers or services.
5. **Transformation**: Modify or transform data before it's processed or returned.

### Creating a Custom Interceptor

Here's a brief refresher on creating a custom interceptor:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    console.log('Before handling the request...');
    
    return next
      .handle()
      .pipe(
        tap(() => console.log('After handling the request...')),
      );
  }
}
```

You can then apply this interceptor globally, at the controller level, route level, or handler level, as we discussed earlier.

While NestJS doesn't have built-in interceptors, its architecture makes it easy to create and apply interceptors for various purposes. If you need help with specific interceptor implementations or have more questions, feel free to ask!

==========

When should we use Observable in a interceptor

Using `Observable` in an interceptor is essential when you need to handle asynchronous operations, streaming data, or any process that requires observability, such as monitoring for side effects or transforming data streams.

Here are some scenarios where using `Observable` in an interceptor is beneficial:

### 1. **Asynchronous Operations**
Interceptors often handle tasks that might involve asynchronous operations like logging, caching, or making HTTP requests. Using `Observable` allows you to manage these operations in a non-blocking way.

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    console.log('Before handling the request...');
    
    return next
      .handle()
      .pipe(
        tap(() => console.log('After handling the request...')),
      );
  }
}
```

### 2. **Data Transformation**
You might want to transform the data before or after it is handled. Using `Observable`, you can easily manipulate data streams.

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class TransformInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    return next
      .handle()
      .pipe(
        map(data => ({ data, transformed: true })),
      );
  }
}
```

### 3. **Error Handling**
Interceptors can catch and handle errors. Using `Observable`, you can handle errors gracefully and perform necessary actions like logging or returning fallback responses.

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    return next
      .handle()
      .pipe(
        catchError(err => {
          console.error('Error occurred:', err);
          return throwError(() => new Error('An error occurred'));
        }),
      );
  }
}
```

### 4. **Performance Monitoring**
If you need to monitor the performance of your application, such as measuring request handling time, `Observable` provides a way to track the time taken for asynchronous operations.

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class TimingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const now = Date.now();
    return next
      .handle()
      .pipe(
        tap(() => console.log(`Request handled in ${Date.now() - now}ms`)),
      );
  }
}
```

### Summary
- **Use `Observable` in interceptors for asynchronous operations, data transformations, error handling, and performance monitoring.**
- **Observables help manage and manipulate data streams efficiently.**

If you have any specific use cases in mind or need further examples, feel free to share!


===============

Can't we do transformations of the response without using Observable in a interceptor.

In a NestJS interceptor, if the response transformation involves asynchronous operations or needs to handle streams, using `Observable` is the recommended approach. However, if your transformation is synchronous, you can perform the transformation directly within the interceptor without using `Observable`.

Here's an example of a synchronous transformation without `Observable`:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';

@Injectable()
export class SynchronousTransformInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const response = context.switchToHttp().getResponse();
    
    response.on('finish', () => {
      const originalSend = response.send;
      response.send = function (body: any) {
        // Transform the response body synchronously
        const transformedBody = { data: body, transformed: true };
        return originalSend.call(this, transformedBody);
      };
    });
    
    return next.handle();
  }
}
```

In this example, the interceptor synchronously transforms the response body by wrapping the original `response.send` method. This approach works well for simple, synchronous transformations. However, it might not be suitable for more complex scenarios involving asynchronous operations or streams.

### Considerations
- **Simplicity**: Use synchronous transformations for straightforward modifications that don't require asynchronous operations.
- **Scalability**: For more complex scenarios, especially those involving async operations or data streaming, using `Observable` provides better flexibility and scalability.

Let me know if this approach works for your needs or if you have any specific scenarios in mind!


===================

Can you provide an example scenario to understand using Data Streams along with interceptors

Certainly! Let's consider an example scenario where we are streaming data from an external API, transforming the data stream, and then returning the transformed data to the client. We'll use an interceptor to handle the data stream and apply the necessary transformations.

### Scenario: Streaming Data from an External API

1. **External API Call**: We'll make an HTTP request to an external API to stream data.
2. **Data Transformation**: The interceptor will transform the data stream (e.g., filtering, mapping).
3. **Return Transformed Data**: The transformed data stream will be returned to the client.

### Steps to Implement

1. **Create an Interceptor**: Define an interceptor that handles the data stream and applies transformations.
2. **Apply the Interceptor**: Apply the interceptor to a specific route or controller.

### Implementation

1. **Create the Interceptor**:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable, from } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import * as axios from 'axios';

@Injectable()
export class DataStreamInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    // Make an HTTP request to the external API
    const externalApiUrl = 'https://api.example.com/stream';

    return from(axios.default.get(externalApiUrl, { responseType: 'stream' })).pipe(
      switchMap(response => {
        const dataStream = response.data; // This is the stream from the external API
        return dataStream.pipe(
          // Apply data transformations (e.g., filtering, mapping)
          map(chunk => {
            const transformedChunk = chunk.toString().toUpperCase();
            return transformedChunk;
          }),
        );
      }),
    );
  }
}
```

2. **Apply the Interceptor to a Route**:

```typescript
import { Controller, Get, UseInterceptors } from '@nestjs/common';
import { DataStreamInterceptor } from './data-stream.interceptor';

@Controller('data')
export class DataController {
  @Get('stream')
  @UseInterceptors(DataStreamInterceptor)
  streamData() {
    return 'Streaming data from external API...';
  }
}
```

### Explanation

- **DataStreamInterceptor**: This interceptor makes an HTTP request to an external API that returns a data stream. The stream is then transformed by converting each chunk to uppercase using the `map` operator from RxJS.
- **DataController**: The `streamData` route applies the `DataStreamInterceptor`, which handles the data streaming and transformation.

### Summary

In this example, the interceptor is responsible for managing the data stream from an external API, transforming the stream, and returning the transformed data to the client. This approach allows you to handle data streams efficiently and apply necessary transformations within the interceptor.

If you have any specific use cases or further questions, feel free to share!


====
Does that mean we use this when we have a huge volume of data being returned from a service and to overcome memory limiations we process it in chunks using streams.

Yes, exactly! Using data streams is particularly beneficial when dealing with large volumes of data, as it allows you to process the data in chunks rather than loading the entire dataset into memory at once. This approach helps overcome memory limitations and improves the efficiency of your application.

### Benefits of Using Data Streams

1. **Memory Efficiency**: By processing data in chunks, you reduce the memory footprint, which is crucial when handling large datasets.
2. **Performance**: Streaming allows you to start processing data as soon as the first chunk is available, rather than waiting for the entire dataset to be loaded.
3. **Scalability**: Applications that handle large data sets can scale more effectively by leveraging streaming and chunk-based processing.

### Example Scenario

Consider a scenario where you need to process and transform a large CSV file from an external service. Instead of loading the entire file into memory, you can stream it and process each chunk as it arrives.

Here's how you might implement this in an interceptor:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable, from } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import * as axios from 'axios';

@Injectable()
export class CsvStreamInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const externalApiUrl = 'https://api.example.com/large-csv';

    return from(axios.default.get(externalApiUrl, { responseType: 'stream' })).pipe(
      switchMap(response => {
        const csvStream = response.data;
        return csvStream.pipe(
          // Process each chunk of the CSV stream
          map(chunk => {
            const processedChunk = this.processCsvChunk(chunk);
            return processedChunk;
          }),
        );
      }),
    );
  }

  private processCsvChunk(chunk: any): any {
    // Implement your logic to process each chunk of the CSV
    return chunk.toString().toUpperCase(); // Example transformation
  }
}
```

In this example, the `CsvStreamInterceptor` streams a large CSV file from an external service, processes each chunk of the CSV as it arrives, and transforms it. This approach ensures that you handle large datasets efficiently without running into memory limitations.

### Summary

- **Streaming Data**: Process data in chunks to reduce memory usage.
- **Efficiency and Performance**: Start processing data as soon as it arrives.
- **Scalability**: Handle large datasets more effectively.

Using data streams in combination with interceptors is a powerful way to manage and process large volumes of data efficiently. If you have more questions or need further examples, feel free to ask!

=================

Is there a way know that the output from a service can be huge and switch to chunk processing otherwise use the default processing one

Yes, you can implement logic to determine if the output from a service is large and switch to chunk processing accordingly. One way to achieve this is by inspecting the `Content-Length` header of the HTTP response. If the `Content-Length` exceeds a certain threshold, you can switch to chunk processing; otherwise, you can use default processing.

Here's an example of how you might implement this logic in a NestJS interceptor:

1. **Create the Interceptor**:
```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler, HttpException, HttpStatus } from '@nestjs/common';
import { Observable, from, throwError } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import * as axios from 'axios';

@Injectable()
export class ConditionalStreamInterceptor implements NestInterceptor {
  private readonly CHUNK_THRESHOLD = 1000000; // 1 MB (adjust based on your requirements)

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const externalApiUrl = 'https://api.example.com/data';

    return from(axios.default.head(externalApiUrl)).pipe(
      switchMap(response => {
        const contentLength = response.headers['content-length'];
        
        if (contentLength && contentLength > this.CHUNK_THRESHOLD) {
          return from(axios.default.get(externalApiUrl, { responseType: 'stream' })).pipe(
            switchMap(response => {
              const dataStream = response.data;
              return dataStream.pipe(
                // Apply chunk processing
                map(chunk => this.processChunk(chunk)),
              );
            }),
          );
        } else {
          return from(axios.default.get(externalApiUrl)).pipe(
            switchMap(response => next.handle().pipe(
              // Default processing
              map(() => response.data),
            )),
          );
        }
      }),
      catchError(err => {
        console.error('Error occurred:', err);
        return throwError(new HttpException('Failed to process data', HttpStatus.INTERNAL_SERVER_ERROR));
      }),
    );
  }

  private processChunk(chunk: any): any {
    // Implement your chunk processing logic
    return chunk.toString().toUpperCase(); // Example transformation
  }
}
```

2. **Apply the Interceptor to a Route**:

```typescript
import { Controller, Get, UseInterceptors } from '@nestjs/common';
import { ConditionalStreamInterceptor } from './conditional-stream.interceptor';

@Controller('data')
export class DataController {
  @Get()
  @UseInterceptors(ConditionalStreamInterceptor)
  getData() {
    return 'Processing data from external API...';
  }
}
```

### Explanation

- **`Content-Length` Check**: The interceptor first makes a `HEAD` request to the external API to retrieve the `Content-Length` header, which indicates the size of the data.
- **Conditional Processing**: If the content length exceeds the defined threshold (`CHUNK_THRESHOLD`), the interceptor switches to chunk processing by streaming the data. Otherwise, it uses the default processing.
- **Error Handling**: The interceptor includes error handling to manage any potential errors during the data retrieval and processing.

This approach ensures that you handle large datasets efficiently while using default processing for smaller datasets.

If you have any specific requirements or further questions, feel free to share!

=================

Even though the data is processed in chunks, I feel we may have to combine the data together when rendering it to the client. How is this done

When processing data in chunks, you can accumulate the chunks and combine them into a single response before sending it to the client. This approach ensures that the client receives a complete and cohesive response. Here's how you can achieve this in NestJS using an interceptor:

### Steps to Combine Data Chunks

1. **Accumulate Chunks**: Collect chunks of data as they are processed.
2. **Combine Chunks**: Combine the accumulated chunks into a single response.
3. **Send Combined Response**: Send the combined response to the client.

### Implementation

1. **Create the Interceptor**:

```typescript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler, HttpException, HttpStatus } from '@nestjs/common';
import { Observable, from, throwError } from 'rxjs';
import { switchMap, map, catchError, reduce } from 'rxjs/operators';
import * as axios from 'axios';

@Injectable()
export class CombineChunksInterceptor implements NestInterceptor {
  private readonly CHUNK_THRESHOLD = 1000000; // 1 MB (adjust based on your requirements)

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const externalApiUrl = 'https://api.example.com/large-data';

    return from(axios.default.head(externalApiUrl)).pipe(
      switchMap(response => {
        const contentLength = response.headers['content-length'];
        
        if (contentLength && contentLength > this.CHUNK_THRESHOLD) {
          return from(axios.default.get(externalApiUrl, { responseType: 'stream' })).pipe(
            switchMap(response => {
              const dataStream = response.data;
              return dataStream.pipe(
                // Accumulate chunks
                reduce((acc, chunk) => acc + chunk.toString(), ''),
                // Apply final transformation if needed
                map(combinedData => this.finalTransform(combinedData)),
              );
            }),
          );
        } else {
          return from(axios.default.get(externalApiUrl)).pipe(
            switchMap(response => next.handle().pipe(
              // Default processing
              map(() => response.data),
            )),
          );
        }
      }),
      catchError(err => {
        console.error('Error occurred:', err);
        return throwError(new HttpException('Failed to process data', HttpStatus.INTERNAL_SERVER_ERROR));
      }),
    );
  }

  private finalTransform(data: string): any {
    // Implement your final transformation logic
    return { transformedData: data };
  }
}
```

2. **Apply the Interceptor to a Route**:

```typescript
import { Controller, Get, UseInterceptors } from '@nestjs/common';
import { CombineChunksInterceptor } from './combine-chunks.interceptor';

@Controller('data')
export class DataController {
  @Get()
  @UseInterceptors(CombineChunksInterceptor)
  getData() {
    return 'Processing data from external API...';
  }
}
```

### Explanation

- **Accumulate Chunks**: The `reduce` operator is used to accumulate chunks of data as they are processed. This operator combines the chunks into a single string.
- **Combine Chunks**: Once all chunks are accumulated, the `map` operator applies a final transformation if needed.
- **Send Combined Response**: The combined data is sent as the final response to the client.

This approach ensures that the client receives a complete and cohesive response, even when the data is processed in chunks.

If you have any specific use cases or further questions, feel free to share!

