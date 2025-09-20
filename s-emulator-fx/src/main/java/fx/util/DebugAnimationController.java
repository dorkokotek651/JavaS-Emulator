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
    }

    public void animateInstructionHighlight(TableView<?> table, int instructionIndex) {
        if (table == null || instructionIndex < 0 || instructionIndex >= table.getItems().size()) {
            return;
        }
        
        if (!StyleManager.areAnimationsEnabled()) {
            return;
        }
        
        stopCurrentAnimation();
        
        TableRow<?> row = getTableRow(table, instructionIndex);
        if (row == null) {
            return;
        }
        
        currentAnimatedRow = row;
        
        createPulseAnimation(row);
    }

    private void createPulseAnimation(TableRow<?> row) {
        row.getStyleClass().add("debug-current-instruction-animated");
        
        Timeline pulseTimeline = new Timeline();
        
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO,
                new KeyValue(row.scaleXProperty(), 1.02),
                new KeyValue(row.scaleYProperty(), 1.02)
            )
        );
        
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(400),
                new KeyValue(row.scaleXProperty(), 1.05),
                new KeyValue(row.scaleYProperty(), 1.05)
            )
        );
        
        pulseTimeline.getKeyFrames().add(
            new KeyFrame(PULSE_DURATION,
                new KeyValue(row.scaleXProperty(), 1.02),
                new KeyValue(row.scaleYProperty(), 1.02)
            )
        );
        
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
        
        pulseTimeline.setCycleCount(1);
        
        currentAnimation = pulseTimeline;
        
        pulseTimeline.play();
        
        pulseTimeline.setOnFinished(event -> {
            if (row.getStyleClass().contains("debug-pulse-effect")) {
                row.getStyleClass().remove("debug-pulse-effect");
            }
            if (!row.getStyleClass().contains("debug-current-instruction-animated")) {
                row.getStyleClass().add("debug-current-instruction-animated");
            }
            currentAnimation = null;
        });
    }

    public void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        
        if (currentAnimatedRow != null) {
            currentAnimatedRow.getStyleClass().removeAll(
                "debug-current-instruction-animated",
                "debug-pulse-effect"
            );
            
            currentAnimatedRow.setScaleX(1.0);
            currentAnimatedRow.setScaleY(1.0);
            
            currentAnimatedRow = null;
        }
    }

    public void clearDebugHighlighting(TableView<?> table) {
        stopCurrentAnimation();
        
        if (table != null) {
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

    private TableRow<?> getTableRow(TableView<?> table, int index) {
        if (table == null || index < 0) {
            return null;
        }
        
        table.scrollTo(index);
        
        table.layout();
        
        for (javafx.scene.Node node : table.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow) {
                TableRow<?> row = (TableRow<?>) node;
                if (row.getIndex() == index) {
                    return row;
                }
            }
        }
        
        return null;
    }

    public boolean isAnimationRunning() {
        return currentAnimation != null && currentAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }
}
