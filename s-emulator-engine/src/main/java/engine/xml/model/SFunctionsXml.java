package engine.xml.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public class SFunctionsXml {
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "S-Function")
    private List<SFunctionXml> sFunctions;
    
    public SFunctionsXml() {
    }
    
    public List<SFunctionXml> getSFunctions() {
        return sFunctions;
    }
    
    public void setSFunctions(List<SFunctionXml> sFunctions) {
        this.sFunctions = sFunctions;
    }
}
