package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.PlantInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Plant extends Entity {
    private String type;
    private double growthLevel = 0.0;
    private int maturityLevel = 0;

    public Plant(PlantInput plantInput) {
        type = plantInput.getType();
        setMass(plantInput.getMass());
        setName(plantInput.getName());
    }

    public Plant(Plant other) {
        setName(other.getName());
        setMass(other.getMass());
        type = other.getType();
        growthLevel = other.getGrowthLevel();
        maturityLevel = other.getMaturityLevel();
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode plantNode = MAPPER.createObjectNode();
        plantNode.put("type", getType());
        plantNode.put("name", getName());
        plantNode.put("mass", getMass());
        env.set("plants", plantNode);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class FloweringPlants extends Plant {
    public FloweringPlants(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(0.9);
    }

    public FloweringPlants(FloweringPlants other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new FloweringPlants(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                double maturityOxygenRate = 0.0;
                if (this.getMaturityLevel() == 0) {
                    maturityOxygenRate = 0.2;
                } else if (this.getMaturityLevel() == 1) {
                    maturityOxygenRate = 0.7;
                } else if (this.getMaturityLevel() == 2) {
                    maturityOxygenRate = 0.4;
                }
                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 6 + maturityOxygenRate) * 100.0) / 100.0);
                a.calculateAirQuality();
                a.setBlockingPossibility(a.getToxicityLevel());
            }
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GymnospermsPlants extends Plant {
    public GymnospermsPlants(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(0.6);
    }

    public GymnospermsPlants(GymnospermsPlants other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new GymnospermsPlants(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                double maturityOxygenRate = 0.0;
                if (this.getMaturityLevel() == 0) {
                    maturityOxygenRate = 0.2;
                } else if (this.getMaturityLevel() == 1) {
                    maturityOxygenRate = 0.7;
                } else if (this.getMaturityLevel() == 2) {
                    maturityOxygenRate = 0.4;
                }
                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 0 + maturityOxygenRate) * 100.0) / 100.0);
                a.calculateAirQuality();
                a.setBlockingPossibility(a.getToxicityLevel());
            }
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Ferns extends Plant {
    public Ferns(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(0.3);
    }

    public Ferns(Ferns other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Ferns(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                double maturityOxygenRate = 0.0;
                if (this.getMaturityLevel() == 0) {
                    maturityOxygenRate = 0.2;
                } else if (this.getMaturityLevel() == 1) {
                    maturityOxygenRate = 0.7;
                } else if (this.getMaturityLevel() == 2) {
                    maturityOxygenRate = 0.4;
                }
                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 0 + maturityOxygenRate) * 100.0) / 100.0);
                a.calculateAirQuality();
                a.setBlockingPossibility(a.getToxicityLevel());
            }
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Mosses extends Plant {
    public Mosses(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(0.4);
    }

    public Mosses(Mosses other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Mosses(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                double maturityOxygenRate = 0.0;
                if (this.getMaturityLevel() == 0) {
                    maturityOxygenRate = 0.2;
                } else if (this.getMaturityLevel() == 1) {
                    maturityOxygenRate = 0.7;
                } else if (this.getMaturityLevel() == 2) {
                    maturityOxygenRate = 0.4;
                }
                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 0.8 + maturityOxygenRate) * 100.0) / 100.0);
                a.calculateAirQuality();
                a.setBlockingPossibility(a.getToxicityLevel());
            }
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Algae extends Plant {
    public Algae(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(0.2);
    }

    public Algae(Algae other) {
        super(other);
    }

    @Override
    public Entity createDeepCopy() {
        return new Algae(this);
    }

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Air a) {
                double maturityOxygenRate = 0.0;
                if (this.getMaturityLevel() == 0) {
                    maturityOxygenRate = 0.2;
                } else if (this.getMaturityLevel() == 1) {
                    maturityOxygenRate = 0.7;
                } else if (this.getMaturityLevel() == 2) {
                    maturityOxygenRate = 0.4;
                }
                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 0.5 + maturityOxygenRate) * 100.0) / 100.0);
                a.calculateAirQuality();
                a.setBlockingPossibility(a.getToxicityLevel());
            }
        }
    }
}

