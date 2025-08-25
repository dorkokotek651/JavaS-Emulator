package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "S-Instructions")
public class SInstructionsXml {
    
    @JacksonXmlProperty(localName = "S-Instruction")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SInstructionXml> sInstruction;
    
    public SInstructionsXml() {
    }
    
    public List<SInstructionXml> getSInstruction() {
        return sInstruction;
    }
    
    public void setSInstruction(List<SInstructionXml> sInstruction) {
        this.sInstruction = sInstruction;
    }
}
