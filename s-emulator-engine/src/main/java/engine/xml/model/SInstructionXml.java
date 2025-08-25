package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "S-Instruction")
public class SInstructionXml {
    
    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;
    
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;
    
    @JacksonXmlProperty(localName = "S-Variable")
    private String sVariable;
    
    @JacksonXmlProperty(localName = "S-Label")
    private String sLabel;
    
    @JacksonXmlProperty(localName = "S-Instruction-Arguments")
    private SInstructionArgumentsXml sInstructionArguments;
    
    public SInstructionXml() {
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSVariable() {
        return sVariable;
    }
    
    public void setSVariable(String sVariable) {
        this.sVariable = sVariable;
    }
    
    public String getSLabel() {
        return sLabel;
    }
    
    public void setSLabel(String sLabel) {
        this.sLabel = sLabel;
    }
    
    public SInstructionArgumentsXml getSInstructionArguments() {
        return sInstructionArguments;
    }
    
    public void setSInstructionArguments(SInstructionArgumentsXml sInstructionArguments) {
        this.sInstructionArguments = sInstructionArguments;
    }
}
