package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * XML model class for S-Function elements in V2 schema.
 */
public class SFunctionXml {
    
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;
    
    @JacksonXmlProperty(isAttribute = true, localName = "user-string")
    private String userString;
    
    @JacksonXmlProperty(localName = "S-Instructions")
    private SInstructionsXml sInstructions;
    
    public SFunctionXml() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUserString() {
        return userString;
    }
    
    public void setUserString(String userString) {
        this.userString = userString;
    }
    
    public SInstructionsXml getSInstructions() {
        return sInstructions;
    }
    
    public void setSInstructions(SInstructionsXml sInstructions) {
        this.sInstructions = sInstructions;
    }
}
