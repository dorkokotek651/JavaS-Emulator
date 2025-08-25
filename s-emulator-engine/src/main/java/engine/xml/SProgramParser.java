package engine.xml;

import engine.api.SProgram;
import engine.api.SInstruction;
import engine.exception.XMLValidationException;
import engine.model.SProgramImpl;
import engine.model.instruction.InstructionFactory;
import engine.xml.model.SInstructionArgumentXml;
import engine.xml.model.SProgramXml;
import engine.xml.model.SInstructionXml;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SProgramParser {
    private static final Pattern X_VARIABLE_PATTERN = Pattern.compile("^x\\d+$");
    private static final Pattern Z_VARIABLE_PATTERN = Pattern.compile("^z\\d+$");
    private static final Pattern Y_VARIABLE_PATTERN = Pattern.compile("^y$");
    private static final Pattern LABEL_PATTERN = Pattern.compile("^L\\d+$|^EXIT$");

    private final XMLValidator xmlValidator;
    private final XmlMapper xmlMapper;

    public SProgramParser() throws XMLValidationException {
        this.xmlValidator = new XMLValidator();
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SProgram parseXMLFile(String filePath) throws XMLValidationException {
        xmlValidator.validateXMLFile(filePath);

        try {
            SProgramXml xmlProgram = 
                xmlMapper.readValue(new File(filePath), SProgramXml.class);

            return convertToSProgram(xmlProgram);
        } catch (Exception e) {
            throw new XMLValidationException("Failed to parse XML file '" + filePath + "': " + e.getMessage(), e);
        }
    }

    private SProgram convertToSProgram(SProgramXml xmlProgram) throws XMLValidationException {
        if (xmlProgram.getName() == null || xmlProgram.getName().trim().isEmpty()) {
            throw new XMLValidationException("Program name cannot be null or empty");
        }

        if (xmlProgram.getSInstructions() == null || 
            xmlProgram.getSInstructions().getSInstruction() == null ||
            xmlProgram.getSInstructions().getSInstruction().isEmpty()) {
            throw new XMLValidationException("Program must contain at least one instruction");
        }

        SProgramImpl program = new SProgramImpl(xmlProgram.getName().trim());
        
        Set<String> definedLabels = new HashSet<>();
        Set<String> referencedLabels = new HashSet<>();

        for (SInstructionXml xmlInstruction : xmlProgram.getSInstructions().getSInstruction()) {
            validateInstruction(xmlInstruction);
            
            if (xmlInstruction.getSLabel() != null && !xmlInstruction.getSLabel().trim().isEmpty()) {
                String label = xmlInstruction.getSLabel().trim();
                if (definedLabels.contains(label)) {
                    throw new XMLValidationException("Duplicate label found: " + label);
                }
                definedLabels.add(label);
            }

            collectReferencedLabels(xmlInstruction, referencedLabels);
            
            engine.api.SInstruction instruction = convertXmlInstructionToSInstruction(xmlInstruction);
            program.addInstruction(instruction);
        }

        validateLabelReferences(definedLabels, referencedLabels);
        program.validate();

        return program;
    }

    private void validateInstruction(SInstructionXml xmlInstruction) throws XMLValidationException {
        if (xmlInstruction.getName() == null || xmlInstruction.getName().trim().isEmpty()) {
            throw new XMLValidationException("Instruction name cannot be null or empty");
        }

        if (xmlInstruction.getType() == null || xmlInstruction.getType().trim().isEmpty()) {
            throw new XMLValidationException("Instruction type cannot be null or empty");
        }

        String type = xmlInstruction.getType().trim();
        if (!type.equals("basic") && !type.equals("synthetic")) {
            throw new XMLValidationException("Invalid instruction type: " + type + ". Must be 'basic' or 'synthetic'");
        }

        if (xmlInstruction.getSVariable() == null || xmlInstruction.getSVariable().trim().isEmpty()) {
            throw new XMLValidationException("Instruction variable cannot be null or empty");
        }

        String variable = xmlInstruction.getSVariable().trim();
        if (!isValidVariableName(variable)) {
            throw new XMLValidationException("Invalid variable name: " + variable + 
                ". Must be 'y', 'x' followed by digits, or 'z' followed by digits");
        }

        if (xmlInstruction.getSLabel() != null && !xmlInstruction.getSLabel().trim().isEmpty()) {
            String label = xmlInstruction.getSLabel().trim();
            if (!LABEL_PATTERN.matcher(label).matches()) {
                throw new XMLValidationException("Invalid label format: " + label + 
                    ". Must be 'L' followed by digits or 'EXIT'");
            }
        }

        validateInstructionArguments(xmlInstruction);
    }

    private boolean isValidVariableName(String variable) {
        return Y_VARIABLE_PATTERN.matcher(variable).matches() ||
               X_VARIABLE_PATTERN.matcher(variable).matches() ||
               Z_VARIABLE_PATTERN.matcher(variable).matches();
    }

    private void validateInstructionArguments(SInstructionXml xmlInstruction) throws XMLValidationException {
        String instructionName = xmlInstruction.getName().trim();
        
        if (xmlInstruction.getSInstructionArguments() != null) {
            Map<String, String> arguments = new HashMap<>();
            
            for (SInstructionArgumentXml arg : xmlInstruction.getSInstructionArguments().getSInstructionArgument()) {
                if (arg.getName() == null || arg.getName().trim().isEmpty()) {
                    throw new XMLValidationException("Argument name cannot be null or empty in instruction: " + instructionName);
                }
                if (arg.getValue() == null || arg.getValue().trim().isEmpty()) {
                    throw new XMLValidationException("Argument value cannot be null or empty for argument '" + 
                        arg.getName() + "' in instruction: " + instructionName);
                }
                arguments.put(arg.getName().trim(), arg.getValue().trim());
            }

            validateArgumentsForInstructionType(instructionName, arguments);
        }
    }

    private void validateArgumentsForInstructionType(String instructionName, Map<String, String> arguments) throws XMLValidationException {
        switch (instructionName) {
            case "JUMP_NOT_ZERO":
                if (!arguments.containsKey("JNZLabel")) {
                    throw new XMLValidationException("JUMP_NOT_ZERO instruction requires 'JNZLabel' argument");
                }
                break;
            case "ASSIGNMENT":
                if (!arguments.containsKey("assignedVariable")) {
                    throw new XMLValidationException("ASSIGNMENT instruction requires 'assignedVariable' argument");
                }
                break;
            case "CONSTANT_ASSIGNMENT":
                if (!arguments.containsKey("constantValue")) {
                    throw new XMLValidationException("CONSTANT_ASSIGNMENT instruction requires 'constantValue' argument");
                }
                break;
            case "GOTO_LABEL":
                if (!arguments.containsKey("gotoLabel")) {
                    throw new XMLValidationException("GOTO_LABEL instruction requires 'gotoLabel' argument");
                }
                break;
            case "JUMP_ZERO":
                if (!arguments.containsKey("JZLabel")) {
                    throw new XMLValidationException("JUMP_ZERO instruction requires 'JZLabel' argument");
                }
                break;
            case "JUMP_EQUAL_CONSTANT":
                if (!arguments.containsKey("JEConstantLabel") || !arguments.containsKey("constantValue")) {
                    throw new XMLValidationException("JUMP_EQUAL_CONSTANT instruction requires 'JEConstantLabel' and 'constantValue' arguments");
                }
                break;
            case "JUMP_EQUAL_VARIABLE":
                if (!arguments.containsKey("JEVariableLabel") || !arguments.containsKey("variableName")) {
                    throw new XMLValidationException("JUMP_EQUAL_VARIABLE instruction requires 'JEVariableLabel' and 'variableName' arguments");
                }
                break;
        }
    }

    private void collectReferencedLabels(SInstructionXml xmlInstruction, Set<String> referencedLabels) {
        if (xmlInstruction.getSInstructionArguments() != null) {
            for (SInstructionArgumentXml arg : xmlInstruction.getSInstructionArguments().getSInstructionArgument()) {
                String argName = arg.getName().trim();
                String argValue = arg.getValue().trim();
                
                if (argName.contains("Label")) {
                    referencedLabels.add(argValue);
                }
            }
        }
    }

    private void validateLabelReferences(Set<String> definedLabels, Set<String> referencedLabels) throws XMLValidationException {
        for (String referencedLabel : referencedLabels) {
            if (!LABEL_PATTERN.matcher(referencedLabel).matches()) {
                throw new XMLValidationException("Invalid label format: '" + referencedLabel + 
                    "'. Must be 'L' followed by digits or 'EXIT'");
            }
            
            if (!definedLabels.contains(referencedLabel) && !referencedLabel.equals("EXIT")) {
                throw new XMLValidationException("Referenced label '" + referencedLabel + "' is not defined in the program");
            }
        }
    }

    private SInstruction convertXmlInstructionToSInstruction(SInstructionXml xmlInstruction) throws XMLValidationException {
        String name = xmlInstruction.getName().trim();
        String variable = xmlInstruction.getSVariable().trim();
        String label = (xmlInstruction.getSLabel() != null && !xmlInstruction.getSLabel().trim().isEmpty()) 
            ? xmlInstruction.getSLabel().trim() : null;
        
        Map<String, String> arguments = new HashMap<>();
        if (xmlInstruction.getSInstructionArguments() != null) {
            for (SInstructionArgumentXml arg : xmlInstruction.getSInstructionArguments().getSInstructionArgument()) {
                arguments.put(arg.getName().trim(), arg.getValue().trim());
            }
        }

        try {
            return InstructionFactory.createInstruction(name, variable, label, arguments);
        } catch (IllegalArgumentException e) {
            throw new XMLValidationException("Failed to create instruction '" + name + "': " + e.getMessage(), e);
        }
    }
}
