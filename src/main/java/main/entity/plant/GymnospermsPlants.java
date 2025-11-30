package main.entity.plant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.PlantInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.air.Air;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class GymnospermsPlants extends Plant {
    private static final double BLOCKING_POSSIBILITY = 0.6;
    private static final double MATURITY_RATE_LEVEL_0 = 0.2;
    private static final double MATURITY_RATE_LEVEL_1 = 0.7;
    private static final double MATURITY_RATE_LEVEL_2 = 0.4;
    private static final double ROUND_FACTOR = 100.0;

    public GymnospermsPlants(final PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(BLOCKING_POSSIBILITY);
    }

    public GymnospermsPlants(final GymnospermsPlants other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new GymnospermsPlants(this);
    }

    @Override
    public void changeEnvironment(final int currentTime, final SimulationMap map,
                                  final int x, final int y) {
        final Air air = (Air) map.getEntityMap()[y][x][EntitySlot.AIR.idx()];
        if (air != null) {
            double maturityOxygenRate = 0.0;
            if (getMaturityLevel() == 0) {
                maturityOxygenRate = MATURITY_RATE_LEVEL_0;
            } else if (getMaturityLevel() == 1) {
                maturityOxygenRate = MATURITY_RATE_LEVEL_1;
            } else if (getMaturityLevel() == 2) {
                maturityOxygenRate = MATURITY_RATE_LEVEL_2;
            }
            air.setOxygenLevel(Math.round((air.getOxygenLevel()
                    + maturityOxygenRate) * ROUND_FACTOR) / ROUND_FACTOR);
            air.calculateAirQuality();
            air.setBlockingPossibility(air.getToxicityLevel());
        }
    }
}
