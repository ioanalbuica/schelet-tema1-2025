package main;

import fileio.*;

import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SimulationMap {
    private int height, width;
    private ArrayList<ArrayList<LinkedList<Entity>>> entityMap;

    public SimulationMap(SimulationInput simInput) {
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

        for (AirInput airInput: simInput.getTerritorySectionParams().getAir()) {
            for (PairInput pos : airInput.getSections()) {
                Air air = null;
                switch (airInput.getType()) {
                    case "TemperateAir" -> air = new TemperateAir(airInput);
                    case "TropicalAir" -> air = new TropicalAir(airInput);
                    case "PolarAir" -> air = new PolarAir(airInput);
                    case "DesertAir" -> air = new DesertAir(airInput);
                    case "MountainAir" -> air = new MountainAir(airInput);
                }
                if (air != null) {
                    air.setScannedTime(1);
                    entityMap.get(pos.getY()).get(pos.getX()).add(air);
                }
            }
        }

        for (SoilInput si : simInput.getTerritorySectionParams().getSoil()) {
            for (PairInput pos : si.getSections()) {
                Soil soil = null;
                switch (si.getType()) {
                    case "ForestSoil" -> soil = new ForestSoil(si);
                    case "SwampSoil" -> soil = new SwampSoil(si);
                    case "DesertSoil" -> soil = new DesertSoil(si);
                    case "GrasslandSoil" -> soil = new GrasslandSoil(si);
                    case "TundraSoil" -> soil = new TundraSoil(si);
                }
                if (soil != null) {
                    soil.setScannedTime(1);
                    entityMap.get(pos.getY()).get(pos.getX()).add(soil);
                }
            }
        }

        for (WaterInput waterInput : simInput.getTerritorySectionParams().getWater()) {
            for (PairInput pos : waterInput.getSections()) {
                entityMap.get(pos.getY()).get(pos.getX()).add(new Water(waterInput));
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

    public void changeMapWeather(String type, String value) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList <Entity> entities = entityMap.get(i).get(j);
                for (Entity e : entities) {
                    if (e instanceof Air) {
                        ((Air) e).changeWeather(type, value);
                    }
                }
            }
        }
    }

    public void actualiseMap(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity.getScannedTime() != 0) {
                        entity.changeEnvironment(currentTime, entities);
                    }
                }
            }
        }
    }

    public void moveAnimals(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity instanceof Animal a && entity.getScannedTime() != 0) {
                        if ((currentTime - a.getScannedTime()) % 2 == 0) {
                            a.move(this, j, i);
                        }
                    }
                }
            }
        }
    }

    public void feedAnimals() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                LinkedList<Entity> entities = entityMap.get(i).get(j);
                LinkedList<Entity> entitiesCopy = new LinkedList<>(entities);

                for (Entity entity : entitiesCopy) {
                    if (entities.contains(entity) && entity instanceof Animal a && entity.getScannedTime() != 0) {
                        a.feed(this, j, i);
                    }
                }
            }
        }
    }
}
