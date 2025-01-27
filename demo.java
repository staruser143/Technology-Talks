public class CreateUserCommand {
    private final String userId;
    private final String username;

    public CreateUserCommand(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Getters
}

public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand> {
    private final EventBus eventBus;

    public CreateUserCommandHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void handle(CreateUserCommand command) {
        // Business logic for creating a user
        // For example:
        // User user = new User(command.getUserId(), command.getUsername());
        // userRepository.save(user);

        // Publish UserCreatedEvent
        UserCreatedEvent event = new UserCreatedEvent(command.getUserId(), command.getUsername());
        eventBus.publish(event);
    }
}
import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();

    public <T> void registerHandler(Class<T> type, CommandHandler<T> handler) {
        handlers.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void dispatch(T command) {
        CommandHandler<T> handler = (CommandHandler<T>) handlers.get(command.getClass());
        if (handler != null) {
            handler.handle(command);
        } else {
            throw new IllegalStateException("No handler found for command: " + command.getClass());
        }
    }
}
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<Object> subscribers = new ArrayList<>();

    public void register(Object subscriber) {
        subscribers.add(subscriber);
    }

    public void publish(Object event) {
        for (Object subscriber : subscribers) {
            // Use reflection or a library like Guava EventBus to call the appropriate method on the subscriber
        }
    }
}

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public CommandDispatcher commandDispatcher(EventBus eventBus) {
        CommandDispatcher dispatcher = new CommandDispatcher();
        dispatcher.registerHandler(CreateUserCommand.class, new CreateUserCommandHandler(eventBus));
        return dispatcher;
    }
}
public interface Validator<T> {
    void validate(T command);
}

public class CreateUserCommandValidator implements Validator<CreateUserCommand> {
    @Override
    public void validate(CreateUserCommand command) {
        if (command.getUsername() == null || command.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        // Add more validation rules as needed
    }
}
public interface CommandInterceptor<T> {
    void intercept(T command);
}

public class LoggingInterceptor<T> implements CommandInterceptor<T> {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public void intercept(T command) {
        logger.info("Executing command: {}", command);
    }
}

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.commandhandler.*.*(..))")
    public void logCommandExecution(JoinPoint joinPoint) {
        Object command = joinPoint.getArgs()[0];
        logger.info("Executing command: {}", command);
    }
}


import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String serialize(Object command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize command", e);
        }
    }
}

import java.util.List;

public class CommandHandlerPipeline<T> {
    private final List<Validator<T>> validators;
    private final List<CommandInterceptor<T>> interceptors;
    private final CommandHandler<T> handler;
    private final CommandSerializer serializer;

    public CommandHandlerPipeline(List<Validator<T>> validators, 
                                  List<CommandInterceptor<T>> interceptors, 
                                  CommandHandler<T> handler, 
                                  CommandSerializer serializer) {
        this.validators = validators;
        this.interceptors = interceptors;
        this.handler = handler;
        this.serializer = serializer;
    }

    public void handle(T command) {
        // Validation
        for (Validator<T> validator : validators) {
            validator.validate(command);
        }
        
        // Logging
        for (CommandInterceptor<T> interceptor : interceptors) {
            interceptor.intercept(command);
        }

        // Serialization
        String serializedCommand = serializer.serialize(command);
        System.out.println("Serialized command: " + serializedCommand);

        // Command Handling
        handler.handle(command);
    }
}

@Configuration
public class AppConfig {

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public CommandDispatcher commandDispatcher(EventBus eventBus) {
        CommandDispatcher dispatcher = new CommandDispatcher();
        
        List<Validator<CreateUserCommand>> validators = List.of(new CreateUserCommandValidator());
        List<CommandInterceptor<CreateUserCommand>> interceptors = List.of(new LoggingInterceptor<>());
        CommandSerializer serializer = new CommandSerializer();
        
        CreateUserCommandHandler commandHandler = new CreateUserCommandHandler(eventBus);
        CommandHandlerPipeline<CreateUserCommand> pipeline = 
            new CommandHandlerPipeline<>(validators, interceptors, commandHandler, serializer);
        
        dispatcher.registerHandler(CreateUserCommand.class, pipeline);
        return dispatcher;
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CommandDispatcher commandDispatcher;

    @Autowired
    public UserController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @PostMapping
    public void createUser(@RequestBody CreateUserCommand command) {
        commandDispatcher.dispatch(command);
    }
}
