package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.WaterInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;
import static java.lang.Math.abs;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Water extends Entity{
    private String type;
    private double salinity;
    private double pH;
    private double purity;
    private double turbidity;
    private double contaminantIndex;
    private boolean isFrozen;
    private double waterQuality;

    public void calculateWaterQuality() {
        double purity_score        = purity / 100;
        double pH_score            = 1 - abs(pH - 7.5) / 7.5;
        double salinity_score      = 1 - (salinity / 350);
        double turbidity_score     = 1 - (turbidity / 100);
        double contaminant_score   = 1 - (contaminantIndex / 100);
        double frozen_score        = isFrozen ? 0 : 1;
        waterQuality = (0.3 * purity_score + 0.2 * pH_score + 0.15 * salinity_score + 0.1 * turbidity_score
                        + 0.15 * contaminant_score + 0.2 * frozen_score) * 100;
    }

    public Water(WaterInput waterInput) {
        type = waterInput.getType();
        setMass(waterInput.getMass());
        setName(waterInput.getName());
        salinity = waterInput.getSalinity();
        pH = waterInput.getPH();
        purity = waterInput.getPurity();
        turbidity = waterInput.getTurbidity();
        contaminantIndex = waterInput.getContaminantIndex();
        isFrozen = waterInput.isFrozen();
        setBlockingPossibility(0);
        calculateWaterQuality();
    }

    public Water(Water other) {
        setName(other.getName());
        setMass(other.getMass());
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
        type = other.type;
        salinity = other.salinity;
        pH = other.pH;
        purity = other.purity;
        turbidity = other.turbidity;
        contaminantIndex = other.contaminantIndex;
        isFrozen = other.isFrozen;
        waterQuality = other.waterQuality;
    }

    @Override
    public Entity createDeepCopy() {
        return new Water(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                if ((currentTime - getScannedTime()) % 2 == 0) {
                    a.setHumidity(Math.round((a.getHumidity() + 0.1) * 100.0) / 100.0);
                    a.calculateAirQuality();
                    a.setBlockingPossibility(a.getToxicityLevel());
                }
            } else if (entity instanceof Soil s) {
                if ((currentTime - getScannedTime()) % 2 == 0) {
                    s.setWaterRetention(Math.round((s.getWaterRetention() + 0.1) * 100.0) / 100.0);
                    s.calculateSoilQuality();
                    s.calculateBlockingPossibility();
                }
            } else if (entity instanceof Plant p && p.getScannedTime() > 0) {
                p.setGrowthLevel(p.getGrowthLevel() + 0.2);
                if (p.getGrowthLevel() >= 1.0) {
                    p.setGrowthLevel(0.0);
                    p.setMaturityLevel(p.getMaturityLevel() + 1);
                }
                if (p.getMaturityLevel() == 3) {
                    entitiesList.remove(entity);
                }
            }
        }
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode waterNode = MAPPER.createObjectNode();
        waterNode.put("type", getType());
        waterNode.put("name", getName());
        waterNode.put("mass", getMass());
        env.set("water", waterNode);
    }
}
