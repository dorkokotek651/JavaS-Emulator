package engine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing function composition expressions.
 * Handles nested function calls like: (mul,(+,x1,y),(S,(S,x2)))
 */
public class CompositionParser {
    
    /**
     * Represents a parsed function call with its name and arguments.
     */
    public static class FunctionCall {
        private final String functionName;
        private final List<String> arguments;
        
        public FunctionCall(String functionName, List<String> arguments) {
            this.functionName = functionName;
            this.arguments = new ArrayList<>(arguments);
        }
        
        public String getFunctionName() {
            return functionName;
        }
        
        public List<String> getArguments() {
            return new ArrayList<>(arguments);
        }
        
        @Override
        public String toString() {
            return "FunctionCall{name='" + functionName + "', args=" + arguments + "}";
        }
    }
    
    /**
     * Parses a function composition string into individual function calls.
     * Handles nested parentheses and comma separation.
     * 
     * @param composition the composition string to parse
     * @return list of function calls in execution order
     * @throws IllegalArgumentException if composition is invalid
     */
    public static List<FunctionCall> parseComposition(String composition) {
        if (composition == null || composition.trim().isEmpty()) {
            throw new IllegalArgumentException("Composition string cannot be null or empty");
        }
        
        String trimmed = composition.trim();
        if (!trimmed.startsWith("(") || !trimmed.endsWith(")")) {
            throw new IllegalArgumentException("Composition must be enclosed in parentheses: " + composition);
        }
        
        List<FunctionCall> functionCalls = new ArrayList<>();
        parseFunctionCall(trimmed, functionCalls);
        return functionCalls;
    }
    
    /**
     * Parses a single function call and adds it to the list.
     * Recursively handles nested function calls.
     * 
     * @param functionCallStr the function call string
     * @param functionCalls the list to add parsed calls to
     * @return the variable name that will hold the result of this function call
     */
    private static String parseFunctionCall(String functionCallStr, List<FunctionCall> functionCalls) {
        if (!functionCallStr.startsWith("(") || !functionCallStr.endsWith(")")) {
            // This is a simple variable, not a function call
            return functionCallStr.trim();
        }
        
        // Remove outer parentheses
        String content = functionCallStr.substring(1, functionCallStr.length() - 1);
        
        // Find function name (first token before comma, or entire content if no comma)
        int firstCommaIndex = findTopLevelComma(content, 0);
        String functionName;
        String remainingContent;
        
        if (firstCommaIndex == -1) {
            // No comma found - this is a zero-argument function
            functionName = content.trim();
            remainingContent = "";
        } else {
            functionName = content.substring(0, firstCommaIndex).trim();
            remainingContent = content.substring(firstCommaIndex + 1);
        }
        
        if (functionName.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be empty: " + functionCallStr);
        }
        
        // Parse arguments
        List<String> arguments = new ArrayList<>();
        
        int startIndex = 0;
        while (startIndex < remainingContent.length()) {
            int nextCommaIndex = findTopLevelComma(remainingContent, startIndex);
            String argument;
            
            if (nextCommaIndex == -1) {
                // Last argument
                argument = remainingContent.substring(startIndex).trim();
                startIndex = remainingContent.length();
            } else {
                argument = remainingContent.substring(startIndex, nextCommaIndex).trim();
                startIndex = nextCommaIndex + 1;
            }
            
            if (argument.startsWith("(") && argument.endsWith(")")) {
                // This argument is a nested function call
                String resultVar = parseFunctionCall(argument, functionCalls);
                arguments.add(resultVar);
            } else {
                // This argument is a simple variable
                arguments.add(argument);
            }
        }
        
        // Generate a unique variable name for this function call result
        String resultVar = "z_func_" + functionCalls.size();
        
        // Add this function call
        functionCalls.add(new FunctionCall(functionName, arguments));
        
        return resultVar;
    }
    
    /**
     * Finds the next comma at the top level (not inside nested parentheses).
     * 
     * @param str the string to search
     * @param startIndex the index to start searching from
     * @return the index of the next top-level comma, or -1 if not found
     */
    private static int findTopLevelComma(String str, int startIndex) {
        int parenLevel = 0;
        
        for (int i = startIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (c == '(') {
                parenLevel++;
            } else if (c == ')') {
                parenLevel--;
            } else if (c == ',' && parenLevel == 0) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Validates that parentheses are properly balanced.
     * 
     * @param str the string to validate
     * @return true if parentheses are balanced, false otherwise
     */
    public static boolean areParenthesesBalanced(String str) {
        if (str == null) {
            return false;
        }
        
        int level = 0;
        for (char c : str.toCharArray()) {
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
                if (level < 0) {
                    return false; // More closing than opening
                }
            }
        }
        
        return level == 0; // Should end with balanced parentheses
    }
}
