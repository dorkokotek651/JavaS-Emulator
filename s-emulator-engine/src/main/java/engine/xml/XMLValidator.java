package engine.xml;

import engine.exception.XMLValidationException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.InputStream;

public class XMLValidator {
    private static final String XSD_RESOURCE_PATH = "/S-Emulator-v2.xsd";
    private final Schema schema;

    public XMLValidator() throws XMLValidationException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream xsdStream = getClass().getResourceAsStream(XSD_RESOURCE_PATH);
            
            if (xsdStream == null) {
                throw new XMLValidationException("XSD schema file not found in resources: " + XSD_RESOURCE_PATH);
            }
            
            this.schema = factory.newSchema(new StreamSource(xsdStream));
        } catch (Exception e) {
            throw new XMLValidationException("Failed to initialize XML schema validator", e);
        }
    }

    public void validateXMLFile(String filePath) throws XMLValidationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new XMLValidationException("File path cannot be null or empty");
        }

        File xmlFile = new File(filePath);
        
        if (!xmlFile.exists()) {
            throw new XMLValidationException("File does not exist: " + filePath);
        }

        if (!xmlFile.isFile()) {
            throw new XMLValidationException("Path does not point to a file: " + filePath);
        }

        if (!filePath.toLowerCase().endsWith(".xml")) {
            throw new XMLValidationException("File must have .xml extension: " + filePath);
        }

        try {
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (Exception e) {
            throw new XMLValidationException("XML validation failed for file '" + filePath + "': " + e.getMessage(), e);
        }
    }
}
