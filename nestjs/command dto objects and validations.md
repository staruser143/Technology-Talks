# Using Command Objects with CQRS in NestJS

When using CQRS (Command Query Responsibility Segregation) in a NestJS application, creating command objects based on incoming requests is a common pattern. This involves mapping the data from the request to a command object which will be processed by a command handler. Here’s how we can efficiently create command objects from requests in NestJS:

### Steps to Create Command Objects in NestJS

1. **Define the Command Object**: Create a class that represents the command.
2. **Create a DTO (Data Transfer Object)**: Define a DTO to validate and transform incoming request data.
3. **Use a Controller to Handle Requests**: Map incoming requests to command objects in the controller.
4. **Inject the Command Handler**: Use the command handler to process the command.

### Example Implementation

#### 1. Define the Command Object

```typescript
// CreateAccountCommand.ts
export class CreateAccountCommand {
  constructor(
    public readonly accountId: string,
    public readonly ownerName: string,
    public readonly initialBalance: number
  ) {}
}
```

#### 2. Create a DTO

```typescript
// CreateAccountDto.ts
import { IsString, IsNumber } from 'class-validator';

export class CreateAccountDto {
  @IsString()
  accountId: string;

  @IsString()
  ownerName: string;

  @IsNumber()
  initialBalance: number;
}
```

#### 3. Use a Controller to Handle Requests

```typescript
// AccountController.ts
import { Controller, Post, Body } from '@nestjs/common';
import { CreateAccountDto } from './CreateAccountDto';
import { CreateAccountCommand } from './CreateAccountCommand';
import { CommandBus } from '@nestjs/cqrs';

@Controller('accounts')
export class AccountController {
  constructor(private readonly commandBus: CommandBus) {}

  @Post()
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    const { accountId, ownerName, initialBalance } = createAccountDto;
    const command = new CreateAccountCommand(accountId, ownerName, initialBalance);
    return await this.commandBus.execute(command);
  }
}
```

#### 4. Implement the Command Handler

```typescript
// CreateAccountHandler.ts
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { CreateAccountCommand } from './CreateAccountCommand';

@CommandHandler(CreateAccountCommand)
export class CreateAccountHandler implements ICommandHandler<CreateAccountCommand> {
  async execute(command: CreateAccountCommand) {
    const { accountId, ownerName, initialBalance } = command;
    // Business logic to create an account
    console.log(`Account created: ${accountId}, Owner: ${ownerName}, Balance: ${initialBalance}`);
    return { success: true };
  }
}
```

### Explanation

1. **Command Object**: The `CreateAccountCommand` class represents the command to create a new account. It encapsulates the necessary data.
2. **DTO**: The `CreateAccountDto` class is used to validate and map incoming request data. Using the `class-validator` package, you can ensure the data conforms to expected types.
3. **Controller**: The `AccountController` handles HTTP POST requests. It maps the incoming request data to a `CreateAccountCommand` object and sends it to the command bus using `commandBus.execute()`.
4. **Command Handler**: The `CreateAccountHandler` class implements the ICommandHandler interface for the `CreateAccountCommand`. It contains the business logic for handling the command.

### Benefits of This Approach

- **Validation**: Using DTOs ensures that incoming request data is validated before creating command objects.
- **Separation of Concerns**: Controllers handle request mapping, while command handlers manage business logic.
- **Scalability**: The command bus pattern allows you to easily add more commands and handlers.

This setup provides a clean and maintainable way to create and handle command objects in a NestJS application using CQRS.

# DTOs vs Command Object. Can we use just one?

While it is technically possible to use a single class for both DTOs (Data Transfer Objects) and command objects, it is generally recommended to keep them separate. Here are the reasons why separating DTOs and command objects is beneficial, along with an example to illustrate the concept:

### Reasons to Separate DTOs and Command Objects

1. **Separation of Concerns**: 
    - **DTOs**: Focus on validating and transferring data from the client to the application.
    - **Command Objects**: Represent specific actions or intentions in the application, encapsulating business logic and behavior.

2. **Validation Flexibility**: 
    - DTOs often contain validation rules specific to the data transfer process, which might not be relevant to the command's business logic.

3. **Decoupling**: 
    - Keeping DTOs and command objects separate allows you to change one without affecting the other. This decoupling makes the system more maintainable and flexible.

4. **Readability and Maintenance**: 
    - Clear separation makes the codebase easier to understand and maintain, as each class has a single responsibility.

### Example

Here’s an example to illustrate the separation of DTOs and command objects in a NestJS application.

#### 1. Define the DTO

```typescript
// CreateAccountDto.ts
import { IsString, IsNumber } from 'class-validator';

export class CreateAccountDto {
  @IsString()
  accountId: string;

  @IsString()
  ownerName: string;

  @IsNumber()
  initialBalance: number;
}
```

#### 2. Define the Command Object

```typescript
// CreateAccountCommand.ts
export class CreateAccountCommand {
  constructor(
    public readonly accountId: string,
    public readonly ownerName: string,
    public readonly initialBalance: number
  ) {}
}
```

#### 3. Use a Controller to Handle Requests

```typescript
// AccountController.ts
import { Controller, Post, Body } from '@nestjs/common';
import { CreateAccountDto } from './CreateAccountDto';
import { CreateAccountCommand } from './CreateAccountCommand';
import { CommandBus } from '@nestjs/cqrs';

@Controller('accounts')
export class AccountController {
  constructor(private readonly commandBus: CommandBus) {}

  @Post()
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    const { accountId, ownerName, initialBalance } = createAccountDto;
    const command = new CreateAccountCommand(accountId, ownerName, initialBalance);
    return await this.commandBus.execute(command);
  }
}
```

#### 4. Implement the Command Handler

```typescript
// CreateAccountHandler.ts
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { CreateAccountCommand } from './CreateAccountCommand';

@CommandHandler(CreateAccountCommand)
export class CreateAccountHandler implements ICommandHandler<CreateAccountCommand> {
  async execute(command: CreateAccountCommand) {
    const { accountId, ownerName, initialBalance } = command;
    // Business logic to create an account
    console.log(`Account created: ${accountId}, Owner: ${ownerName}, Balance: ${initialBalance}`);
    return { success: true };
  }
}
```

### Summary

While it is possible to use a single class for both DTOs and command objects, separating them provides better separation of concerns, validation flexibility, decoupling, and maintainability. This approach aligns with best practices in software design, ensuring that each class has a single, well-defined responsibility.


# Custom validations or Validations across one or more fields received from request.
In a NestJS application, we can perform custom validations or validations across multiple fields in a DTO (Data Transfer Object) by using the `class-validator` library. This library provides decorators for common validation rules, and it also allows us to create custom validation decorators.

### Steps to Implement Custom Validations in a DTO

1. **Install Dependencies**: Ensure you have the necessary libraries installed.
2. **Define the DTO**: Create a DTO class with validation decorators.
3. **Create Custom Validation Decorators**: Implement custom validation logic.
4. **Use the DTO in a Controller**: Apply the DTO to validate incoming requests.

### Example Implementation

#### 1. Install Dependencies

Make sure we have `class-validator` and `class-transformer` installed:

```bash
npm install class-validator class-transformer
```

#### 2. Define the DTO

Create a DTO class that includes built-in validation decorators and custom validation decorators:

```typescript
// CreateAccountDto.ts
import { IsString, IsNumber, Validate, ValidateIf } from 'class-validator';
import { Match } from './validators/match.decorator';

export class CreateAccountDto {
  @IsString()
  accountId: string;

  @IsString()
  ownerName: string;

  @IsNumber()
  initialBalance: number;

  @IsString()
  password: string;

  @IsString()
  confirmPassword: string;

  @Validate(Match, ['password'])
  @IsString()
  confirmPassword: string;
}
```

#### 3. Create Custom Validation Decorators

Create a custom validation decorator for matching fields, such as ensuring that `password` and `confirmPassword` fields match:

```typescript
// match.decorator.ts
import {
  registerDecorator,
  ValidationOptions,
  ValidatorConstraint,
  ValidatorConstraintInterface,
  ValidationArguments,
} from 'class-validator';

@ValidatorConstraint({ async: false })
export class MatchConstraint implements ValidatorConstraintInterface {
  validate(value: any, args: ValidationArguments) {
    const [relatedPropertyName] = args.constraints;
    const relatedValue = (args.object as any)[relatedPropertyName];
    return value === relatedValue;
  }

  defaultMessage(args: ValidationArguments) {
    const [relatedPropertyName] = args.constraints;
    return `${args.property} must match ${relatedPropertyName}`;
  }
}

export function Match(property: string, validationOptions?: ValidationOptions) {
  return (object: Object, propertyName: string) => {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName,
      options: validationOptions,
      constraints: [property],
      validator: MatchConstraint,
    });
  };
}
```

#### 4. Use the DTO in a Controller

Use the DTO in a controller to automatically validate incoming requests:

```typescript
// AccountController.ts
import { Controller, Post, Body } from '@nestjs/common';
import { CreateAccountDto } from './CreateAccountDto';

@Controller('accounts')
export class AccountController {
  @Post()
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully' };
  }
}
```

### Explanation

1. **DTO Class**: The `CreateAccountDto` class uses built-in validation decorators like `@IsString` and `@IsNumber`. It also includes a custom decorator `@Match` for validating that `password` and `confirmPassword` fields match.
2. **Custom Validation Decorator**: The `Match` function is a custom decorator that uses the `class-validator` library to register a custom validation constraint. The `MatchConstraint` class defines the validation logic.
3. **Controller**: The `AccountController` uses the `CreateAccountDto` to ensure that incoming requests are validated. If the validation fails, an appropriate error response is sent.

This approach allows you to perform complex validations, including cross-field validations, in a clean and maintainable way.

# Using Pipes for Transforming and Validating Data
In NestJS, pipes are a powerful feature used for transforming and validating data. They can be applied at various levels (method, controller, or globally) to handle incoming data before it reaches the request handler. Here's an overview of when and how to use pipes for validation:

### When to Use Pipes for Validation

1. **Validation of Incoming Requests**:
   - Use pipes to validate incoming request data, ensuring it meets specific constraints before processing.
   - Ideal for scenarios where you need to enforce data integrity and prevent invalid data from entering your application.

2. **Data Transformation**:
   - Pipes can also transform data, converting it to the required format. For example, converting strings to numbers or trimming whitespace.

3. **Reusable Validation Logic**:
   - Pipes can encapsulate validation logic that can be reused across different parts of your application.

### How to Use Pipes for Validation

1. **Built-in Validation Pipe**:
   - NestJS provides a built-in `ValidationPipe` that integrates with the `class-validator` library to perform validation.

2. **Custom Pipes**:
   - We can create custom pipes to handle specific validation scenarios or complex validation logic.

### Example Implementation

#### 1. Install Dependencies

Ensure we have the necessary libraries installed:

```bash
npm install class-validator class-transformer
```

#### 2. Define the DTO

Create a DTO class with validation decorators:

```typescript
// create-account.dto.ts
import { IsString, IsNumber, MinLength, IsPositive } from 'class-validator';

export class CreateAccountDto {
  @IsString()
  @MinLength(3)
  accountId: string;

  @IsString()
  @MinLength(3)
  ownerName: string;

  @IsNumber()
  @IsPositive()
  initialBalance: number;
}
```

#### 3. Use the Validation Pipe in a Controller

Apply the `ValidationPipe` to validate incoming requests:

```typescript
// account.controller.ts
import { Controller, Post, Body, UsePipes, ValidationPipe } from '@nestjs/common';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
export class AccountController {
  @Post()
  @UsePipes(new ValidationPipe())
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

### Explanation

1. **DTO Class**:
   - The `CreateAccountDto` class uses validation decorators from the `class-validator` library to enforce constraints on the fields.

2. **Validation Pipe**:
   - The `ValidationPipe` automatically validates incoming request data against the DTO's validation rules.
   - If validation fails, the pipe throws a detailed validation error response.

3. **Controller**:
   - The `AccountController` uses the `ValidationPipe` to validate the request body against the `CreateAccountDto` class.
   - If the request data is valid, the `createAccount` method processes the request.

### Custom Validation Pipe Example

We can also create custom validation pipes for more specific validation logic:

```typescript
// custom-validation.pipe.ts
import { PipeTransform, Injectable, ArgumentMetadata, BadRequestException } from '@nestjs/common';

@Injectable()
export class CustomValidationPipe implements PipeTransform {
  transform(value: any, metadata: ArgumentMetadata) {
    if (!value || typeof value !== 'object') {
      throw new BadRequestException('Invalid payload');
    }

    // Custom validation logic
    if (value.initialBalance < 100) {
      throw new BadRequestException('Initial balance must be at least 100');
    }

    return value;
  }
}
```

#### 4. Use the Custom Validation Pipe in a Controller

Apply the custom validation pipe to validate incoming requests:

```typescript
// account.controller.ts
import { Controller, Post, Body, UsePipes } from '@nestjs/common';
import { CustomValidationPipe } from './custom-validation.pipe';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
export class AccountController {
  @Post()
  @UsePipes(CustomValidationPipe)
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

### Summary

Using pipes for validation in NestJS is a powerful way to ensure data integrity and enforce validation rules before processing requests. The built-in `ValidationPipe` is convenient for standard validation scenarios, while custom pipes offer flexibility for more complex validation logic. By leveraging these features, you can maintain a clean and maintainable codebase with robust validation.

# Is it mandatory to use @UsePipes with ValidationPipe?

Actually, the usage of `@UsePipes` with `ValidationPipe` is essential when we want NestJS to automatically validate the DTOs using the decorators from `class-validator`. Here's why:

1. **Automatic Validation**: By using `@UsePipes(new ValidationPipe())`, we enable automatic validation of incoming requests based on the decorators in your DTO. Without this, the validation decorators on the DTO fields won't be executed.

2. **Centralized Validation Logic**: `ValidationPipe` ensures that all incoming data is validated according to the DTO's rules, providing a centralized and consistent way to handle validation.

3. **Error Handling**: `ValidationPipe` also handles validation errors, sending appropriate responses back to the client when the data doesn't meet the defined constraints.

### Example with `ValidationPipe`

Here’s an example of how to use `ValidationPipe` to validate a DTO:

#### 1. DTO with Validation Decorators

```typescript
// create-account.dto.ts
import { IsString, IsNumber, MinLength, IsPositive } from 'class-validator';

export class CreateAccountDto {
  @IsString()
  @MinLength(3)
  accountId: string;

  @IsString()
  @MinLength(3)
  ownerName: string;

  @IsNumber()
  @IsPositive()
  initialBalance: number;
}
```

#### 2. Controller with `ValidationPipe`

```typescript
// account.controller.ts
import { Controller, Post, Body, UsePipes, ValidationPipe } from '@nestjs/common';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
export class AccountController {
  @Post()
  @UsePipes(new ValidationPipe())
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

### Custom Validation Pipe

For custom validation logic, you would use a custom validation pipe as shown in the previous examples. Here’s a brief recap:

#### Custom Validation Pipe Example

```typescript
// custom-validation.pipe.ts
import { PipeTransform, Injectable, ArgumentMetadata, BadRequestException } from '@nestjs/common';

@Injectable()
export class CustomValidationPipe implements PipeTransform {
  transform(value: any, metadata: ArgumentMetadata) {
    if (!value || typeof value !== 'object') {
      throw new BadRequestException('Invalid payload');
    }

    // Custom validation logic
    if (value.initialBalance < 100) {
      throw new BadRequestException('Initial balance must be at least 100');
    }

    return value;
  }
}
```

#### Using Custom Validation Pipe in a Controller

```typescript
// account.controller.ts
import { Controller, Post, Body, UsePipes } from '@nestjs/common';
import { CustomValidationPipe } from './custom-validation.pipe';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
export class AccountController {
  @Post()
  @UsePipes(CustomValidationPipe)
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

### Summary

- **ValidationPipe with @UsePipes**: Necessary for automatic validation of DTOs using `class-validator` decorators.
- **Custom Validation Pipe**: Useful for implementing specific validation logic that goes beyond what `class-validator` provides.

Using `@UsePipes(new ValidationPipe())` ensures that all our DTO validations are enforced automatically, maintaining a clean and centralized validation mechanism.

# Applying ValidationPipe @ Global/Controller/Method Level
In NestJS, when we want to validate incoming request data using `class-validator` decorators within DTOs, you do need to use the `ValidationPipe`. This pipe tells NestJS to apply the validation rules defined in your DTOs. While you don't always need to explicitly use `@UsePipes` in your controllers, it's important to ensure that the `ValidationPipe` is applied either globally or at the controller/method level.

### Applying Validation Globally (Recommended)

We can apply the `ValidationPipe` globally in main application file. This ensures that all incoming requests are validated according to the DTOs' rules.

#### main.ts

```typescript
import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.useGlobalPipes(new ValidationPipe()); // Apply ValidationPipe globally
  await app.listen(3000);
}
bootstrap();
```

### Applying Validation at the Controller/Method Level

If we prefer to apply validation at a more granular level, we can use the `@UsePipes` decorator in your controllers or methods.

#### Controller Level

```typescript
import { Controller, Post, Body, UsePipes, ValidationPipe } from '@nestjs/common';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
@UsePipes(new ValidationPipe()) // Apply ValidationPipe at the controller level
export class AccountController {
  @Post()
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

#### Method Level

```typescript
import { Controller, Post, Body, UsePipes, ValidationPipe } from '@nestjs/common';
import { CreateAccountDto } from './create-account.dto';

@Controller('accounts')
export class AccountController {
  @Post()
  @UsePipes(new ValidationPipe()) // Apply ValidationPipe at the method level
  async createAccount(@Body() createAccountDto: CreateAccountDto) {
    // Handle the creation of the account
    return { message: 'Account created successfully', data: createAccountDto };
  }
}
```

### Example DTO with Validation Decorators

```typescript
// create-account.dto.ts
import { IsString, IsNumber, MinLength, IsPositive } from 'class-validator';

export class CreateAccountDto {
  @IsString()
  @MinLength(3)
  accountId: string;

  @IsString()
  @MinLength(3)
  ownerName: string;

  @IsNumber()
  @IsPositive()
  initialBalance: number;
}
```

### Summary

To ensure that your DTO validation decorators are applied, you should use the `ValidationPipe`. This can be set globally in your main application file, or at the controller or method level using the `@UsePipes` decorator. This setup ensures that all incoming requests are validated according to the rules defined in your DTOs.
