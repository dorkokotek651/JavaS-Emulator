package ui.console;

import engine.api.ExecutionResult;
import engine.api.SInstruction;
import java.util.List;
import java.util.Map;

public class OutputFormatter {
    private static final int INDEX_WIDTH = 6;
    private static final String SEPARATOR = "=" + "=".repeat(100);
    private static final String THIN_SEPARATOR = "-" + "-".repeat(100);
    
    private OutputFormatter() {
    }

    public static String formatIndex(int index) {
        String indexStr = "#" + index;
        for (int i = indexStr.length(); i < INDEX_WIDTH; i++) {
            indexStr = indexStr + " ";
        }
        return indexStr;
    }

    public static String formatInstruction(int index, SInstruction instruction) {
        if (instruction == null) {
            return formatIndex(index) + " <null instruction>";
        }
        
        return formatIndex(index) + " " + instruction.toString();
    }

    public static String formatInstructionList(List<SInstruction> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            return "No instructions found.";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            sb.append(formatInstruction(i + 1, instructions.get(i)));
            if (i < instructions.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String formatExecutionResult(ExecutionResult result) {
        if (result == null) {
            return "No execution result available.";
        }
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Execution Result #").append(result.getRunNumber()).append(":\n");
        sb.append(THIN_SEPARATOR).append("\n");
        sb.append("Expansion Level: ").append(result.getExpansionLevel()).append("\n");
        sb.append("Input Values: ").append(formatInputValues(result.getInputs())).append("\n");
        sb.append("Result (y): ").append(result.getYValue()).append("\n");
        sb.append("Total Cycles: ").append(result.getTotalCycles()).append("\n");
        
        return sb.toString();
    }

    public static String formatExecutionHistory(List<ExecutionResult> history) {
        if (history == null || history.isEmpty()) {
            return "No execution history available.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Execution History (").append(history.size()).append(" runs):\n");
        sb.append(SEPARATOR).append("\n");
        
        for (int i = 0; i < history.size(); i++) {
            ExecutionResult result = history.get(i);
            sb.append(formatExecutionResultSummary(result));
            if (i < history.size() - 1) {
                sb.append("\n").append(THIN_SEPARATOR).append("\n");
            }
        }
        
        return sb.toString();
    }

    public static String formatExecutionResultSummary(ExecutionResult result) {
        if (result == null) {
            return "No result available.";
        }
        
        return String.format("Run #%d | Level: %d | Inputs: %s | Result: %d | Cycles: %d",
            result.getRunNumber(),
            result.getExpansionLevel(),
            formatInputValues(result.getInputs()),
            result.getYValue(),
            result.getTotalCycles()
        );
    }

    public static String formatInputValues(List<Integer> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "[]";
        }
        
        return inputs.toString();
    }

    public static String formatVariableValues(int yValue, Map<String, Integer> inputVariables, Map<String, Integer> workingVariables) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        
        sb.append("y=").append(yValue);
        first = false;
        
        for (Map.Entry<String, Integer> entry : inputVariables.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        for (Map.Entry<String, Integer> entry : workingVariables.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }

    public static String formatProgramInfo(String name, List<String> inputVars, List<String> labels, int maxLevel) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Program: ").append(name != null ? name : "Unknown").append("\n");
        sb.append("Input Variables: ").append(inputVars != null ? inputVars : "[]").append("\n");
        sb.append("Labels: ").append(labels != null ? labels : "[]").append("\n");
        sb.append("Max Expansion Level: ").append(maxLevel).append("\n");
        
        return sb.toString();
    }

    public static String formatMenu(String title, List<String> options) {
        if (options == null || options.isEmpty()) {
            return title + "\nNo options available.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append(title).append("\n");
        sb.append(SEPARATOR).append("\n");
        
        for (int i = 0; i < options.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, options.get(i)));
        }
        
        sb.append(SEPARATOR).append("\n");
        sb.append("Please enter your choice (1-").append(options.size()).append("): ");
        
        return sb.toString();
    }

    public static String formatError(String message) {
        return "[ERROR] " + (message != null ? message : "Unknown error occurred");
    }

    public static String formatSuccess(String message) {
        return "[SUCCESS] " + (message != null ? message : "Operation completed successfully");
    }

    public static String formatWarning(String message) {
        return "[WARNING] " + (message != null ? message : "Warning");
    }

    public static String formatInfo(String message) {
        return "[INFO] " + (message != null ? message : "Information");
    }

    public static String formatSeparator() {
        return SEPARATOR;
    }

    public static String formatThinSeparator() {
        return THIN_SEPARATOR;
    }

    public static String formatWelcome() {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("    Welcome to S-Emulator Console Interface    \n");
        sb.append("    A Simple Programming Language Emulator     \n");
        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    public static String formatGoodbye() {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("    Thank you for using S-Emulator!    \n");
        sb.append("    Goodbye!                           \n");
        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }
}
