package main.entity.soil;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.SoilInput;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class SwampSoil extends Soil {
    private double waterLogging;

    private static final double NITROGEN_MULTIPLIER = 1.1;
    private static final double ORGANIC_MATTER_MULTIPLIER = 2.2;
    private static final double WATER_LOGGING_MULTIPLIER = 5;
    private static final double MAX_SCORE = 100.0;
    private static final double BLOCKING_MULTIPLIER = 10.0;

    public SwampSoil(final SoilInput soilInput) {
        super(soilInput);
        waterLogging = soilInput.getWaterLogging();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateSoilQuality() {
        double score = (getNitrogen() * NITROGEN_MULTIPLIER)
                + (getOrganicMatter() * ORGANIC_MATTER_MULTIPLIER)
                - (getWaterLogging() * WATER_LOGGING_MULTIPLIER);
        double normalizeScore = Math.max(0, Math.min(MAX_SCORE, score));
        setSoilQuality(Math.round(normalizeScore * MAX_SCORE) / MAX_SCORE);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility(waterLogging * BLOCKING_MULTIPLIER);
    }

    @Override
    public void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        ObjectNode soilNode = mapper.createObjectNode();
        soilNode.put("type", getType());
        soilNode.put("name", getName());
        soilNode.put("mass", getMass());
        soilNode.put("nitrogen", getNitrogen());
        soilNode.put("waterRetention", getWaterRetention());
        soilNode.put("soilpH", getSoilPH());
        soilNode.put("organicMatter", getOrganicMatter());
        soilNode.put("soilQuality", getSoilQuality());
        soilNode.put("waterLogging", waterLogging);
        env.set("soil", soilNode);
    }
}
