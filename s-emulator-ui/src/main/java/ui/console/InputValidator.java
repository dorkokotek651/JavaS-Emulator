package ui.console;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InputValidator {
    
    private InputValidator() {
        // Utility class - prevent instantiation
    }

    public static int validateInteger(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
        
        String trimmedInput = input.trim();
        
        try {
            int value = Integer.parseInt(trimmedInput);
            if (value < 0) {
                throw new IllegalArgumentException("Value cannot be negative: " + value);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer format: '" + trimmedInput + "'. Please enter a valid non-negative integer.", e);
        }
    }

    public static void validateFilePath(String path) throws IllegalArgumentException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        String trimmedPath = path.trim();
        
        if (!trimmedPath.toLowerCase().endsWith(".xml")) {
            throw new IllegalArgumentException("File must have .xml extension: " + trimmedPath);
        }
        
        File file = new File(trimmedPath);
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + trimmedPath);
        }
        
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path does not point to a file: " + trimmedPath);
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable: " + trimmedPath);
        }
    }

    public static int validateExpansionLevel(String input, int maxLevel) throws IllegalArgumentException {
        int level = validateInteger(input);
        
        if (level > maxLevel) {
            throw new IllegalArgumentException("Expansion level " + level + " exceeds maximum level " + maxLevel + 
                ". Please enter a value between 0 and " + maxLevel + ".");
        }
        
        return level;
    }

    public static int validateMenuChoice(String input, int minChoice, int maxChoice) throws IllegalArgumentException {
        int choice = validateInteger(input);
        
        if (choice < minChoice || choice > maxChoice) {
            throw new IllegalArgumentException("Invalid menu choice: " + choice + 
                ". Please enter a value between " + minChoice + " and " + maxChoice + ".");
        }
        
        return choice;
    }

    public static List<Integer> parseIntegerList(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String trimmedInput = input.trim();
        List<Integer> values = new ArrayList<>();
        
        // Handle empty input or just whitespace
        if (trimmedInput.isEmpty()) {
            return values;
        }
        
        // Split by comma and parse each value
        String[] parts = trimmedInput.split(",");
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Empty value found at position " + (i + 1) + 
                    ". Please provide comma-separated integers (e.g., '1, 2, 3').");
            }
            
            try {
                int value = Integer.parseInt(part);
                if (value < 0) {
                    throw new IllegalArgumentException("Negative value not allowed at position " + (i + 1) + 
                        ": " + value + ". All input values must be non-negative.");
                }
                values.add(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer at position " + (i + 1) + 
                    ": '" + part + "'. Please provide comma-separated integers (e.g., '1, 2, 3').", e);
            }
        }
        
        return values;
    }

    public static boolean validateYesNo(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty. Please enter 'y' for yes or 'n' for no.");
        }
        
        String trimmedInput = input.trim().toLowerCase();
        
        return switch (trimmedInput) {
            case "y", "yes" -> true;
            case "n", "no" -> false;
            default -> throw new IllegalArgumentException("Invalid input: '" + input + 
                "'. Please enter 'y' for yes or 'n' for no.");
        };
    }

    public static String validateNonEmptyString(String input, String fieldName) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        return input.trim();
    }

    public static void validateInputCount(List<Integer> inputs, List<String> requiredInputs) throws IllegalArgumentException {
        if (inputs.size() != requiredInputs.size()) {
            throw new IllegalArgumentException("Expected " + requiredInputs.size() + 
                " input values for variables " + requiredInputs + 
                ", but got " + inputs.size() + " values: " + inputs);
        }
    }
}
