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
public final class DesertSoil extends Soil {
    private double salinity;

    private static final double NITROGEN_COEFF = 0.5;
    private static final double WATER_RETENTION_COEFF = 0.3;
    private static final double SALINITY_COEFF = 2.0;
    private static final double MAX_SCORE = 100.0;
    private static final double ROUND_FACTOR = 100.0;

    public DesertSoil(final SoilInput soilInput) {
        super(soilInput);
        salinity = soilInput.getSalinity();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateSoilQuality() {
        double score = (getNitrogen() * NITROGEN_COEFF)
                + (getWaterRetention() * WATER_RETENTION_COEFF) - (salinity * SALINITY_COEFF);
        double normalizeScore = Math.max(0, Math.min(MAX_SCORE, score));
        setSoilQuality(Math.round(normalizeScore * ROUND_FACTOR) / ROUND_FACTOR);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility((MAX_SCORE - getWaterRetention()
                + salinity) / MAX_SCORE * MAX_SCORE);
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
        soilNode.put("salinity", salinity);
        env.set("soil", soilNode);
    }
}
