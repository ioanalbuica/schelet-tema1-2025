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
    private int maturityLevel;

    public Plant(PlantInput plantInput) {
        type = plantInput.getType();
        setMass(plantInput.getMass());
        setName(plantInput.getName());
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

