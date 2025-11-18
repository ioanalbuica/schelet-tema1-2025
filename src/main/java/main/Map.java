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
public class Map {
    private int height, width;
    private ArrayList<ArrayList<LinkedList<Entity>>> entityMap;

    public Map(SimulationInput simInput) {
        int x_pos = simInput.getTerritoryDim().indexOf('x');
        height = Integer.parseInt(simInput.getTerritoryDim().substring(0, x_pos));
        width = Integer.parseInt(simInput.getTerritoryDim().substring(x_pos + 1));

        entityMap = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            ArrayList<LinkedList<Entity>> row = new ArrayList<>(width);
            for (int j = 0; j < width; j++) {
                row.add(new LinkedList<>());
            }
            entityMap.add(row);
        }

        for (SoilInput si : simInput.getTerritorySectionParams().getSoil()) {
            for (PairInput pos : si.getSections()) {
                switch (si.getType()) {
                    case "ForestSoil" -> entityMap.get(pos.getX()).get(pos.getY()).add(new ForestSoil(si));
                    case "SwampSoil" -> entityMap.get(pos.getX()).get(pos.getY()).add(new SwampSoil(si));
                    case "DesertSoil" -> entityMap.get(pos.getX()).get(pos.getY()).add(new DesertSoil(si));
                    case "GrasslandSoil" -> entityMap.get(pos.getX()).get(pos.getY()).add(new GrasslandSoil(si));
                    case "TundraSoil" -> entityMap.get(pos.getX()).get(pos.getY()).add(new TundraSoil(si));
                }
            }
        }

        for (WaterInput waterInput : simInput.getTerritorySectionParams().getWater()) {
            for (PairInput pos : waterInput.getSections()) {
                entityMap.get(pos.getX()).get(pos.getY()).add(new Water(waterInput));
            }
        }

        for (AirInput airInput: simInput.getTerritorySectionParams().getAir()) {
            for (PairInput pos : airInput.getSections()) {
                switch (airInput.getType()) {
                    case "TemperateAir" -> entityMap.get(pos.getX()).get(pos.getY()).add(new TemperateAir(airInput));
                    case "TropicalAir" -> entityMap.get(pos.getX()).get(pos.getY()).add(new TropicalAir(airInput));
                    case "PolarAir" -> entityMap.get(pos.getX()).get(pos.getY()).add(new PolarAir(airInput));
                    case "DesertAir" -> entityMap.get(pos.getX()).get(pos.getY()).add(new DesertAir(airInput));
                    case "MountainAir" -> entityMap.get(pos.getX()).get(pos.getY()).add(new MountainAir(airInput));
                }
            }
        }

        for (PlantInput plantInput : simInput.getTerritorySectionParams().getPlants()) {
            for (PairInput pos : plantInput.getSections()) {
                switch (plantInput.getType()) {
                    case "FloweringPlants" -> entityMap.get(pos.getX()).get(pos.getY()).add(new FloweringPlants(plantInput));
                    case "GymnospermsPlants " -> entityMap.get(pos.getX()).get(pos.getY()).add(new GymnospermsPlants(plantInput));
                    case "Ferns" -> entityMap.get(pos.getX()).get(pos.getY()).add(new Ferns (plantInput));
                    case "Mosses" -> entityMap.get(pos.getX()).get(pos.getY()).add(new Mosses(plantInput));
                    case "Algae" -> entityMap.get(pos.getX()).get(pos.getY()).add(new Algae(plantInput));
                }
            }
        }


    }
}
