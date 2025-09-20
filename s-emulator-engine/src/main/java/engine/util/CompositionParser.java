package engine.util;

import java.util.ArrayList;
import java.util.List;

public class CompositionParser {
    
    private CompositionParser() {
    }
    
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
    
    private static String parseFunctionCall(String functionCallStr, List<FunctionCall> functionCalls) {
        if (!functionCallStr.startsWith("(") || !functionCallStr.endsWith(")")) {
            return functionCallStr.trim();
        }
        
        String content = functionCallStr.substring(1, functionCallStr.length() - 1);
        
        int firstCommaIndex = findTopLevelComma(content, 0);
        String functionName;
        String remainingContent;
        
        if (firstCommaIndex == -1) {
            functionName = content.trim();
            remainingContent = "";
        } else {
            functionName = content.substring(0, firstCommaIndex).trim();
            remainingContent = content.substring(firstCommaIndex + 1);
        }
        
        if (functionName.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be empty: " + functionCallStr);
        }
        
        List<String> arguments = new ArrayList<>();
        
        int startIndex = 0;
        while (startIndex < remainingContent.length()) {
            int nextCommaIndex = findTopLevelComma(remainingContent, startIndex);
            String argument;
            
            if (nextCommaIndex == -1) {
                argument = remainingContent.substring(startIndex).trim();
                startIndex = remainingContent.length();
            } else {
                argument = remainingContent.substring(startIndex, nextCommaIndex).trim();
                startIndex = nextCommaIndex + 1;
            }
            
            if (argument.startsWith("(") && argument.endsWith(")")) {
                String resultVar = parseFunctionCall(argument, functionCalls);
                arguments.add(resultVar);
            } else {
                arguments.add(argument);
            }
        }
        
        String resultVar = "z_func_" + functionCalls.size();
        
        functionCalls.add(new FunctionCall(functionName, arguments));
        
        return resultVar;
    }
    
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
                    return false;
                }
            }
        }
        
        return level == 0;
    }
}
