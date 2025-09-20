package fx.util;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.exception.SProgramException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileValidator {
    
    private final SEmulatorEngine engine;
    
    public FileValidator(SEmulatorEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null");
        }
        this.engine = engine;
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors != null ? errors : new ArrayList<>());
            this.warnings = new ArrayList<>(warnings != null ? warnings : new ArrayList<>());
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) {
                return "";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Errors:\n");
            for (int i = 0; i < errors.size(); i++) {
                sb.append(i + 1).append(". ").append(errors.get(i)).append("\n");
            }
            return sb.toString().trim();
        }
        
        public String getWarningSummary() {
            if (warnings.isEmpty()) {
                return "";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Warnings:\n");
            for (int i = 0; i < warnings.size(); i++) {
                sb.append(i + 1).append(". ").append(warnings.get(i)).append("\n");
            }
            return sb.toString().trim();
        }
    }
    
    public ValidationResult validateFile(File file) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            validateBasicFile(file);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
            return new ValidationResult(false, errors, warnings);
        }

        try {

            engine.loadProgram(file.getAbsolutePath());

            SProgram program = engine.getCurrentProgram();
            if (program != null) {
                validateProgramStructure(program, errors, warnings);
                validateFunctionReferences(program, errors);
            }
            
        } catch (SProgramException e) {
            errors.add("Program validation failed: " + e.getMessage());
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings);
    }
    
    private void validateBasicFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getName());
        }
        
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file.getName());
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable: " + file.getName());
        }

        String fileName = file.getName().toLowerCase(java.util.Locale.ENGLISH);
        if (!fileName.endsWith(".xml")) {
            throw new IllegalArgumentException("File must be an XML file (.xml extension required)");
        }

        long fileSizeKB = file.length() / 1024;
        if (fileSizeKB == 0) {
            throw new IllegalArgumentException("File is empty: " + file.getName());
        }
        
        if (fileSizeKB > 10240) {
            throw new IllegalArgumentException("File is too large (maximum 10MB): " + fileSizeKB + "KB");
        }
    }
    
    private void validateProgramStructure(SProgram program, List<String> errors, List<String> warnings) {
        if (program == null) {
            errors.add("Program is null after loading");
            return;
        }

        String programName = program.getName();
        if (programName == null || programName.trim().isEmpty()) {
            errors.add("Program must have a name");
        }

        if (program.getInstructions().isEmpty()) {
            errors.add("Program must contain at least one instruction");
        }

        int instructionCount = program.getInstructions().size();
        if (instructionCount > 10000) {
            warnings.add("Program has many instructions (" + instructionCount + "). This may affect performance.");
        }

        int maxLevel = program.getMaxExpansionLevel();
        if (maxLevel > 10) {
            warnings.add("Program has high expansion level (" + maxLevel + "). This may affect performance.");
        }
    }
    
    private void validateFunctionReferences(SProgram program, List<String> errors) {

        String programDisplay = program.toString();
        if (programDisplay.contains("QUOTE") || programDisplay.contains("JUMP_EQUAL_FUNCTION")) {
        }
    }
    
    public List<String> validateFilePath(File file) {
        List<String> warnings = new ArrayList<>();
        
        if (file == null) {
            return warnings;
        }
        
        String path = file.getAbsolutePath();

        if (path.contains(" ")) {
            warnings.add("File path contains spaces. This should work fine but may cause issues in some environments.");
        }

        if (path.length() > 260) {
            warnings.add("File path is very long (" + path.length() + " characters). This may cause issues on some systems.");
        }

        if (path.matches(".*[<>:\"|?*].*")) {
            warnings.add("File path contains special characters that may cause issues.");
        }

        if (!path.matches("^[\\x00-\\x7F]*$")) {
            warnings.add("File path contains non-ASCII characters. This may cause issues in some environments.");
        }
        
        return warnings;
    }
    
    public String getDisplayMessage(ValidationResult validationResult) {
        if (validationResult == null) {
            return "Validation failed: Unknown error";
        }
        
        if (validationResult.isValid()) {
            if (validationResult.hasWarnings()) {
                return "File is valid with warnings:\n" + validationResult.getWarningSummary();
            } else {
                return "File is valid and ready to load";
            }
        } else {
            StringBuilder message = new StringBuilder();
            message.append("File validation failed:\n");
            message.append(validationResult.getErrorSummary());
            
            if (validationResult.hasWarnings()) {
                message.append("\n\nAdditional warnings:\n");
                message.append(validationResult.getWarningSummary());
            }
            
            return message.toString();
        }
    }
}
