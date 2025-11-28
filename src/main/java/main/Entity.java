package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Entity {
    private String name;
    private double mass;
    private double blockingPossibility;
    private int scannedTime = 0;

    abstract public void changeEnvironment(int currentTime, SimulationMap map, int x, int y);
    abstract public void printEntity(ObjectMapper MAPPER, ObjectNode env);
    public abstract Entity createDeepCopy();
}
