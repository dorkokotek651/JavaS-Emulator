package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "S-Instruction-Argument")
public class SInstructionArgumentXml {
    
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;
    
    @JacksonXmlProperty(isAttribute = true, localName = "value")
    private String value;
    
    public SInstructionArgumentXml() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
