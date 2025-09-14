package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "S-Program")
public class SProgramXml {
    
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;
    
    @JacksonXmlProperty(localName = "S-Instructions")
    private SInstructionsXml sInstructions;
    
    @JacksonXmlProperty(localName = "S-Functions")
    private SFunctionsXml sFunctions;
    
    public SProgramXml() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public SInstructionsXml getSInstructions() {
        return sInstructions;
    }
    
    public void setSInstructions(SInstructionsXml sInstructions) {
        this.sInstructions = sInstructions;
    }
    
    public SFunctionsXml getSFunctions() {
        return sFunctions;
    }
    
    public void setSFunctions(SFunctionsXml sFunctions) {
        this.sFunctions = sFunctions;
    }
}
