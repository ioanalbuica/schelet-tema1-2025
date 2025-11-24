package main;

import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ScannedMap {
    private int height, width;
    private ArrayList<ArrayList<LinkedList<Entity>>> entityMap;

    public ScannedMap(SimulationMap map) {
        height = map.getHeight();
        width = map.getWidth();

        entityMap = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            ArrayList<LinkedList<Entity>> row = new ArrayList<>(width);
            for (int j = 0; j < width; j++) {
                row.add(new LinkedList<>());
            }
            entityMap.add(row);
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = map.getEntityMap().get(i).get(j);
                for (Entity entity : entities) {
                    if (entity instanceof Soil || entity instanceof Air) {
                        entityMap.get(i).get(j).add(entity);
                    }
                }
            }
        }
    }

    public void addScannedObject(Entity entity, int x, int y) {
        entityMap.get(y).get(x).add(entity);
    }

    public void actualiseMap(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity.getScannedTime() > 0) {
                        entity.changeEnvironment(currentTime, entities);
                    }
                }
            }
        }
    }

    public void moveAnimals(SimulationMap map, int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity instanceof Animal a && entity.getScannedTime() > 0) {
                        if ((currentTime - a.getScannedTime()) % 2 == 0) {
                            a.move(map, j, i);
                        }
                    }
                }
            }
        }
    }

    public void feedAnimals(SimulationMap map) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity instanceof Animal a && entity.getScannedTime() > 0) {
                        a.feed(map, j, i);
                    }
                }
            }
        }
    }
}
