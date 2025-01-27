@startuml
actor FrontEndApp
participant OrderController
participant CommandDispatcher
participant CreateOrderHandler
participant EventBus
participant SerializationConcern
participant WebSocketNotificationListener
participant WebSocketServer
participant LoggingListener
participant LoggingService
participant ValidationConcern
participant SerializationConcern

FrontEndApp -> OrderController: CreateOrderCommand
OrderController -> CommandDispatcher: dispatch(command)
CommandDispatcher -> ValidationConcern: validate(command)
ValidationConcern --> CommandDispatcher: validation result
CommandDispatcher -> SerializationConcern: serialize(command)
SerializationConcern --> CommandDispatcher: serialized command
CommandDispatcher -> CreateOrderHandler: handle(command) <<async>>
CreateOrderHandler -> CreateOrderHandler: Process command
CreateOrderHandler -> EventBus: publish(event)
EventBus -> SerializationConcern: serialize(event)
SerializationConcern --> EventBus: serialized event
EventBus -> WebSocketNotificationListener: onEvent(event)
WebSocketNotificationListener -> WebSocketServer: sendWebSocketMessage(userId, message)
WebSocketServer -> FrontEndApp: WebSocket notification
EventBus -> LoggingListener: onEvent(event)
@enduml
