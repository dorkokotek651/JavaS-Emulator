package engine.integration;

import engine.api.ExecutionResult;
import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.exception.SProgramException;
import engine.model.SEmulatorEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MinusProgramExpansionTest {
    
    private SEmulatorEngine engine;
    private String testResourcesPath;
    
    @BeforeEach
    void setUp() throws SProgramException {
        engine = new SEmulatorEngineImpl();
        
        URL resourcesUrl = getClass().getClassLoader().getResource(".");
        assertNotNull(resourcesUrl, "Test resources directory not found");
        testResourcesPath = resourcesUrl.getPath();
        
        engine.loadProgram(testResourcesPath + "minus.xml");
    }
    
    @Test
    void testMinusProgramAtAllExpansionLevels() throws SProgramException {
        SProgram program = engine.getCurrentProgram();
        int maxExpansionLevel = program.getMaxExpansionLevel();
        
        System.out.println("=== MINUS PROGRAM EXPANSION ANALYSIS ===");
        System.out.println("Program: " + program.getName());
        System.out.println("Max Expansion Level: " + maxExpansionLevel);
        System.out.println("Input Variables: " + program.getInputVariables());
        System.out.println();
        
        List<Integer> testInputs = Arrays.asList(10, 3);
        
        for (int level = 0; level <= maxExpansionLevel; level++) {
            System.out.println("--- EXPANSION LEVEL " + level + " ---");
            
            System.out.println("Program Display:");
            String programDisplay = (level == 0) ? engine.displayProgram() : engine.expandProgram(level);
            System.out.println(programDisplay);
            System.out.println();
            
            if (level > 0) {
                System.out.println("Program with History:");
                String programWithHistory = engine.expandProgramWithHistory(level);
                System.out.println(programWithHistory);
                System.out.println();
            }
            
            System.out.println("Execution with inputs " + testInputs + ":");
            ExecutionResult result = engine.runProgram(level, testInputs);
            
            System.out.println("Result: " + result);
            System.out.println("Y Value (x1 - x2): " + result.getYValue());
            System.out.println("Total Cycles: " + result.getTotalCycles());
            System.out.println("Input Variables: " + result.getInputVariables());
            System.out.println("Working Variables: " + result.getWorkingVariables());
            System.out.println("Number of Executed Instructions: " + result.getExecutedInstructions().size());
            
            assertEquals(7, result.getYValue(), "Should compute 10 - 3 = 7 at level " + level);
            assertTrue(result.getTotalCycles() > 0, "Should have positive cycles at level " + level);
            
            System.out.println("=".repeat(50));
            System.out.println();
        }
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void testMinusProgramWithDifferentInputsAtLevel(int expansionLevel) throws SProgramException {
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        
        if (expansionLevel > maxLevel) {
            return;
        }
        
        System.out.println("Testing expansion level " + expansionLevel);
        
        int[][] testCases = {
            {5, 2},
            {10, 4},
            {7, 7},
            {15, 0},
            {0, 0},
            {20, 5}
        };
        
        for (int[] testCase : testCases) {
            int x1 = testCase[0];
            int x2 = testCase[1];
            int expected = x1 - x2;
            
            List<Integer> inputs = Arrays.asList(x1, x2);
            ExecutionResult result = engine.runProgram(expansionLevel, inputs);
            
            System.out.println("  " + x1 + " - " + x2 + " = " + result.getYValue() + 
                             " (cycles: " + result.getTotalCycles() + ")");
            
            assertEquals(expected, result.getYValue(), 
                "Should compute " + x1 + " - " + x2 + " = " + expected + " at expansion level " + expansionLevel);
        }
        System.out.println();
    }
    
    @Test
    void testCycleCountComparison() throws SProgramException {
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        
        List<Integer> inputs = Arrays.asList(8, 3);
        
        System.out.println("=== CYCLE COUNT COMPARISON ===");
        System.out.println("Computing 8 - 3 at different expansion levels:");
        
        for (int level = 0; level <= maxLevel; level++) {
            ExecutionResult result = engine.runProgram(level, inputs);
            System.out.println("Level " + level + ": " + result.getTotalCycles() + " cycles, " + 
                             result.getExecutedInstructions().size() + " instructions executed");
        }
    }
    
    @Test
    void testMinusProgramStructure() {
        SProgram program = engine.getCurrentProgram();
        
        assertEquals("Minus", program.getName());
        assertEquals(2, program.getInputVariables().size());
        assertTrue(program.getInputVariables().contains("x1"));
        assertTrue(program.getInputVariables().contains("x2"));
        
        List<String> labels = program.getLabels();
        assertTrue(labels.contains("L1"));
        assertTrue(labels.contains("L2")); 
        assertTrue(labels.contains("L3"));
        
        assertEquals(9, program.getInstructions().size());
        
        assertTrue(program.getMaxExpansionLevel() > 0, 
                  "Minus program should have synthetic instructions requiring expansion");
    }
    
    @Test
    void testMinusEdgeCases() throws SProgramException {
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        
        System.out.println("=== EDGE CASES ===");
        
        int[][] edgeCases = {
            {0, 5},
            {3, 10},
            {100, 100},
            {1, 0},
            {0, 0}
        };
        
        for (int level = 0; level <= Math.min(maxLevel, 2); level++) {
            System.out.println("--- Level " + level + " ---");
            
            for (int[] testCase : edgeCases) {
                int x1 = testCase[0];
                int x2 = testCase[1];
                int expected = Math.max(0, x1 - x2);
                
                List<Integer> inputs = Arrays.asList(x1, x2);
                ExecutionResult result = engine.runProgram(level, inputs);
                
                System.out.println("  " + x1 + " - " + x2 + " = " + result.getYValue());
                assertEquals(expected, result.getYValue());
            }
            System.out.println();
        }
    }
}
