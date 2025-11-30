package main.entity.soil;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.SoilInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.plant.Plant;

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

    private static final double GROWTH_INCREMENT = 0.2;
    private static final int MATURITY_CAP = 3;

    public Soil(final SoilInput soilInput) {
        type = soilInput.getType();
        this.setName(soilInput.getName());
        this.setMass(soilInput.getMass());
        nitrogen = soilInput.getNitrogen();
        waterRetention = soilInput.getWaterRetention();
        soilPH = soilInput.getSoilpH();
        organicMatter = soilInput.getOrganicMatter();
    }

    @Override
    public final void changeEnvironment(final int currentTime, final SimulationMap map,
                                  final int x, final int y) {
        Plant p = (Plant) map.getEntityMap()[y][x][EntitySlot.PLANT.idx()];
        if (p != null && p.getScannedTime() > 0) {
            p.setGrowthLevel(p.getGrowthLevel() + GROWTH_INCREMENT);
            if (p.getGrowthLevel() >= 1.0) {
                p.setGrowthLevel(0.0);
                p.setMaturityLevel(p.getMaturityLevel() + 1);
            }
            if (p.getMaturityLevel() >= MATURITY_CAP) {
                map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
            }
        }
    }

    /**
     * nu creem deep-copy pt soil ca nu se poate baga in inventar
     * @return
     */
    @Override
    public final Entity createDeepCopy() {
        return null;
    }

    public abstract void calculateSoilQuality();
    public abstract void calculateBlockingPossibility();
}
