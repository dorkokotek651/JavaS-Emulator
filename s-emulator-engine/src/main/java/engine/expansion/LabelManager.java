package engine.expansion;

import engine.model.SEmulatorConstants;
import java.util.HashSet;
import java.util.Set;

public class LabelManager {
    private final Set<String> usedLabels;
    private int labelCounter;

    public LabelManager() {
        this.usedLabels = new HashSet<>();
        this.labelCounter = 1;
    }

    public LabelManager(Set<String> existingLabels) {
        this.usedLabels = new HashSet<>(existingLabels != null ? existingLabels : Set.of());
        this.labelCounter = findNextLabelCounter(this.usedLabels);
    }

    public String generateUniqueLabel() {
        String label;
        do {
            label = "L" + labelCounter++;
        } while (usedLabels.contains(label));
        
        usedLabels.add(label);
        return label;
    }

    public void markLabelAsUsed(String label) {
        if (label != null && !label.trim().isEmpty()) {
            String trimmedLabel = label.trim();
            usedLabels.add(trimmedLabel);
            
            if (SEmulatorConstants.LABEL_PATTERN.matcher(trimmedLabel).matches() && 
                trimmedLabel.startsWith("L") && !trimmedLabel.equals(SEmulatorConstants.EXIT_LABEL)) {
                try {
                    int labelNum = Integer.parseInt(trimmedLabel.substring(1));
                    if (labelNum >= labelCounter) {
                        labelCounter = labelNum + 1;
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid label format: " + trimmedLabel);
                }
            }
        }
    }

    public boolean isLabelUsed(String label) {
        return label != null && usedLabels.contains(label.trim());
    }

    public Set<String> getUsedLabels() {
        return Set.copyOf(usedLabels);
    }

    public void copyUsedLabelsFrom(LabelManager other) {
        if (other != null) {
            this.usedLabels.addAll(other.usedLabels);
            this.labelCounter = Math.max(this.labelCounter, other.labelCounter);
        }
    }

    public void reset() {
        usedLabels.clear();
        labelCounter = 1;
    }

    public int getLabelCount() {
        return usedLabels.size();
    }

    private int findNextLabelCounter(Set<String> existingLabels) {
        int maxCounter = 0;
        for (String label : existingLabels) {
            if (label.startsWith("L") && !label.equals(SEmulatorConstants.EXIT_LABEL)) {
                try {
                    int counter = Integer.parseInt(label.substring(1));
                    maxCounter = Math.max(maxCounter, counter);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid label format: " + label);
                }
            }
        }
        return maxCounter + 1;
    }
}
