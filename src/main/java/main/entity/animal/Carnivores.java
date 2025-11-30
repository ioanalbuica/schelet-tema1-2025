package main.entity.animal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.plant.Plant;
import main.entity.water.Water;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Carnivores extends Animal {
    private static final double NUMBER_OF_NEIGHBOURS = 4;
    private static final double EATEN_PREY_ORGANICMATTER = 0.5;
    private static final double CARNIVORE_ATTACKING_PROBABILITY = 7;

    public Carnivores(final AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(CARNIVORE_ATTACKING_PROBABILITY);
    }

    public Carnivores(final Carnivores other) {
        super(other);
    }

    @Override
    public final Entity createDeepCopy() {
        return new Carnivores(this);
    }

    @Override
    public void move(final int currentTime, final SimulationMap map,
                           final int x, final int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if ((currentTime - a.getLastMovedTime()) % 2 != 0 || currentTime == a.getLastMovedTime()) {
            return;
        }

        a.setLastMovedTime(currentTime);
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int[] bestPW = null, bestP = null, bestW = null, bestAny = null;
        double maxQwPW = -1.0, maxQwW = -1.0;

        Entity[][][] grid = map.getEntityMap();
        for (int k = 0; k < NUMBER_OF_NEIGHBOURS; k++) {
            int nx = x + dx[k];
            int ny = y + dy[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            Water w = (Water) grid[ny][nx][EntitySlot.WATER.idx()];
            Plant p = (Plant) grid[ny][nx][EntitySlot.PLANT.idx()];
            boolean hasPlant = (p != null && p.getScannedTime() > 0);
            boolean hasWater = (w != null && w.getScannedTime() > 0);
            double currentWaterQuality = -1.0;
            if (hasWater) {
                w.calculateWaterQuality();
                currentWaterQuality = w.getWaterQuality();
            }

            if (hasPlant && hasWater) {
                if (currentWaterQuality > maxQwPW) {
                    maxQwPW = currentWaterQuality;
                    bestPW = new int[]{nx, ny};
                }
            } else if (hasPlant) {
                if (bestP == null) {
                    bestP = new int[]{nx, ny};
                }
            } else if (hasWater) {
                if (currentWaterQuality > maxQwW) {
                    maxQwW = currentWaterQuality;
                    bestW = new int[]{nx, ny};
                }
            } else {
                if (bestAny == null) {
                    bestAny = new int[]{nx, ny};
                }
            }
        }

        if (bestPW != null) {
            Animal prey = (Animal) grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()];
            if (prey != null) {
                setMass(getMass() + prey.getMass());
                setNextOrganicMatter(EATEN_PREY_ORGANICMATTER);
                grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()] = null;
                setState("well-fed");
                setHasEaten(true);
            }
            grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        if (bestP != null) {
            Animal prey = (Animal) grid[bestP[1]][bestP[0]][EntitySlot.ANIMAL.idx()];
            if (prey != null) {
                setMass(getMass() + prey.getMass());
                setNextOrganicMatter(EATEN_PREY_ORGANICMATTER);
                grid[bestP[1]][bestP[0]][EntitySlot.ANIMAL.idx()] = null;
                setState("well-fed");
                setHasEaten(true);
            }
            grid[bestP[1]][bestP[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        if (bestW != null) {
            Animal prey = (Animal) grid[bestW[1]][bestW[0]][EntitySlot.ANIMAL.idx()];
            if (prey != null) {
                setMass(getMass() + prey.getMass());
                setNextOrganicMatter(EATEN_PREY_ORGANICMATTER);
                grid[bestW[1]][bestW[0]][EntitySlot.ANIMAL.idx()] = null;
                setState("well-fed");
                setHasEaten(true);
            }
            grid[bestW[1]][bestW[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        Animal prey = (Animal) grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()];
        if (prey != null) {
            setMass(getMass() + prey.getMass());
            setNextOrganicMatter(EATEN_PREY_ORGANICMATTER);
            grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = null;
            setState("well-fed");
            setHasEaten(true);
        }
        grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = this;
        grid[y][x][EntitySlot.ANIMAL.idx()] = null;
    }
}
