package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.PlantInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Plant extends Entity {
    private String type;
    private int maturityLevel;
    private double blockingPossibility;

    public Plant(PlantInput plantInput) {
        setMass(plantInput.getMass());
        setType(plantInput.getType());
        setName(plantInput.getName());
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class FloweringPlants extends Plant {
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    public FloweringPlants(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(90);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GymnospermsPlants extends Plant {
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    public GymnospermsPlants(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(60);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Ferns extends Plant {
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    public Ferns(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(30);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Mosses extends Plant {
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    public Mosses(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(40);
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Algae extends Plant {
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    public Algae(PlantInput plantInput) {
        super(plantInput);
        setBlockingPossibility(20);
    }
}

