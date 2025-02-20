@Component
public class CommandRouter {
    private final List<HandlerConfig> handlerConfigs;

    public CommandRouter(HandlerConfigLoader configLoader) {
        this.handlerConfigs = configLoader.getHandlers();
    }

    public Optional<String> findMatchingHandler(Map<String, Object> requestAttributes) {
        return handlerConfigs.stream()
            .filter(config -> matches(config, requestAttributes))
            .map(HandlerConfig::getName)
            .findFirst();
    }

    private boolean matches(HandlerConfig config, Map<String, Object> requestAttributes) {
        return config.getConditions().entrySet().stream()
            .allMatch(entry -> 
                requestAttributes.containsKey(entry.getKey()) && 
                requestAttributes.get(entry.getKey()).equals(entry.getValue())
            );
    }
}