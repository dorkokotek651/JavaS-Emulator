package engine.xml;

import engine.api.SProgram;
import engine.api.SInstruction;
import engine.exception.XMLValidationException;
import engine.model.FunctionRegistry;
import engine.model.SProgramImpl;
import engine.model.SEmulatorConstants;
import engine.model.instruction.InstructionFactory;
import engine.xml.model.SInstructionArgumentXml;
import engine.xml.model.SProgramXml;
import engine.xml.model.SInstructionXml;
import engine.xml.model.SFunctionXml;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SProgramParser {

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
        
        FunctionRegistry functionRegistry = new FunctionRegistry();
        if (xmlProgram.getSFunctions() != null && xmlProgram.getSFunctions().getSFunctions() != null) {
            for (SFunctionXml xmlFunction : xmlProgram.getSFunctions().getSFunctions()) {
                SProgram functionProgram = convertXmlFunctionToSProgram(xmlFunction);
                functionRegistry.registerFunction(xmlFunction.getName(), xmlFunction.getUserString(), functionProgram);
            }
        }
        
        program.setFunctionRegistry(functionRegistry);
        
        program.validate();

        return program;
    }
    
    private SProgram convertXmlFunctionToSProgram(SFunctionXml xmlFunction) throws XMLValidationException {
        if (xmlFunction.getName() == null || xmlFunction.getName().trim().isEmpty()) {
            throw new XMLValidationException("Function name cannot be null or empty");
        }
        
        if (xmlFunction.getUserString() == null || xmlFunction.getUserString().trim().isEmpty()) {
            throw new XMLValidationException("Function user-string cannot be null or empty");
        }
        
        if (xmlFunction.getSInstructions() == null || 
            xmlFunction.getSInstructions().getSInstruction() == null ||
            xmlFunction.getSInstructions().getSInstruction().isEmpty()) {
            throw new XMLValidationException("Function '" + xmlFunction.getName() + "' must contain at least one instruction");
        }
        
        SProgramImpl functionProgram = new SProgramImpl(xmlFunction.getName().trim());
        
        Set<String> definedLabels = new HashSet<>();
        Set<String> referencedLabels = new HashSet<>();
        
        for (SInstructionXml xmlInstruction : xmlFunction.getSInstructions().getSInstruction()) {
            validateInstruction(xmlInstruction);
            
            if (xmlInstruction.getSLabel() != null && !xmlInstruction.getSLabel().trim().isEmpty()) {
                String label = xmlInstruction.getSLabel().trim();
                if (definedLabels.contains(label)) {
                    throw new XMLValidationException("Duplicate label found in function '" + xmlFunction.getName() + "': " + label);
                }
                definedLabels.add(label);
            }
            
            collectReferencedLabels(xmlInstruction, referencedLabels);
            
            SInstruction instruction = convertXmlInstructionToSInstruction(xmlInstruction);
            functionProgram.addInstruction(instruction);
        }
        
        validateLabelReferences(definedLabels, referencedLabels);
        functionProgram.validate();
        
        return functionProgram;
    }

    private void validateInstruction(SInstructionXml xmlInstruction) throws XMLValidationException {
        if (xmlInstruction.getName() == null || xmlInstruction.getName().trim().isEmpty()) {
            throw new XMLValidationException("Instruction name cannot be null or empty");
        }

        if (xmlInstruction.getType() == null || xmlInstruction.getType().trim().isEmpty()) {
            throw new XMLValidationException("Instruction type cannot be null or empty");
        }

        String type = xmlInstruction.getType().trim();
        if (!type.equals(SEmulatorConstants.INSTRUCTION_TYPE_BASIC) && !type.equals(SEmulatorConstants.INSTRUCTION_TYPE_SYNTHETIC)) {
            throw new XMLValidationException("Invalid instruction type: " + type + ". Must be '" + SEmulatorConstants.INSTRUCTION_TYPE_BASIC + "' or '" + SEmulatorConstants.INSTRUCTION_TYPE_SYNTHETIC + "'");
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
            if (!SEmulatorConstants.LABEL_PATTERN.matcher(label).matches()) {
                throw new XMLValidationException("Invalid label format: " + label + 
                    ". Must be 'L' followed by digits or 'EXIT'");
            }
        }

        validateInstructionArguments(xmlInstruction);
    }

    private boolean isValidVariableName(String variable) {
        return SEmulatorConstants.Y_VARIABLE_PATTERN.matcher(variable).matches() ||
               SEmulatorConstants.X_VARIABLE_PATTERN.matcher(variable).matches() ||
               SEmulatorConstants.Z_VARIABLE_PATTERN.matcher(variable).matches();
    }

    private void validateInstructionArguments(SInstructionXml xmlInstruction) throws XMLValidationException {
        String instructionName = xmlInstruction.getName().trim();
        
        if (xmlInstruction.getSInstructionArguments() != null) {
            Map<String, String> arguments = new HashMap<>();
            
            for (SInstructionArgumentXml arg : xmlInstruction.getSInstructionArguments().getSInstructionArgument()) {
                if (arg.getName() == null || arg.getName().trim().isEmpty()) {
                    throw new XMLValidationException("Argument name cannot be null or empty in instruction: " + instructionName);
                }
                if (arg.getValue() == null) {
                    throw new XMLValidationException("Argument value cannot be null for argument '" + 
                        arg.getName() + "' in instruction: " + instructionName);
                }
                
                if (arg.getValue().trim().isEmpty() && !arg.getName().equals(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG)) {
                    throw new XMLValidationException("Argument value cannot be empty for argument '" + 
                        arg.getName() + "' in instruction: " + instructionName);
                }
                arguments.put(arg.getName().trim(), arg.getValue().trim());
            }

            validateArgumentsForInstructionType(instructionName, arguments);
        }
    }

    private void validateArgumentsForInstructionType(String instructionName, Map<String, String> arguments) throws XMLValidationException {
        switch (instructionName) {
            case SEmulatorConstants.JUMP_NOT_ZERO_NAME:
                if (!arguments.containsKey(SEmulatorConstants.JNZ_LABEL_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.JUMP_NOT_ZERO_NAME + " instruction requires '" + SEmulatorConstants.JNZ_LABEL_ARG + "' argument");
                }
                break;
            default:
                break;
            case SEmulatorConstants.ASSIGNMENT_NAME:
                if (!arguments.containsKey(SEmulatorConstants.ASSIGNED_VARIABLE_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.ASSIGNMENT_NAME + " instruction requires '" + SEmulatorConstants.ASSIGNED_VARIABLE_ARG + "' argument");
                }
                break;
            case SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME:
                if (!arguments.containsKey(SEmulatorConstants.CONSTANT_VALUE_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME + " instruction requires '" + SEmulatorConstants.CONSTANT_VALUE_ARG + "' argument");
                }
                break;
            case SEmulatorConstants.GOTO_LABEL_NAME:
                if (!arguments.containsKey(SEmulatorConstants.GOTO_LABEL_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.GOTO_LABEL_NAME + " instruction requires '" + SEmulatorConstants.GOTO_LABEL_ARG + "' argument");
                }
                break;
            case SEmulatorConstants.JUMP_ZERO_NAME:
                if (!arguments.containsKey(SEmulatorConstants.JZ_LABEL_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.JUMP_ZERO_NAME + " instruction requires '" + SEmulatorConstants.JZ_LABEL_ARG + "' argument");
                }
                break;
            case SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME:
                if (!arguments.containsKey(SEmulatorConstants.JE_CONSTANT_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.CONSTANT_VALUE_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME + " instruction requires '" + SEmulatorConstants.JE_CONSTANT_LABEL_ARG + "' and '" + SEmulatorConstants.CONSTANT_VALUE_ARG + "' arguments");
                }
                break;
            case SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME:
                if (!arguments.containsKey(SEmulatorConstants.JE_VARIABLE_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.VARIABLE_NAME_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME + " instruction requires '" + SEmulatorConstants.JE_VARIABLE_LABEL_ARG + "' and '" + SEmulatorConstants.VARIABLE_NAME_ARG + "' arguments");
                }
                break;
            case SEmulatorConstants.QUOTE_NAME:
                if (!arguments.containsKey(SEmulatorConstants.FUNCTION_NAME_ARG) || !arguments.containsKey(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.QUOTE_NAME + " instruction requires '" + SEmulatorConstants.FUNCTION_NAME_ARG + "' and '" + SEmulatorConstants.FUNCTION_ARGUMENTS_ARG + "' arguments");
                }
                break;
            case SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME:
                if (!arguments.containsKey(SEmulatorConstants.JE_FUNCTION_LABEL_ARG) || 
                    !arguments.containsKey(SEmulatorConstants.FUNCTION_NAME_ARG) || 
                    !arguments.containsKey(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG)) {
                    throw new XMLValidationException(SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME + " instruction requires '" + 
                        SEmulatorConstants.JE_FUNCTION_LABEL_ARG + "', '" + SEmulatorConstants.FUNCTION_NAME_ARG + "', and '" + 
                        SEmulatorConstants.FUNCTION_ARGUMENTS_ARG + "' arguments");
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
            if (!SEmulatorConstants.LABEL_PATTERN.matcher(referencedLabel).matches()) {
                throw new XMLValidationException("Invalid label format: '" + referencedLabel + 
                    "'. Must be 'L' followed by digits or 'EXIT'");
            }
            
            if (!definedLabels.contains(referencedLabel) && !referencedLabel.equals(SEmulatorConstants.EXIT_LABEL)) {
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
