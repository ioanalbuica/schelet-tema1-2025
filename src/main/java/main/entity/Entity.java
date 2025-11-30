package main.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Entity {
    private String name;
    private double mass;
    private double blockingPossibility;
    private int scannedTime = 0;

    /**
     * fiecare entitate afecteaza entitatile din celula unde se afla si ea
     */
    public abstract void changeEnvironment(int currentTime, SimulationMap map,
                                           int x, int y);
    public abstract void printEntity(ObjectMapper mapper, ObjectNode env);
    /**
     * folosit pentru a crea copy si a le introduce in inventar
     */
    public abstract Entity createDeepCopy();
}
