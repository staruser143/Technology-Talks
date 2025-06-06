public class MyChangeFeedHandler implements ChangeFeedProcessorItemCallback<JsonNode> {

    @Override
    public void processChanges(ChangeFeedProcessorContext context, List<JsonNode> docs) {
        for (JsonNode doc : docs) {
            // 👇 YOUR CHANGE HANDLING LOGIC
            System.out.println("Detected change: " + doc.toString());
        }
    }
}