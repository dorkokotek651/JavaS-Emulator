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
    }

    public void animateButtonToRunningState(Button button) {
        if (button == null) {
            return;
        }
        
        if (!StyleManager.areAnimationsEnabled()) {
            return;
        }
        
        stopCurrentAnimation();
        
        currentAnimatedButton = button;
        originalButtonText = button.getText();
        
        createButtonRunningAnimation(button);
    }

    private void createButtonRunningAnimation(Button button) {
        Timeline animationTimeline = new Timeline();
        
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO,
                event -> {
                    button.setText("Running...");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(200),
                event -> {
                    button.getStyleClass().remove("button-running");
                    button.getStyleClass().add("button-running-pulse");
                }
            )
        );
        
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(600),
                event -> {
                    button.getStyleClass().remove("button-running-pulse");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        animationTimeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(800),
                event -> {
                    button.getStyleClass().remove("button-running");
                    button.getStyleClass().add("button-running-pulse");
                }
            )
        );
        
        animationTimeline.getKeyFrames().add(
            new KeyFrame(ANIMATION_DURATION,
                event -> {
                    button.getStyleClass().remove("button-running-pulse");
                    button.getStyleClass().add("button-running");
                }
            )
        );
        
        animationTimeline.setCycleCount(1);
        
        currentAnimation = animationTimeline;
        
        animationTimeline.play();
        
        animationTimeline.setOnFinished(event -> {
            if (button.getStyleClass().contains("button-running-pulse")) {
                button.getStyleClass().remove("button-running-pulse");
            }
            if (!button.getStyleClass().contains("button-running")) {
                button.getStyleClass().add("button-running");
            }
            currentAnimation = null;
        });
    }

    public void resetButtonToOriginalState() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        
        if (currentAnimatedButton != null) {
            if (originalButtonText != null) {
                currentAnimatedButton.setText(originalButtonText);
            }
            
            currentAnimatedButton.getStyleClass().removeAll(
                "button-running",
                "button-running-pulse"
            );
            
            currentAnimatedButton.setScaleX(1.0);
            currentAnimatedButton.setScaleY(1.0);
            
            currentAnimatedButton = null;
            originalButtonText = null;
        }
    }

    public void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }

    public boolean isAnimationRunning() {
        return currentAnimation != null && currentAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }

    public Duration getAnimationDuration() {
        return ANIMATION_DURATION;
    }
}
