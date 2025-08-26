package engine.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import engine.api.ExecutionResult;
import engine.api.SInstruction;
import engine.api.SProgram;
import engine.api.SystemState;
import engine.exception.StateSerializationException;
import engine.model.SProgramImpl;
import engine.model.instruction.InstructionFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemStateSerializer {
    private final ObjectMapper objectMapper;
    public SystemStateSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    public void saveToFile(SystemState state, String filePath) throws StateSerializationException {
        try {
            String fullPath = ensureJsonExtension(filePath);
            
            validateFilePath(fullPath);
            
            SystemStateData stateData = convertToData(state);
            
            objectMapper.writeValue(new File(fullPath), stateData);
            
        } catch (IOException e) {
            throw new StateSerializationException("Failed to save state to file: " + filePath, e);
        }
    }
    
    public SystemState loadFromFile(String filePath) throws StateSerializationException {
        try {
            String fullPath = ensureJsonExtension(filePath);
            
            File file = new File(fullPath);
            if (!file.exists()) {
                throw new StateSerializationException("State file not found: " + fullPath);
            }
            if (!file.canRead()) {
                throw new StateSerializationException("Cannot read state file: " + fullPath);
            }
            
            SystemStateData stateData = objectMapper.readValue(file, SystemStateData.class);
            
            return convertFromData(stateData);
            
        } catch (IOException e) {
            throw new StateSerializationException("Failed to load state from file: " + filePath, e);
        }
    }
    
    private String ensureJsonExtension(String filePath) {
        if (filePath.endsWith(".json")) {
            return filePath;
        }
        return filePath + ".json";
    }
    
    private void validateFilePath(String filePath) throws StateSerializationException {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            
            if (parentDir != null && !Files.exists(parentDir)) {
                throw new StateSerializationException("Directory does not exist: " + parentDir);
            }
            
            if (Files.exists(path) && !Files.isWritable(path)) {
                throw new StateSerializationException("File is not writable: " + filePath);
            }
            
        } catch (Exception e) {
            throw new StateSerializationException("Invalid file path: " + filePath, e);
        }
    }
    
    private SystemStateData convertToData(SystemState state) {
        ProgramStateData programData = null;
        if (state.getCurrentProgram() != null) {
            programData = convertProgramToData(state.getCurrentProgram());
        }
        
        List<ExecutionResultData> historyData = new ArrayList<>();
        for (ExecutionResult result : state.getExecutionHistory()) {
            historyData.add(convertExecutionResultToData(result));
        }
        
        return new SystemStateData(programData, historyData, state.getNextRunNumber());
    }
    
    private ProgramStateData convertProgramToData(SProgram program) {
        List<InstructionData> instructionsData = new ArrayList<>();
        for (SInstruction instruction : program.getInstructions()) {
            instructionsData.add(convertInstructionToData(instruction));
        }
        
        return new ProgramStateData(program.getName(), instructionsData);
    }
    
    private InstructionData convertInstructionToData(SInstruction instruction) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", instruction.getName());
        properties.put("variable", instruction.getVariable());
        properties.put("cycles", instruction.getCycles());
        properties.put("arguments", instruction.getArguments());
        
        return new InstructionData(
            instruction.getClass().getSimpleName(),
            instruction.getLabel(),
            properties
        );
    }
    
    private ExecutionResultData convertExecutionResultToData(ExecutionResult result) {
        return new ExecutionResultData(
            result.getRunNumber(),
            result.getExpansionLevel(),
            result.getInputs(),
            result.getYValue(),
            result.getInputVariables(),
            result.getWorkingVariables(),
            result.getTotalCycles()
        );
    }
    
    private SystemState convertFromData(SystemStateData data) throws StateSerializationException {
        try {
            SProgram program = null;
            if (data.getProgram() != null) {
                program = convertDataToProgram(data.getProgram());
            }
            
            List<ExecutionResult> history = new ArrayList<>();
            for (ExecutionResultData resultData : data.getExecutionHistory()) {
                history.add(convertDataToExecutionResult(resultData));
            }
            
            return new SystemStateImpl(program, history, data.getNextRunNumber());
            
        } catch (Exception e) {
            throw new StateSerializationException("Failed to convert loaded data to system state", e);
        }
    }
    
    private SProgram convertDataToProgram(ProgramStateData data) throws StateSerializationException {
        try {
            SProgramImpl program = new SProgramImpl(data.getName());
            
            for (InstructionData instrData : data.getInstructions()) {
                SInstruction instruction = convertDataToInstruction(instrData);
                program.addInstruction(instruction);
            }
            
            return program;
            
        } catch (Exception e) {
            throw new StateSerializationException("Failed to reconstruct program from data", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private SInstruction convertDataToInstruction(InstructionData data) throws StateSerializationException {
        try {
            Map<String, Object> props = data.getProperties();
            String variable = (String) props.get("variable");
            Map<String, String> arguments = (Map<String, String>) props.get("arguments");
            
            return InstructionFactory.createInstruction(
                (String) props.get("name"),
                variable,
                data.getLabel(),
                arguments != null ? arguments : new HashMap<>()
            );
            
        } catch (Exception e) {
            throw new StateSerializationException("Failed to reconstruct instruction: " + data.getType(), e);
        }
    }
    
    private ExecutionResult convertDataToExecutionResult(ExecutionResultData data) {
        return new ExecutionResult(
            data.getRunNumber(),
            data.getExpansionLevel(),
            data.getInputs(),
            data.getYValue(),
            data.getInputVariables(),
            data.getWorkingVariables(),
            data.getTotalCycles(),
            new ArrayList<>()
        );
    }
}
