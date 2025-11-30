package main;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.Entity;
import main.entity.EntitySlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class TerraBot {
    private int x, y;
    private int batteryLevel;
    private ArrayList<Entity> inventory = new ArrayList<>();
    private Map<String, List<String>> knowledgeBase = new LinkedHashMap<>();

    private static final int DIRECTIONS = 4;

    public void addFact(final String entityName, final String fact) {
        knowledgeBase.putIfAbsent(entityName, new ArrayList<>());
        List<String> facts = knowledgeBase.get(entityName);
        if (!facts.contains(fact)) {
            facts.add(fact);
        }
    }

    public int move(final SimulationMap map) {
        int[] dy = {1, 0, -1, 0};
        int[] dx = {0, 1, 0, -1};
        boolean foundValid = false;

        int minimum = Integer.MAX_VALUE, minX = x, minY = y;
        for (int k = 0; k < DIRECTIONS; k++) {
            int ny = y + dy[k];
            int nx = x + dx[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }

            Entity[] entities = map.getEntityMap()[ny][nx];
            double mean = 0.0;
            int count = 0;

            for (int i = EntitySlot.AIR.idx(); i <= EntitySlot.ANIMAL.idx(); i++) {
                if (entities[i] != null && i != EntitySlot.WATER.idx()) {
                    mean += entities[i].getBlockingPossibility();
                    count++;
                }
            }

            if (count > 0) {
                mean = Math.abs(mean / count);
                int result = (int) Math.round(mean);

                if (result < minimum) {
                    minimum = result;
                    minX = nx;
                    minY = ny;
                    foundValid = true;
                }
            }
        }

        if (!foundValid) {
            return -1;
        }

        if (batteryLevel >= minimum) {
            batteryLevel -= minimum;
            x = minX;
            y = minY;
            return 0;
        } else {
            return -1;
        }
    }
}
