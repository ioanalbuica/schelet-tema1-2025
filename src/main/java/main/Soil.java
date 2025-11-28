package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.SoilInput;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Soil extends Entity {
    private String type;
    private double nitrogen;
    private double waterRetention;
    private double soilPH;
    private double organicMatter;
    private double soilQuality;

    public Soil(SoilInput soilInput) {
        type = soilInput.getType();
        this.setName(soilInput.getName());
        this.setMass(soilInput.getMass());
        nitrogen = soilInput.getNitrogen();
        waterRetention = soilInput.getWaterRetention();
        soilPH = soilInput.getSoilpH();
        organicMatter = soilInput.getOrganicMatter();
    }

    @Override
    public void changeEnvironment(int currentTime, SimulationMap map, int x, int y) {
        Plant p = (Plant)map.getEntityMap()[y][x][EntitySlot.PLANT.idx()];
        if (p != null && p.getScannedTime() > 0) {
            p.setGrowthLevel(p.getGrowthLevel() + 0.2);
            if (p.getGrowthLevel() >= 1.0) {
                p.setGrowthLevel(0.0);
                p.setMaturityLevel(p.getMaturityLevel() + 1);
            }
            if (p.getMaturityLevel() == 3) {
                map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
            }
        }
    }

    @Override
    public Entity createDeepCopy() {
        return null;
    }

    abstract public void calculateSoilQuality();
    abstract public void calculateBlockingPossibility();

}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ForestSoil extends Soil {
    private double leafLitter;

    public ForestSoil(SoilInput soilInput) {
        super(soilInput);
        leafLitter = soilInput.getLeafLitter();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility((getWaterRetention() * 0.6 + leafLitter * 0.4) / 80 * 100);
    }

    @Override
    public void calculateSoilQuality() {
        double score = (this.getNitrogen() * 1.2) + (this.getOrganicMatter() * 2) + (this.getWaterRetention() * 1.5) + (this.getLeafLitter() * 0.3);
        double normalizeScore = Math.max(0, Math.min(100, score));
        this.setSoilQuality(Math.round(normalizeScore * 100.0) / 100.0);
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode soilNode = MAPPER.createObjectNode();
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

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SwampSoil extends Soil {
    private double waterLogging;

    public SwampSoil(SoilInput soilInput) {
        super(soilInput);
        waterLogging = soilInput.getWaterLogging();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateSoilQuality() {
        double score = (this.getNitrogen()* 1.1) + (this.getOrganicMatter() * 2.2) - (this.getWaterLogging() * 5);
        double normalizeScore = Math.max(0, Math.min(100, score));
        this.setSoilQuality(Math.round(normalizeScore * 100.0) / 100.0);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility(waterLogging * 10);
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode soilNode = MAPPER.createObjectNode();
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

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class DesertSoil extends Soil {
    private double salinity;

    public DesertSoil(SoilInput soilInput) {
        super(soilInput);
        salinity = soilInput.getSalinity();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateSoilQuality() {
        double score = (this.getNitrogen() * 0.5) + (this.getWaterRetention() * 0.3) - (salinity * 2);
        double normalizeScore = Math.max(0, Math.min(100, score));
        this.setSoilQuality(Math.round(normalizeScore * 100.0) / 100.0);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility((100 - getWaterRetention() + salinity) / 100 * 100);
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode soilNode = MAPPER.createObjectNode();
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

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GrasslandSoil extends Soil {
    private double rootDensity;

    public GrasslandSoil(SoilInput soilInput) {
        super(soilInput);
        rootDensity = soilInput.getRootDensity();
        calculateSoilQuality();
        setBlockingPossibility(((50 - rootDensity) + getWaterRetention() * 0.5) / 75 * 100);
    }


    @Override
    public void calculateSoilQuality() {
        double score = (this.getNitrogen() * 1.3) + (this.getOrganicMatter() * 1.5) + (rootDensity * 0.8);
        double normalizeScore = Math.max(0, Math.min(100, score));
        this.setSoilQuality(Math.round(normalizeScore * 100.0) / 100.0);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility(((50 - rootDensity) + getWaterRetention() * 0.5) / 75 * 100);
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode soilNode = MAPPER.createObjectNode();
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

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TundraSoil extends Soil {
    private double permafrostDepth;

    public TundraSoil(SoilInput soilInput) {
        super(soilInput);
        permafrostDepth = soilInput.getPermafrostDepth();
        calculateSoilQuality();
        calculateBlockingPossibility();
    }

    @Override
    public void calculateSoilQuality() {
        double score = 	(this.getNitrogen() * 0.7) + (this.getOrganicMatter() * 0.5) - (permafrostDepth * 1.5);
        double normalizeScore = Math.max(0, Math.min(100, score));
        this.setSoilQuality(Math.round(normalizeScore * 100.0) / 100.0);
    }

    @Override
    public void calculateBlockingPossibility() {
        setBlockingPossibility((50 - permafrostDepth) / 50 * 100);
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode soilNode = MAPPER.createObjectNode();
        soilNode.put("type", getType());
        soilNode.put("name", getName());
        soilNode.put("mass", getMass());
        soilNode.put("nitrogen", getNitrogen());
        soilNode.put("waterRetention", getWaterRetention());
        soilNode.put("soilpH", getSoilPH());
        soilNode.put("organicMatter", getOrganicMatter());
        soilNode.put("soilQuality", getSoilQuality());
        soilNode.put("permafrostDepth", permafrostDepth);
        env.set("soil", soilNode);
    }
}





