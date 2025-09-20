package fx.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class ButtonAnimationController {
    
    private static final Duration ANIMATION_DURATION = Duration.millis(1200);
    
    private Timeline currentAnimation;
    private Button currentAnimatedButton;
    private String originalButtonText;
    
    public ButtonAnimationController() {
        // Constructor
    }
    
    /**
     * Animates the button state transition from "Start Run" to "Running..." with a pulse effect.
     * Includes a delay to ensure users can see the animation.
     */
    public void animateButtonToRunningState(Button button) {
        if (button == null) {
            return;
        }
        
        // Check if animations are enabled
        if (!StyleManager.areAnimationsEnabled()) {
            return;
        }
        
        // Stop any existing animation
        stopCurrentAnimation();
        
        currentAnimatedButton = button;
        originalButtonText = button.getText();
        
        // Create the animation sequence
        createButtonRunningAnimation(button);
    }
    
    /**
     * Creates the button running animation with delay and pulse effects.
     */
    private void createButtonRunningAnimation(Button button) {
        Timeline animationTimeline = new Timeline();
        
        // Phase 1: Initial state change (0ms)
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO,
                event -> {
                    button.setText("Running...");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        // Phase 2: First pulse effect (200ms)
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(200),
                event -> {
                    button.getStyleClass().remove("button-running");
                    button.getStyleClass().add("button-running-pulse");
                }
            )
        );
        
        // Phase 3: Return to running state (600ms)
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(600),
                event -> {
                    button.getStyleClass().remove("button-running-pulse");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        // Phase 4: Second pulse effect (800ms)
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(800),
                event -> {
                    button.getStyleClass().remove("button-running");
                    button.getStyleClass().add("button-running-pulse");
                }
            )
        );
        
        // Phase 5: Final running state (1200ms)
        animationTimeline.getKeyFrames().add(
            new KeyFrame(ANIMATION_DURATION,
                event -> {
                    button.getStyleClass().remove("button-running-pulse");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        // Set the animation to run once
        animationTimeline.setCycleCount(1);
        
        // Store reference to current animation
        currentAnimation = animationTimeline;
        
        // Start the animation
        animationTimeline.play();
        
        // Clean up when animation finishes
        animationTimeline.setOnFinished(event -> {
            // Ensure we end with the correct CSS class
            if (button.getStyleClass().contains("button-running-pulse")) {
                button.getStyleClass().remove("button-running-pulse");
            }
            if (!button.getStyleClass().contains("button-running")) {
                button.getStyleClass().add("button-running");
            }
            currentAnimation = null;
        });
    }
    
    /**
     * Resets the button to its original state and stops any running animation.
     */
    public void resetButtonToOriginalState() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        
        if (currentAnimatedButton != null) {
            // Reset button text
            if (originalButtonText != null) {
                currentAnimatedButton.setText(originalButtonText);
            }
            
            // Clean up CSS classes
            currentAnimatedButton.getStyleClass().removeAll(
                "button-running",
                "button-running-pulse"
            );
            
            // Reset scale
            currentAnimatedButton.setScaleX(1.0);
            currentAnimatedButton.setScaleY(1.0);
            
            currentAnimatedButton = null;
            originalButtonText = null;
        }
    }
    
    /**
     * Stops the current animation without resetting the button state.
     */
    public void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }
    
    /**
     * Checks if an animation is currently running.
     */
    public boolean isAnimationRunning() {
        return currentAnimation != null && currentAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }
    
    /**
     * Gets the total duration of the button animation.
     */
    public Duration getAnimationDuration() {
        return ANIMATION_DURATION;
    }
}
