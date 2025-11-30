package main.entity.plant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.PlantInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.Entity;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Plant extends Entity {
    private String type;
    private double growthLevel = 0.0;
    private int maturityLevel = 0;

    public Plant(final PlantInput plantInput) {
        type = plantInput.getType();
        setMass(plantInput.getMass());
        setName(plantInput.getName());
    }

    public Plant(final Plant other) {
        setName(other.getName());
        setMass(other.getMass());
        type = other.getType();
        growthLevel = other.getGrowthLevel();
        maturityLevel = other.getMaturityLevel();
        setScannedTime(other.getScannedTime());
        setBlockingPossibility(other.getBlockingPossibility());
    }

    @Override
    public final void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        ObjectNode plantNode = mapper.createObjectNode();
        plantNode.put("type", getType());
        plantNode.put("name", getName());
        plantNode.put("mass", getMass());
        env.set("plants", plantNode);
    }
}
