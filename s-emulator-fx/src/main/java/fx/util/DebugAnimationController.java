package fx.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Duration;

public class DebugAnimationController {
    
    private static final Duration PULSE_DURATION = Duration.millis(800);
    
    private Timeline currentAnimation;
    private TableRow<?> currentAnimatedRow;
    
    public DebugAnimationController() {
        // Constructor
    }
    
    /**
     * Animates the transition to a new instruction during debug execution.
     * Creates a smooth pulse effect that draws attention to the current instruction.
     */
    public void animateInstructionHighlight(TableView<?> table, int instructionIndex) {
        if (table == null || instructionIndex < 0 || instructionIndex >= table.getItems().size()) {
            return;
        }
        
        // Check if animations are enabled
        if (!StyleManager.areAnimationsEnabled()) {
            return;
        }
        
        // Stop any existing animation
        stopCurrentAnimation();
        
        // Get the table row for the instruction
        TableRow<?> row = getTableRow(table, instructionIndex);
        if (row == null) {
            return;
        }
        
        currentAnimatedRow = row;
        
        // Create a pulse animation
        createPulseAnimation(row);
    }
    
    /**
     * Creates a smooth pulse animation for the instruction row.
     */
    private void createPulseAnimation(TableRow<?> row) {
        // Start with the animated state
        row.getStyleClass().add("debug-current-instruction-animated");
        
        // Create timeline for pulse effect
        Timeline pulseTimeline = new Timeline();
        
        // KeyFrame 1: Start with normal animated state (0ms)
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO,
                new KeyValue(row.scaleXProperty(), 1.02),
                new KeyValue(row.scaleYProperty(), 1.02)
            )
        );
        
        // KeyFrame 2: Pulse to larger size (400ms)
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(400),
                new KeyValue(row.scaleXProperty(), 1.05),
                new KeyValue(row.scaleYProperty(), 1.05)
            )
        );
        
        // KeyFrame 3: Return to normal animated size (800ms)
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(PULSE_DURATION,
                new KeyValue(row.scaleXProperty(), 1.02),
                new KeyValue(row.scaleYProperty(), 1.02)
            )
        );
        
        // Add CSS class changes for enhanced visual effect
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(200),
                event -> {
                    row.getStyleClass().remove("debug-current-instruction-animated");
                    row.getStyleClass().add("debug-pulse-effect");
                }
            )
        );
        
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(600),
                event -> {
                    row.getStyleClass().remove("debug-pulse-effect");
                    row.getStyleClass().add("debug-current-instruction-animated");
                }
            )
        );
        
        // Set the animation to run once
        pulseTimeline.setCycleCount(1);
        
        // Store reference to current animation
        currentAnimation = pulseTimeline;
        
        // Start the animation
        pulseTimeline.play();
        
        // Clean up when animation finishes
        pulseTimeline.setOnFinished(event -> {
            // Ensure we end with the correct CSS class
            if (row.getStyleClass().contains("debug-pulse-effect")) {
                row.getStyleClass().remove("debug-pulse-effect");
            }
            if (!row.getStyleClass().contains("debug-current-instruction-animated")) {
                row.getStyleClass().add("debug-current-instruction-animated");
            }
            currentAnimation = null;
        });
    }
    
    /**
     * Stops the current animation and cleans up the row styling.
     */
    public void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        
        if (currentAnimatedRow != null) {
            // Clean up CSS classes
            currentAnimatedRow.getStyleClass().removeAll(
                "debug-current-instruction-animated",
                "debug-pulse-effect"
            );
            
            // Reset scale
            currentAnimatedRow.setScaleX(1.0);
            currentAnimatedRow.setScaleY(1.0);
            
            currentAnimatedRow = null;
        }
    }
    
    /**
     * Clears all debug highlighting and stops any running animations.
     */
    public void clearDebugHighlighting(TableView<?> table) {
        stopCurrentAnimation();
        
        if (table != null) {
            // Clear all debug-related CSS classes from all rows
            for (int i = 0; i < table.getItems().size(); i++) {
                TableRow<?> row = getTableRow(table, i);
                if (row != null) {
                    row.getStyleClass().removeAll(
                        "debug-current-instruction",
                        "debug-current-instruction-animated",
                        "debug-pulse-effect"
                    );
                    row.setScaleX(1.0);
                    row.setScaleY(1.0);
                }
            }
        }
    }
    
    /**
     * Gets the TableRow for a specific index in the table.
     */
    private TableRow<?> getTableRow(TableView<?> table, int index) {
        if (table == null || index < 0) {
            return null;
        }
        
        // Scroll to the row to ensure it's rendered
        table.scrollTo(index);
        
        // Force a layout pass to ensure the row is rendered
        table.layout();
        
        // Look for the specific row by index
        // We'll use a more reliable method by looking for the row in the table's children
        for (javafx.scene.Node node : table.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow) {
                TableRow<?> row = (TableRow<?>) node;
                if (row.getIndex() == index) {
                    return row;
                }
            }
        }
        
        // Fallback: return null if we can't find the specific row
        return null;
    }
    
    /**
     * Checks if an animation is currently running.
     */
    public boolean isAnimationRunning() {
        return currentAnimation != null && currentAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }
}
