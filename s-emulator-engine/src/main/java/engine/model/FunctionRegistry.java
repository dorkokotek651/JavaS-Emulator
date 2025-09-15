package engine.model;

import engine.api.SProgram;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FunctionRegistry {
    
    private final Map<String, SProgram> functions;
    private final Map<String, String> functionDisplayNames;
    
    public FunctionRegistry() {
        this.functions = new HashMap<>();
        this.functionDisplayNames = new HashMap<>();
    }
    
    public void registerFunction(String name, String displayName, SProgram program) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        if (program == null) {
            throw new IllegalArgumentException("Function program cannot be null");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Function display name cannot be null or empty");
        }
        
        String formalName = name.trim();
        String userDisplayName = displayName.trim();
        
        functions.put(formalName, program);
        functionDisplayNames.put(formalName, userDisplayName);
    }
    
    public SProgram getFunction(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return functions.get(name.trim());
    }
    
    public boolean functionExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return functions.containsKey(name.trim());
    }
    
    public String getFunctionDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return functionDisplayNames.get(name.trim());
    }
    
    public Set<String> getAllFunctionNames() {
        return Set.copyOf(functions.keySet());
    }
    
    public Map<String, String> getAllFunctions() {
        return Map.copyOf(functionDisplayNames);
    }
    
    public void clear() {
        functions.clear();
        functionDisplayNames.clear();
    }
    
    public int size() {
        return functions.size();
    }
    
    public boolean isEmpty() {
        return functions.isEmpty();
    }
}
