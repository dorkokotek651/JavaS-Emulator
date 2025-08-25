package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "S-Instruction-Arguments")
public class SInstructionArgumentsXml {
    
    @JacksonXmlProperty(localName = "S-Instruction-Argument")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<SInstructionArgumentXml> sInstructionArgument;
    
    public SInstructionArgumentsXml() {
    }
    
    public List<SInstructionArgumentXml> getSInstructionArgument() {
        return sInstructionArgument;
    }
    
    public void setSInstructionArgument(List<SInstructionArgumentXml> sInstructionArgument) {
        this.sInstructionArgument = sInstructionArgument;
    }
}
