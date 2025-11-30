package main.entity.water;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.WaterInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.air.Air;
import main.entity.plant.Plant;
import main.entity.soil.Soil;
import static java.lang.Math.abs;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class Water extends Entity {
    private static final double MAX_PERCENT = 100.0;
    private static final double IDEAL_PH = 7.5;
    private static final double MAX_SALINITY = 350.0;
    private static final double HUMIDITY_INCREMENT = 0.1;
    private static final double WATER_RETENTION_INCREMENT = 0.1;
    private static final double PLANT_GROWTH_INCREMENT = 0.2;
    private static final int MAX_PLANT_MATURITY = 3;

    private String type;
    private double salinity;
    private double pH;
    private double purity;
    private double turbidity;
    private double contaminantIndex;
    private boolean isFrozen;
    private double waterQuality;

    public void calculateWaterQuality() {
        final double purityMultiplier = 0.3;
        final double pHMultiplier = 0.2;
        final double salinityMultiplier = 0.15;
        final double turbidityMultiplier = 0.1;
        final double contaminantMultiplier = 0.15;
        final double frozenMultiplier = 0.2;

        double purityScore      = purity / MAX_PERCENT;
        double pHScore          = 1 - abs(pH - IDEAL_PH) / IDEAL_PH;
        double salinityScore    = 1 - (salinity / MAX_SALINITY);
        double turbidityScore   = 1 - (turbidity / MAX_PERCENT);
        double contaminantScore = 1 - (contaminantIndex / MAX_PERCENT);
        double frozenScore      = isFrozen ? 0 : 1;

        waterQuality = (purityMultiplier * purityScore + pHMultiplier * pHScore
                + salinityMultiplier * salinityScore + turbidityMultiplier * turbidityScore
                + contaminantMultiplier * contaminantScore
                + frozenMultiplier * frozenScore) * MAX_PERCENT;
    }

    public Water(final WaterInput waterInput) {
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

    public Water(final Water other) {
        setName(other.getName());
        setMass(other.getMass());
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
        type = other.getType();
        salinity = other.getSalinity();
        pH = other.getPH();
        purity = other.getPurity();
        turbidity = other.getTurbidity();
        contaminantIndex = other.getContaminantIndex();
        isFrozen = other.isFrozen();
        waterQuality = other.getWaterQuality();
    }

    @Override
    public Entity createDeepCopy() {
        return new Water(this);
    }

    @Override
    public void changeEnvironment(final int currentTime, final SimulationMap map,
                                  final int x, final int y) {
        final Air air = (Air) map.getEntityMap()[y][x][EntitySlot.AIR.idx()];
        /**
         * recalculam mereu calitatea aerului
         */
        if (air != null && (currentTime - getScannedTime()) % 2 == 0) {
            air.setHumidity(Math.round((air.getHumidity()
                    + HUMIDITY_INCREMENT) * MAX_PERCENT) / MAX_PERCENT);
            air.calculateAirQuality();
            air.setBlockingPossibility(air.getToxicityLevel());
        }

        /**
         * recalculam mereu calitatea solului
         */
        final Soil soil = (Soil) map.getEntityMap()[y][x][EntitySlot.SOIL.idx()];
        if (soil != null && (currentTime - getScannedTime()) % 2 == 0) {
            soil.setWaterRetention(Math.round((soil.getWaterRetention()
                    + WATER_RETENTION_INCREMENT) * MAX_PERCENT) / MAX_PERCENT);
            soil.calculateSoilQuality();
            soil.calculateBlockingPossibility();
        }

        final Plant plant = (Plant) map.getEntityMap()[y][x][EntitySlot.PLANT.idx()];
        if (plant != null && plant.getScannedTime() > 0) {
            plant.setGrowthLevel(plant.getGrowthLevel() + PLANT_GROWTH_INCREMENT);
            if (plant.getGrowthLevel() >= 1.0) {
                plant.setGrowthLevel(0.0);
                plant.setMaturityLevel(plant.getMaturityLevel() + 1);
            }
            if (plant.getMaturityLevel() == MAX_PLANT_MATURITY) {
                map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
            }
        }
    }

    @Override
    public void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        final ObjectNode waterNode = mapper.createObjectNode();
        waterNode.put("type", getType());
        waterNode.put("name", getName());
        waterNode.put("mass", getMass());
        env.set("water", waterNode);
    }
}
