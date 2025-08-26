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

public class SEmulatorEngineIntegrationTest {
    
    private SEmulatorEngine engine;
    private String testResourcesPath;
    
    @BeforeEach
    void setUp() throws SProgramException {
        engine = new SEmulatorEngineImpl();
        
        URL resourcesUrl = getClass().getClassLoader().getResource(".");
        assertNotNull(resourcesUrl, "Test resources directory not found");
        testResourcesPath = resourcesUrl.getPath();
    }
    
    private String getXmlFilePath(String filename) {
        return testResourcesPath + filename;
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml", 
        "synthetic.xml",
        "successor.xml",
        "minus.xml",
        "badic.xml"
    })
    void testLoadProgram_ValidXmlFiles_ShouldLoadSuccessfully(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        
        assertFalse(engine.isProgramLoaded());
        
        engine.loadProgram(filePath);
        
        assertTrue(engine.isProgramLoaded());
        assertNotNull(engine.getCurrentProgram());
        
        SProgram program = engine.getCurrentProgram();
        assertNotNull(program.getName());
        assertNotNull(program.getInstructions());
        assertFalse(program.getInstructions().isEmpty());
    }
    
    @Test
    void testLoadProgram_InvalidXml_ShouldThrowException() {
        String filePath = getXmlFilePath("error-1.xml");
        
        assertThrows(SProgramException.class, () -> {
            engine.loadProgram(filePath);
        });
    }
    
    @Test
    void testLoadProgram_InvalidLabelFormat_ShouldThrowException() {
        String filePath = getXmlFilePath("id.xml");
        
        assertThrows(SProgramException.class, () -> {
            engine.loadProgram(filePath);
        });
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml",
        "synthetic.xml", 
        "successor.xml",
        "minus.xml",
        "badic.xml"
    })
    void testDisplayProgram_ValidXmlFiles_ShouldReturnValidDisplay(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        engine.loadProgram(filePath);
        
        String display = engine.displayProgram();
        
        assertNotNull(display);
        assertFalse(display.trim().isEmpty());
        assertTrue(display.contains("Program:"));
        assertTrue(display.contains("Instructions:"));
        assertTrue(display.contains("Input Variables:"));
        assertTrue(display.contains("Labels:"));
        assertTrue(display.contains("Max Expansion Level:"));
    }
    
    @Test
    void testDisplayProgram_NoProgram_ShouldReturnNoProgram() {
        String display = engine.displayProgram();
        assertEquals("No program loaded.", display);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml",
        "synthetic.xml",
        "successor.xml", 
        "minus.xml",
        "badic.xml"
    })
    void testExpandProgram_ValidXmlFiles_Level0_ShouldReturnOriginal(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        engine.loadProgram(filePath);
        
        String expanded = engine.expandProgram(0);
        
        assertNotNull(expanded);
        assertFalse(expanded.trim().isEmpty());
        assertTrue(expanded.contains("Program:"));
        assertTrue(expanded.contains("Instructions:"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml",
        "synthetic.xml",
        "successor.xml",
        "minus.xml"
    })
    void testExpandProgram_SyntheticInstructions_Level1_ShouldExpandSynthetics(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        
        if (maxLevel > 0) {
            String expanded = engine.expandProgram(1);
            
            assertNotNull(expanded);
            assertFalse(expanded.trim().isEmpty());
            assertTrue(expanded.contains("Program:"));
            assertTrue(expanded.contains("Instructions:"));
        }
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml",
        "synthetic.xml", 
        "successor.xml",
        "minus.xml"
    })
    void testExpandProgramWithHistory_SyntheticInstructions_ShouldShowHistory(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        
        if (maxLevel > 0) {
            String expandedWithHistory = engine.expandProgramWithHistory(1);
            
            assertNotNull(expandedWithHistory);
            assertFalse(expandedWithHistory.trim().isEmpty());
            assertTrue(expandedWithHistory.contains("Program:"));
        }
    }
    
    @Test
    void testRunProgram_BasicXml_WithInputs_ShouldExecuteSuccessfully() throws SProgramException {
        String filePath = getXmlFilePath("badic.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(5);
        if (inputVars.size() == 1) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertEquals(1, result.getRunNumber());
            assertEquals(0, result.getExpansionLevel());
            assertEquals(inputs, result.getInputs());
            assertTrue(result.getTotalCycles() >= 0);
            assertEquals(5, result.getYValue());
        }
    }
    
    @Test 
    void testRunProgram_PredictableTestXml_WithInputs_ShouldReturnExpectedResult() throws SProgramException {
        String filePath = getXmlFilePath("predictable-test.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(0, 0, 0, 0);
        if (inputVars.size() == 4) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertEquals(1, result.getRunNumber());
            assertEquals(0, result.getExpansionLevel());
            assertEquals(inputs, result.getInputs());
            assertTrue(result.getTotalCycles() >= 0);
            
            assertEquals(42, result.getWorkingVariables().get("z2"));
        }
    }
    
    @Test
    void testRunProgram_SuccessorXml_WithInputs_ShouldComputeSuccessor() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(7);
        if (inputVars.size() == 1) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertEquals(8, result.getYValue());
        }
    }
    
    @Test
    void testRunProgram_MinusXml_WithInputs_ShouldComputeSubtraction() throws SProgramException {
        String filePath = getXmlFilePath("minus.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(10, 3);
        if (inputVars.size() == 2) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertEquals(7, result.getYValue());
        }
    }
    
    @Test
    void testRunProgram_BasicXml_WithInputs_ShouldCopyXToY() throws SProgramException {
        String filePath = getXmlFilePath("badic.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(5);
        if (inputVars.size() == 1) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertEquals(5, result.getYValue());
        }
    }
    
    @Test
    void testRunProgram_SyntheticXml_WithInputs_ShouldExecuteAllSynthetics() throws SProgramException {
        String filePath = getXmlFilePath("synthetic.xml");
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        List<String> inputVars = program.getInputVariables();
        
        List<Integer> inputs = Arrays.asList(0, 0);
        if (inputVars.size() == 2) {
            ExecutionResult result = engine.runProgram(0, inputs);
            
            assertNotNull(result);
            assertTrue(result.getTotalCycles() >= 0);
        }
    }
    

    
    @ParameterizedTest
    @ValueSource(strings = {
        "predictable-test.xml",
        "synthetic.xml",
        "successor.xml", 
        "minus.xml"
    })
    void testRunProgram_WithExpansion_ShouldExecuteExpandedProgram(String filename) throws SProgramException {
        String filePath = getXmlFilePath(filename);
        engine.loadProgram(filePath);
        
        SProgram program = engine.getCurrentProgram();
        int maxLevel = program.getMaxExpansionLevel();
        List<String> inputVars = program.getInputVariables();
        
        if (maxLevel > 0 && !inputVars.isEmpty()) {
            List<Integer> inputs = inputVars.stream()
                .map(var -> var.equals("x1") ? 5 : var.equals("x2") ? 3 : 0)
                .toList();
            
            ExecutionResult result = engine.runProgram(1, inputs);
            
            assertNotNull(result);
            assertEquals(1, result.getExpansionLevel());
            assertTrue(result.getTotalCycles() >= 0);
        }
    }
    
    @Test
    void testGetExecutionHistory_AfterMultipleRuns_ShouldTrackHistory() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        List<Integer> inputs1 = Arrays.asList(5);
        List<Integer> inputs2 = Arrays.asList(10);
        
        engine.runProgram(0, inputs1);
        engine.runProgram(0, inputs2);
        
        List<ExecutionResult> history = engine.getExecutionHistory();
        
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getRunNumber());
        assertEquals(2, history.get(1).getRunNumber());
        assertEquals(6, history.get(0).getYValue());
        assertEquals(11, history.get(1).getYValue());
    }
    
    @Test
    void testGetMaxExpansionLevel_AllPrograms_ShouldReturnCorrectLevel() throws SProgramException {
        String[] basicPrograms = {"badic.xml"};
        String[] syntheticPrograms = {"predictable-test.xml", "synthetic.xml", "successor.xml", "minus.xml"};
        
        for (String filename : basicPrograms) {
            engine.loadProgram(getXmlFilePath(filename));
            assertEquals(0, engine.getMaxExpansionLevel());
        }
        
        for (String filename : syntheticPrograms) {
            engine.loadProgram(getXmlFilePath(filename));
            assertTrue(engine.getMaxExpansionLevel() > 0);
        }
    }
    
    @Test
    void testLoadProgram_InvalidPath_ShouldThrowException() {
        assertThrows(SProgramException.class, () -> {
            engine.loadProgram("nonexistent.xml");
        });
    }
    
    @Test 
    void testLoadProgram_NullPath_ShouldThrowException() {
        assertThrows(SProgramException.class, () -> {
            engine.loadProgram(null);
        });
    }
    
    @Test
    void testLoadProgram_EmptyPath_ShouldThrowException() {
        assertThrows(SProgramException.class, () -> {
            engine.loadProgram("");
        });
    }
    
    @Test
    void testRunProgram_NoProgram_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> {
            engine.runProgram(0, Arrays.asList(1, 2, 3));
        });
    }
    
    @Test
    void testRunProgram_NullInputs_ShouldThrowException() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        assertThrows(IllegalArgumentException.class, () -> {
            engine.runProgram(0, null);
        });
    }
    
    @Test
    void testRunProgram_NegativeExpansionLevel_ShouldThrowException() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        assertThrows(IllegalArgumentException.class, () -> {
            engine.runProgram(-1, Arrays.asList(5));
        });
    }
    
    @Test
    void testRunProgram_TooHighExpansionLevel_ShouldThrowException() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        int maxLevel = engine.getMaxExpansionLevel();
        
        assertThrows(IllegalArgumentException.class, () -> {
            engine.runProgram(maxLevel + 1, Arrays.asList(5));
        });
    }
    
    @Test
    void testRunProgram_NegativeInputs_ShouldThrowException() throws SProgramException {
        String filePath = getXmlFilePath("successor.xml");
        engine.loadProgram(filePath);
        
        assertThrows(IllegalArgumentException.class, () -> {
            engine.runProgram(0, Arrays.asList(-1));
        });
    }
}
