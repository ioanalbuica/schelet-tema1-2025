package main;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Animal extends Entity {
    private String type;
    private String state;
    private double nextOrganicMatter = 0.0;
    private int lastMovedTime = 0;
    private boolean hasEaten = false;

    public Animal(AnimalInput animalInput) {
        type = animalInput.getType();
        setName(animalInput.getName());
        setMass(animalInput.getMass());
    }

    public Animal(Animal other) {
        setName(other.getName());
        setMass(other.getMass());
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
        type = other.type;
        state = other.state;
        nextOrganicMatter = other.nextOrganicMatter;
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode animalNode = MAPPER.createObjectNode();
        animalNode.put("type", getType());
        animalNode.put("name", getName());
        animalNode.put("mass", getMass());
        env.set("animals", animalNode);
    }

    @Override
    public void changeEnvironment(int currentTime, SimulationMap map, int x, int y) {
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

    public void move(int currentTime, SimulationMap map, int x, int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if ((currentTime - a.getLastMovedTime()) % 2 != 0 ||
                a.getScannedTime() == 0) {
            return;
        }

        a.setLastMovedTime(currentTime);
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int[] bestPW = null, bestP = null, bestW = null, bestAny = null;
        double maxQwPW = -1.0, maxQwW = -1.0;

        Entity[][][] grid = map.getEntityMap();
        for (int k = 0; k < 4; k++) {
            int nx = x + dx[k];
            int ny = y + dy[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            boolean destHasAnimal = grid[ny][nx][EntitySlot.ANIMAL.idx()] != null;
            if (destHasAnimal && !isCarnivoreOrParasite()) {
                continue;
            }

            Water w = (Water)grid[ny][nx][EntitySlot.WATER.idx()];
            Plant p = (Plant)grid[ny][nx][EntitySlot.PLANT.idx()];
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
                if (bestP == null) bestP = new int[]{nx, ny};
            } else if (hasWater) {
                if (currentWaterQuality > maxQwW) {
                    maxQwW = currentWaterQuality;
                    bestW = new int[]{nx, ny};
                }
            } else {
                if (bestAny == null) bestAny = new int[]{nx, ny};
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

    public void feed(int currentTime, SimulationMap map, int x, int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if (a.getScannedTime() == 0) {
            return;
        }

        if (hasEaten) {
            hasEaten = false;
            return;
        }

        Plant p = (Plant)map.getEntityMap()[y][x][EntitySlot.PLANT.idx()];
        Water w = (Water)map.getEntityMap()[y][x][EntitySlot.WATER.idx()];
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
            double intakeRate = 0.08;
            double waterToDrink = Math.min(getMass() * intakeRate, w.getMass());
            w.setMass(w.getMass() - waterToDrink);
            if (w.getMass() <= 0) {
                map.getEntityMap()[y][x][EntitySlot.WATER.idx()] = null;
            }
            setMass(getMass() + waterToDrink + plantMass);
            nextOrganicMatter = 0.8;
            setState("well-fed");
        } else if (scannedPlant > 0 || scannedWater > 0) {
            if (scannedWater >= scannedPlant) {
                double intakeRate = 0.08;
                double waterToDrink = Math.min(getMass() * intakeRate, w.getMass());
                w.setMass(w.getMass() - waterToDrink);
                if (w.getMass() <= 0) {
                    map.getEntityMap()[y][x][EntitySlot.WATER.idx()] = null;
                }
                setMass(getMass() + waterToDrink);
                nextOrganicMatter = 0.5;
                setState("well-fed");
            } else {
                double plantMass = p.getMass();
                map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] = null;
                setMass(getMass() + plantMass);
                nextOrganicMatter = 0.5;
                setState("well-fed");
            }
        } else {
            setState("hungry");
        }
    }
}


@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Herbivores extends Animal {
    public Herbivores(AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(1.5);
    }
    public Herbivores(Herbivores other) {
        super(other);
    }

    @Override public Entity createDeepCopy() {
        return new Herbivores(this);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Carnivores extends Animal {
    public Carnivores(AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(7);
    }

    public Carnivores(Carnivores other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Carnivores(this);
    }

    @Override
    public void move(int currentTime, SimulationMap map, int x, int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if ((currentTime - a.getLastMovedTime()) % 2 != 0 ||
                a.getScannedTime() == 0) {
            return;
        }

        a.setLastMovedTime(currentTime);
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int[] bestPW = null, bestP = null, bestW = null, bestAny = null;
        double maxQwPW = -1.0, maxQwW = -1.0;

        Entity[][][] grid = map.getEntityMap();
        for (int k = 0; k < 4; k++) {
            int nx = x + dx[k];
            int ny = y + dy[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            Water w = (Water)grid[ny][nx][EntitySlot.WATER.idx()];
            Plant p = (Plant)grid[ny][nx][EntitySlot.PLANT.idx()];
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
                if (bestP == null) bestP = new int[]{nx, ny};
            } else if (hasWater) {
                if (currentWaterQuality > maxQwW) {
                    maxQwW = currentWaterQuality;
                    bestW = new int[]{nx, ny};
                }
            } else {
                if (bestAny == null) bestAny = new int[]{nx, ny};
            }
        }

        if (bestPW != null) {
            Animal prey = (Animal) grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()];
            if (prey != null) {
                setMass(getMass() + prey.getMass());
                setNextOrganicMatter(0.5);
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
                setNextOrganicMatter(0.5);
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
                setNextOrganicMatter(0.5);
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
            setNextOrganicMatter(0.5);
            grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = null;
            setState("well-fed");
            setHasEaten(true);
        }
        grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = this;
        grid[y][x][EntitySlot.ANIMAL.idx()] = null;
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Omnivores extends Animal {
    public Omnivores(AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(4);
    }

    public Omnivores(Omnivores other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Omnivores(this);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Detritivores extends Animal {
    public Detritivores(AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(1);
    }

    public Detritivores(Detritivores other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Detritivores(this);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Parasites extends Animal {
    public Parasites(AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(9);
    }

    public Parasites(Parasites other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Parasites(this);
    }

    @Override
    public void move(int currentTime, SimulationMap map, int x, int y) {
        Animal a = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if ((currentTime - a.getLastMovedTime()) % 2 != 0 ||
                a.getScannedTime() == 0) {
            return;
        }

        a.setLastMovedTime(currentTime);
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int[] bestPW = null, bestP = null, bestW = null, bestAny = null;
        double maxQwPW = -1.0, maxQwW = -1.0;

        Entity[][][] grid = map.getEntityMap();
        for (int k = 0; k < 4; k++) {
            int nx = x + dx[k];
            int ny = y + dy[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            boolean hasPlant = grid[ny][nx][EntitySlot.PLANT.idx()] != null;
            boolean hasWater = grid[ny][nx][EntitySlot.WATER.idx()] != null;
            double currentWaterQuality = -1.0;
            if (hasWater) {
                Water w = (Water) grid[ny][nx][EntitySlot.WATER.idx()];
                w.calculateWaterQuality();
                currentWaterQuality = w.getWaterQuality();
            }

            if (hasPlant && hasWater) {
                if (currentWaterQuality > maxQwPW) {
                    maxQwPW = currentWaterQuality;
                    bestPW = new int[]{nx, ny};
                }
            } else if (hasPlant) {
                if (bestP == null) bestP = new int[]{nx, ny};
            } else if (hasWater) {
                if (currentWaterQuality > maxQwW) {
                    maxQwW = currentWaterQuality;
                    bestW = new int[]{nx, ny};
                }
            } else {
                if (bestAny == null) bestAny = new int[]{nx, ny};
            }
        }

        if (bestPW != null) {
            Animal prey = (Animal) grid[bestPW[1]][bestPW[0]][EntitySlot.ANIMAL.idx()];
            if (prey != null) {
                setMass(getMass() + prey.getMass());
                setNextOrganicMatter(0.5);
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
                setNextOrganicMatter(0.5);
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
                setNextOrganicMatter(0.5);
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
            setNextOrganicMatter(0.5);
            grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = null;
            setState("well-fed");
            setHasEaten(true);
        }
        grid[bestAny[1]][bestAny[0]][EntitySlot.ANIMAL.idx()] = this;
        grid[y][x][EntitySlot.ANIMAL.idx()] = null;
    }
}
