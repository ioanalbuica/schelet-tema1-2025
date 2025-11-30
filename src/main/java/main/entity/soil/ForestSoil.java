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
public final class ForestSoil extends Soil {
    private double leafLitter;

    private static final double WATER_RETENTION_COEFF = 0.6;
    private static final double LEAF_LITTER_COEFF = 0.4;
    private static final double BLOCK_BASE = 80.0;

    private static final double NITROGEN_COEFF = 1.2;
    private static final double ORGANIC_COEFF = 2.0;
    private static final double WATER_QUALITY_COEFF = 1.5;
    private static final double LEAF_LITTER_QUALITY_COEFF = 0.3;

    private static final double MAX_SCORE = 100.0;
    private static final double ROUND_FACTOR = 100.0;

    public ForestSoil(final SoilInput soilInput) {
        super(soilInput);
        leafLitter = soilInput.getLeafLitter();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility((getWaterRetention() * WATER_RETENTION_COEFF
                + leafLitter * LEAF_LITTER_COEFF)
                / BLOCK_BASE * MAX_SCORE);
    }

    @Override
    public void calculateSoilQuality() {
        double score = (getNitrogen() * NITROGEN_COEFF) + (getOrganicMatter() * ORGANIC_COEFF)
                + (getWaterRetention() * WATER_QUALITY_COEFF)
                + (leafLitter * LEAF_LITTER_QUALITY_COEFF);
        double normalizeScore = Math.max(0, Math.min(MAX_SCORE, score));
        setSoilQuality(Math.round(normalizeScore * ROUND_FACTOR) / ROUND_FACTOR);
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
        soilNode.put("leafLitter", leafLitter);
        env.set("soil", soilNode);
    }
}
