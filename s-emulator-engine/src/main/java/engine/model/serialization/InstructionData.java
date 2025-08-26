package engine.model.serialization;

import java.util.Map;

public class InstructionData {
    private String type;
    private String label;
    private Map<String, Object> properties;
    
    public InstructionData() {
    }
    
    public InstructionData(String type, String label, Map<String, Object> properties) {
        this.type = type;
        this.label = label;
        this.properties = properties;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
