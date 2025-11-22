package main;

import fileio.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TerraBot {
    private int x, y;
    private double batteryLevel;

    public int move(Map map) {
        int[] dy = {1, -1, 0, 0};
        int[] dx = {0, 0, 1, -1};
        boolean foundValid = false;

        int minimum = Integer.MAX_VALUE, minX = x, minY = y;
        for (int k = 0; k < 4; k++) {
            int ny = y + dy[k];
            int nx = x + dx[k];

            if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                continue;
            }
            LinkedList<Entity> entities = map.getEntityMap().get(ny).get(nx);
            double mean = 0.0;
            int count = 0;
            if (entities != null && !entities.isEmpty()) {
                for (Entity entity : entities) {
                    mean += entity.getBlockingPossibility();
                    if (!(entity instanceof Water)) {
                        count++;
                    }
                }
                mean = Math.abs(mean / count);
            } else {
                mean = 0.0;
            }

            int result = (int) Math.round(mean);

            if (result < minimum) {
                minimum = result;
                minX = nx;
                minY = ny;
                foundValid = true;
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
