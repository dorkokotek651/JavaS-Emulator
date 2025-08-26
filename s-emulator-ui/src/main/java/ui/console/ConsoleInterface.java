package ui.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleInterface {
    private final Scanner scanner;
    private boolean running;

    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void displayWelcome() {
        System.out.println(OutputFormatter.formatWelcome());
    }

    public void displayGoodbye() {
        System.out.println(OutputFormatter.formatGoodbye());
    }

    public void displayMenu(String title, List<String> options) {
        System.out.print(OutputFormatter.formatMenu(title, options));
    }

    public int getUserChoice(int minChoice, int maxChoice) {
        while (true) {
            try {
                String input = scanner.nextLine();
                return InputValidator.validateMenuChoice(input, minChoice, maxChoice);
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
                System.out.print("Please try again (1-" + maxChoice + "): ");
            }
        }
    }

    public String getFilePath() {
        while (true) {
            System.out.print("Enter the XML file path: ");
            try {
                String input = scanner.nextLine();
                String trimmedInput = input.trim();
                
                if ((trimmedInput.startsWith("\"") && trimmedInput.endsWith("\"")) ||
                    (trimmedInput.startsWith("'") && trimmedInput.endsWith("'"))) {
                    trimmedInput = trimmedInput.substring(1, trimmedInput.length() - 1);
                }
                
                InputValidator.validateFilePath(trimmedInput);
                return trimmedInput;
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }
    
    public String getFilePathInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                String trimmedInput = input.trim();
                
                if ((trimmedInput.startsWith("\"") && trimmedInput.endsWith("\"")) ||
                    (trimmedInput.startsWith("'") && trimmedInput.endsWith("'"))) {
                    trimmedInput = trimmedInput.substring(1, trimmedInput.length() - 1);
                }
                
                if (trimmedInput.isEmpty()) {
                    throw new IllegalArgumentException("File path cannot be empty");
                }
                
                return trimmedInput;
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public int getExpansionLevel(int maxLevel) {
        while (true) {
            System.out.print("Enter expansion level (0-" + maxLevel + "): ");
            try {
                String input = scanner.nextLine();
                return InputValidator.validateExpansionLevel(input, maxLevel);
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public List<Integer> getProgramInputs(List<String> inputVariables) {
        if (inputVariables == null || inputVariables.isEmpty()) {
            displayInfo("This program requires no input values.");
            return List.of();
        }
        
        while (true) {
            System.out.println("Program requires " + inputVariables.size() + 
                " input values for variables: " + inputVariables);
            System.out.print("Enter comma-separated values (e.g., 1, 2, 3): ");
            
            try {
                String input = scanner.nextLine();
                List<Integer> values = InputValidator.parseIntegerList(input);
                List<Integer> adjustedValues = adjustInputsToRequiredSize(values, inputVariables);
                
                if (values.size() < inputVariables.size()) {
                    displayInfo("Received " + values.size() + " inputs, padding missing variables with 0.");
                } else if (values.size() > inputVariables.size()) {
                    displayInfo("Received " + values.size() + " inputs, using first " + inputVariables.size() + " values.");
                }
                
                return adjustedValues;
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public boolean confirmAction(String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            try {
                String input = scanner.nextLine();
                return InputValidator.validateYesNo(input);
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public void displayMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            System.out.println(message);
        }
    }

    public void displayError(String message) {
        System.out.println(OutputFormatter.formatError(message));
    }

    public void displaySuccess(String message) {
        System.out.println(OutputFormatter.formatSuccess(message));
    }

    public void displayWarning(String message) {
        System.out.println(OutputFormatter.formatWarning(message));
    }

    public void displayInfo(String message) {
        System.out.println(OutputFormatter.formatInfo(message));
    }

    public void displaySeparator() {
        System.out.println(OutputFormatter.formatSeparator());
    }

    public void displayThinSeparator() {
        System.out.println(OutputFormatter.formatThinSeparator());
    }

    public void waitForEnter() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
        }
        running = false;
    }

    public String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int getIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                return InputValidator.validateInteger(input);
            } catch (IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public void displayProgramOutput(String output) {
        if (output != null && !output.trim().isEmpty()) {
            displaySeparator();
            System.out.println(output);
            displaySeparator();
        }
    }

    private List<Integer> adjustInputsToRequiredSize(List<Integer> inputs, List<String> requiredInputs) {
        if (inputs.size() == requiredInputs.size()) {
            return new ArrayList<>(inputs);
        }
        
        List<Integer> adjustedInputs = new ArrayList<>();
        
        for (int i = 0; i < requiredInputs.size(); i++) {
            if (i < inputs.size()) {
                adjustedInputs.add(inputs.get(i));
            } else {
                adjustedInputs.add(0);
            }
        }
        
        return adjustedInputs;
    }
}
