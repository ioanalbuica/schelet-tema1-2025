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
public final class GrasslandSoil extends Soil {
    private double rootDensity;

    private static final double BLOCK_DENOMINATOR = 75.0;
    private static final double BLOCK_COEFF = 0.5;
    private static final double ROOT_BASE = 50.0;
    private static final double NITROGEN_COEFF = 1.3;
    private static final double ORGANIC_COEFF = 1.5;
    private static final double ROOT_COEFF = 0.8;
    private static final double MAX_SCORE = 100.0;
    private static final double ROUND_FACTOR = 100.0;

    public GrasslandSoil(final SoilInput soilInput) {
        super(soilInput);
        rootDensity = soilInput.getRootDensity();
        calculateSoilQuality();
        setBlockingPossibility(((ROOT_BASE - rootDensity)
                + getWaterRetention() * BLOCK_COEFF) / BLOCK_DENOMINATOR * MAX_SCORE);
    }

    @Override
    public void calculateSoilQuality() {
        double score = (getNitrogen() * NITROGEN_COEFF) + (getOrganicMatter() * ORGANIC_COEFF)
                + (rootDensity * ROOT_COEFF);
        double normalizeScore = Math.max(0, Math.min(MAX_SCORE, score));
        setSoilQuality(Math.round(normalizeScore * ROUND_FACTOR) / ROUND_FACTOR);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility(((ROOT_BASE - rootDensity)
                + getWaterRetention() * BLOCK_COEFF) / BLOCK_DENOMINATOR * MAX_SCORE);
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
        soilNode.put("rootDensity", rootDensity);
        env.set("soil", soilNode);
    }
}
