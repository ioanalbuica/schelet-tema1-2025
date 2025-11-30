package main.entity.animal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.AnimalInput;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.plant.Plant;
import main.entity.soil.Soil;
import main.entity.water.Water;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Animal extends Entity {
    private String type;
    private String state;
    private double nextOrganicMatter = 0.0;
    private int lastMovedTime = 0;
    private boolean hasEaten = false;
    private static final double INTAKE_RATE = 0.08;
    private static final double PLANT_WATER_ORGANICMATTER = 0.8;
    private static final double WATER_ORGANICMATTER = 0.5;
    private static final double PLANT_ORGANICMATTER = 0.8;
    private static final double NUMBER_OF_NEIGHBOURS = 4;

    public Animal(final AnimalInput animalInput) {
        type = animalInput.getType();
        setName(animalInput.getName());
        setMass(animalInput.getMass());
    }

    public Animal(final Animal other) {
        setName(other.getName());
        setMass(other.getMass());
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
        type = other.getType();
        state = other.getState();
        nextOrganicMatter = other.getNextOrganicMatter();
    }

    @Override
    public final void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        ObjectNode animalNode = mapper.createObjectNode();
        animalNode.put("type", getType());
        animalNode.put("name", getName());
        animalNode.put("mass", getMass());
        env.set("animals", animalNode);
    }

    @Override
    public final void changeEnvironment(final int currentTime, final SimulationMap map,
                                        final int x, final int y) {
        if (nextOrganicMatter > 0) {
            Soil s = (Soil) map.getEntityMap()[y][x][EntitySlot.SOIL.idx()];
            if (s != null) {
                s.setOrganicMatter(s.getOrganicMatter() + nextOrganicMatter);
                s.calculateSoilQuality();
                s.calculateBlockingPossibility();
            }
            nextOrganicMatter = 0.0;
        }
    }

    private boolean isCarnivoreOrParasite() {
        return "Carnivores".equals(type) || "Parasites".equals(type);
    }


    public void move(final int currentTime, final SimulationMap map,
                     final int x, final int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if ((currentTime - a.getLastMovedTime()) % 2 != 0
                || currentTime == a.lastMovedTime) {
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

            boolean destHasAnimal = grid[ny][nx][EntitySlot.ANIMAL.idx()] != null;
            if (destHasAnimal && !isCarnivoreOrParasite()) {
                continue;
            }

            Water w = (Water) grid[ny][nx][EntitySlot.WATER.idx()];
            Plant p = (Plant) grid[ny][nx][EntitySlot.PLANT.idx()];
            boolean hasPlant = (p != null && p.getScannedTime() > 0);
            boolean hasWater = (w != null && w.getScannedTime() > 0);
            double currentWaterQuality = 0.0;
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
            grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        if (bestP != null) {
            grid[bestP[1]][bestP[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        if (bestW != null) {
            grid[bestW[1]][bestW[0]][EntitySlot.ANIMAL.idx()] = this;
            grid[y][x][EntitySlot.ANIMAL.idx()] = null;
            return;
        }
        grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = this;
        grid[y][x][EntitySlot.ANIMAL.idx()] = null;
    }

    public final void feed(final SimulationMap map, final int x, final int y) {
        if (hasEaten) {
            hasEaten = false;
            return;
        }

        Plant p = (Plant) map.getEntityMap()[y][x][EntitySlot.PLANT.idx()];
        Water w = (Water) map.getEntityMap()[y][x][EntitySlot.WATER.idx()];
        int scannedPlant = 0, scannedWater = 0;
        if (p != null && p.getScannedTime() > 0) {
            scannedPlant = p.getScannedTime();
        }
        if (w != null && w.getScannedTime() > 0) {
            scannedWater = w.getScannedTime();
        }

        if (scannedPlant > 0 && scannedWater > 0) {
            double plantMass = p.getMass();
            map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
            double waterToDrink = Math.min(getMass() * INTAKE_RATE, w.getMass());
            w.setMass(w.getMass() - waterToDrink);
            if (w.getMass() <= 0) {
                map.getEntityMap()[y][x][EntitySlot.WATER.idx()] = null;
            }
            setMass(getMass() + waterToDrink + plantMass);
            nextOrganicMatter = PLANT_WATER_ORGANICMATTER;
            setState("well-fed");
        } else if (scannedPlant > 0 || scannedWater > 0) {
            if (scannedWater >= scannedPlant) {
                double waterToDrink = Math.min(getMass() * INTAKE_RATE, w.getMass());
                w.setMass(w.getMass() - waterToDrink);
                if (w.getMass() <= 0) {
                    map.getEntityMap()[y][x][EntitySlot.WATER.idx()] = null;
                }
                setMass(getMass() + waterToDrink);
                nextOrganicMatter = WATER_ORGANICMATTER;
                setState("well-fed");
            } else {
                double plantMass = p.getMass();
                map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
                setMass(getMass() + plantMass);
                nextOrganicMatter = PLANT_ORGANICMATTER;
                setState("well-fed");
            }
        } else {
            setState("hungry");
        }
    }
}
