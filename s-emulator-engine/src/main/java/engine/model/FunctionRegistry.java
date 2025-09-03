package engine.model;

import engine.api.SProgram;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for managing functions (sub-programs) in the S-Emulator.
 * Provides storage and retrieval of functions with their display names.
 */
public class FunctionRegistry {
    
    private final Map<String, SProgram> functions;
    private final Map<String, String> functionDisplayNames;
    
    /**
     * Creates a new function registry.
     */
    public FunctionRegistry() {
        this.functions = new HashMap<>();
        this.functionDisplayNames = new HashMap<>();
    }
    
    /**
     * Registers a function with its formal name, display name, and program.
     * 
     * @param name the formal function name (unique identifier)
     * @param displayName the user-friendly display name
     * @param program the S-Program that implements this function
     * @throws IllegalArgumentException if name is null/empty or program is null
     */
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
    
    /**
     * Retrieves a function program by its formal name.
     * 
     * @param name the formal function name
     * @return the function program, or null if not found
     */
    public SProgram getFunction(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return functions.get(name.trim());
    }
    
    /**
     * Checks if a function exists in the registry.
     * 
     * @param name the formal function name
     * @return true if function exists, false otherwise
     */
    public boolean functionExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return functions.containsKey(name.trim());
    }
    
    /**
     * Gets the display name for a function.
     * 
     * @param name the formal function name
     * @return the display name, or null if function not found
     */
    public String getFunctionDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return functionDisplayNames.get(name.trim());
    }
    
    /**
     * Gets all registered function names.
     * 
     * @return set of all formal function names
     */
    public Set<String> getAllFunctionNames() {
        return Set.copyOf(functions.keySet());
    }
    
    /**
     * Gets metadata for all functions.
     * 
     * @return map of function names to their display names
     */
    public Map<String, String> getAllFunctions() {
        return Map.copyOf(functionDisplayNames);
    }
    
    /**
     * Clears all registered functions.
     */
    public void clear() {
        functions.clear();
        functionDisplayNames.clear();
    }
    
    /**
     * Gets the number of registered functions.
     * 
     * @return the count of registered functions
     */
    public int size() {
        return functions.size();
    }
    
    /**
     * Checks if the registry is empty.
     * 
     * @return true if no functions are registered, false otherwise
     */
    public boolean isEmpty() {
        return functions.isEmpty();
    }
}
