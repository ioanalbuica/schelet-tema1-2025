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
                    case "ForestSoil" -> entityMap.get(pos.getY()).get(pos.getX()).add(new ForestSoil(si));
                    case "SwampSoil" -> entityMap.get(pos.getY()).get(pos.getX()).add(new SwampSoil(si));
                    case "DesertSoil" -> entityMap.get(pos.getY()).get(pos.getX()).add(new DesertSoil(si));
                    case "GrasslandSoil" -> entityMap.get(pos.getY()).get(pos.getX()).add(new GrasslandSoil(si));
                    case "TundraSoil" -> entityMap.get(pos.getY()).get(pos.getX()).add(new TundraSoil(si));
                }
            }
        }

        for (WaterInput waterInput : simInput.getTerritorySectionParams().getWater()) {
            for (PairInput pos : waterInput.getSections()) {
                entityMap.get(pos.getY()).get(pos.getX()).add(new Water(waterInput));
            }
        }

        for (AirInput airInput: simInput.getTerritorySectionParams().getAir()) {
            for (PairInput pos : airInput.getSections()) {
                switch (airInput.getType()) {
                    case "TemperateAir" -> entityMap.get(pos.getY()).get(pos.getX()).add(new TemperateAir(airInput));
                    case "TropicalAir" -> entityMap.get(pos.getY()).get(pos.getX()).add(new TropicalAir(airInput));
                    case "PolarAir" -> entityMap.get(pos.getY()).get(pos.getX()).add(new PolarAir(airInput));
                    case "DesertAir" -> entityMap.get(pos.getY()).get(pos.getX()).add(new DesertAir(airInput));
                    case "MountainAir" -> entityMap.get(pos.getY()).get(pos.getX()).add(new MountainAir(airInput));
                }
            }
        }

        for (PlantInput plantInput : simInput.getTerritorySectionParams().getPlants()) {
            for (PairInput pos : plantInput.getSections()) {
                switch (plantInput.getType()) {
                    case "FloweringPlants" -> entityMap.get(pos.getY()).get(pos.getX()).add(new FloweringPlants(plantInput));
                    case "GymnospermsPlants" -> entityMap.get(pos.getY()).get(pos.getX()).add(new GymnospermsPlants(plantInput));
                    case "Ferns" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Ferns(plantInput));
                    case "Mosses" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Mosses(plantInput));
                    case "Algae" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Algae(plantInput));
                }
            }
        }

        for (AnimalInput animalInput : simInput.getTerritorySectionParams().getAnimals()) {
            for (PairInput pos : animalInput.getSections()) {
                switch (animalInput.getType()) {
                    case "Herbivores" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Herbivores(animalInput));
                    case "Carnivores" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Carnivores(animalInput));
                    case "Omnivores" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Omnivores(animalInput));
                    case "Detritivores" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Detritivores(animalInput));
                    case "Parasites" -> entityMap.get(pos.getY()).get(pos.getX()).add(new Parasites(animalInput));
                }
            }
        }
    }

    public String getSoilQualityLabel(int x, int y) {
        for (Entity e : entityMap.get(y).get(x)) {
            if (e instanceof Soil) {
                Soil s = (Soil)e;
                double sq = s.getSoilQuality();
                if (sq >= 70) {
                    return "good";
                } else if (sq >= 40) {
                    return "moderate";
                } else {
                    return "poor";
                }
            }
        }
        return null;
    }

    public String getAirQualityLabel(int x, int y) {
        for (Entity e : entityMap.get(y).get(x)) {
            if (e instanceof Air) {
                Air a = (Air)e;
                double aq = a.getAirQuality();
                if (aq >= 70) {
                    return "good";
                } else if (aq >= 40) {
                    return "moderate";
                } else {
                    return "poor";
                }
            }
        }
        return null;
    }


}
