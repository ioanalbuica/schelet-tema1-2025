package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Animal extends Entity {
    private String type;
    private String state;
    private double nextOrganicMatter = 0.0;

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

    public void move(SimulationMap map, int x, int y) {
        int[] dy = {1, 0, -1, 0};
        int[] dx = {0, 1, 0, -1};

        int[] bestPW = null, bestP = null, bestW = null, bestAny = null;
        double maxQwPW = -1.0, maxQwW = -1.0;

        for (int k = 0; k < 4; k++) {
            int ny = y + dy[k];
            int nx = x + dx[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            boolean hasPlant = false;
            boolean hasWater = false;
            double currentWaterQuality = 0.0;

            LinkedList<Entity> entities = map.getEntityMap().get(ny).get(nx);
            for (Entity entity : entities) {
                if (entity instanceof Plant) {
                    hasPlant = true;
                } else if (entity instanceof Water) {
                    hasWater = true;
                    currentWaterQuality = ((Water) entity).getWaterQuality();
                }
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

        int[] target;
        if (bestPW != null) {
            target = bestPW;
        } else if (bestP != null) {
            target = bestP;
        } else if (bestW != null) {
            target = bestW;
        } else {
            target = bestAny;
        }

        if (target != null) {
            map.getEntityMap().get(y).get(x).remove(this);
            map.getEntityMap().get(target[1]).get(target[0]).add(this);
        }
    }

    public void feed(SimulationMap map, int x, int y) {
        LinkedList<Entity> entities = map.getEntityMap().get(y).get(x);
        Plant plant = null;
        Water water = null;
        for (Entity e : entities) {
            if (e instanceof Plant && e.getScannedTime() != 0) {
                plant = (Plant) e;
            } else if (e instanceof Water && e.getScannedTime() != 0) {
                water = (Water) e;
            }
        }

        int scannedPlantTime = 0, scannedWaterTime = 0;
        if (plant != null) {
            scannedPlantTime = plant.getScannedTime();
        }
        if (water != null) {
            scannedWaterTime = water.getScannedTime();
        }
        if (scannedPlantTime > 0 && scannedWaterTime > 0) {
            entities.remove(plant);

            double intakeRate = 0.08;
            double waterToDrink = Math.min(getMass() * intakeRate, water.getMass());
            water.setMass(water.getMass() - waterToDrink);
            if (water.getMass() <= 0) {
                entities.remove(water);
            }
            setMass(getMass() + waterToDrink + plant.getMass());
            nextOrganicMatter = 0.8;
            setState("well-fed");
        } else if (scannedPlantTime > 0) {
            entities.remove(plant);
            setMass(getMass() + plant.getMass());
            nextOrganicMatter = 0.5;
            setState("well-fed");
        } else if (scannedWaterTime > 0) {
            double intakeRate = 0.08;
            double waterToDrink = Math.min(getMass() * intakeRate, water.getMass());
            water.setMass(water.getMass() - waterToDrink);
            if (water.getMass() <= 0) {
                entities.remove(water);
            }
            setMass(getMass() + waterToDrink);
            nextOrganicMatter = 0.5;
            setState("well-fed");
        } else {
            nextOrganicMatter = 0.0;
            setState("hungry");
        }
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        if (nextOrganicMatter > 0) {
            for (Entity e : entitiesList) {
                if (e instanceof Soil s) {
                    s.setOrganicMatter(s.getOrganicMatter() + nextOrganicMatter);
                    s.calculateSoilQuality();
                }
            }
            nextOrganicMatter = 0;
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

    @Override
    public Entity createDeepCopy() {
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
    public void feed(SimulationMap map, int x, int y) {
        LinkedList<Entity> entities = map.getEntityMap().get(y).get(x);
        Animal prey = null;

        for (Entity e : entities) {
            if (e instanceof Animal && e != this) {
                prey = (Animal) e;
            }
        }
        if (prey != null) {
            setMass(getMass() + prey.getMass());
            setNextOrganicMatter(0.5);
            setState("well-fed");
            entities.remove(prey);
            return;
        }
        super.feed(map, x, y);
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
    public void feed(SimulationMap map, int x, int y) {
        LinkedList<Entity> entities = map.getEntityMap().get(y).get(x);
        Animal prey = null;

        for (Entity e : entities) {
            if (e instanceof Animal && e != this) {
                prey = (Animal) e;
            }
        }
        if (prey != null) {
            setMass(getMass() + prey.getMass());
            setNextOrganicMatter(0.5);
            setState("well-fed");
            entities.remove(prey);
            return;
        }
        super.feed(map, x, y);
    }
}



