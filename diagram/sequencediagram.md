@startuml
actor FrontEndApp
participant OrderController
participant CommandDispatcher
participant CreateOrderHandler
participant EventBus
participant SerializationConcern
participant WebSocketNotificationListener
participant EmbeddedWebSocketServer
participant LoggingListener
participant LoggingService
participant ValidationConcern
participant SerializationConcern

FrontEndApp -> OrderController: CreateOrderCommand
OrderController -> CommandDispatcher: dispatch(command)
CommandDispatcher -> ValidationConcern: validate(command)
ValidationConcern --> CommandDispatcher: validation result
CommandDispatcher -> CreateOrderHandler: handle(command) <<async>>
CreateOrderHandler -> CreateOrderHandler: Process command
CreateOrderHandler -> EventBus: publish(event)
EventBus -> SerializationConcern: serialize(event)
SerializationConcern --> EventBus: serialized event
EventBus -> WebSocketNotificationListener: onEvent(event)
WebSocketNotificationListener -> EmbeddedWebSocketServer: sendWebSocketMessage(userId, message)
EmbeddedWebSocketServer -> FrontEndApp: WebSocket notification
EventBus -> LoggingListener: onEvent(event)
@enduml
