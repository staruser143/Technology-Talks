import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlTest {
    public static void main(String[] args) {
        Yaml yaml = new Yaml(new Constructor(FieldMapping.class));
        String testYaml = "collection:\n  source: test";
        
        try {
            FieldMapping mapping = yaml.load(testYaml);
            System.out.println("Success! Source: " + mapping.getCollection().getSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}