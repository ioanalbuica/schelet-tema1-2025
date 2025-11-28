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
    private final int MAX_ENT = 5;
    private Entity[][][] entityMap = null;

    public SimulationMap(SimulationInput simInput) {
        int x_pos = simInput.getTerritoryDim().indexOf('x');
        height = Integer.parseInt(simInput.getTerritoryDim().substring(0, x_pos));
        width = Integer.parseInt(simInput.getTerritoryDim().substring(x_pos + 1));
        entityMap = new Entity[height][width][MAX_ENT];

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
                    entityMap[pos.getY()][pos.getX()][EntitySlot.AIR.idx()] = air;
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
                    entityMap[pos.getY()][pos.getX()][EntitySlot.SOIL.idx()] = soil;
                }
            }
        }

        for (WaterInput waterInput : simInput.getTerritorySectionParams().getWater()) {
            for (PairInput pos : waterInput.getSections()) {
                entityMap[pos.getY()][pos.getX()][EntitySlot.WATER.idx()] = new Water(waterInput);
            }
        }

        for (PlantInput plantInput : simInput.getTerritorySectionParams().getPlants()) {
            for (PairInput pos : plantInput.getSections()) {
                switch (plantInput.getType()) {
                    case "FloweringPlants" -> entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = new FloweringPlants(plantInput);
                    case "GymnospermsPlants" -> entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = new GymnospermsPlants(plantInput);
                    case "Ferns" -> entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = new Ferns(plantInput);
                    case "Mosses" -> entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = new Mosses(plantInput);
                    case "Algae" -> entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = new Algae(plantInput);
                }
            }
        }

        for (AnimalInput animalInput : simInput.getTerritorySectionParams().getAnimals()) {
            for (PairInput pos : animalInput.getSections()) {
                switch (animalInput.getType()) {
                    case "Herbivores" -> entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = new Herbivores(animalInput);
                    case "Carnivores" -> entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = new Carnivores(animalInput);
                    case "Omnivores" -> entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = new Omnivores(animalInput);
                    case "Detritivores" -> entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = new Detritivores(animalInput);
                    case "Parasites" -> entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = new Parasites(animalInput);
                }
            }
        }
    }

    public String getSoilQualityLabel(int x, int y) {
        if (entityMap[y][x][EntitySlot.SOIL.idx()] != null) {
            Soil s = (Soil)entityMap[y][x][EntitySlot.SOIL.idx()];
            double sq = s.getSoilQuality();
            if (sq >= 70) {
                return "good";
            } else if (sq >= 40) {
                return "moderate";
            } else {
                return "poor";
            }
        }
        return null;
    }

    public String getAirQualityLabel(int x, int y) {
        Air a = (Air) entityMap[y][x][EntitySlot.AIR.idx()];
        if (a != null) {
            double aq = a.getAirQuality();
            if (aq >= 70) {
                return "good";
            } else if (aq >= 40) {
                return "moderate";
            } else {
                return "poor";
            }
        }
        return null;
    }

    public void changeMapWeather(String type, String value) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Air a = (Air) entityMap[i][j][EntitySlot.AIR.idx()];
                if (a != null) {
                    a.changeWeather(type, value);
                }
            }
        }
    }

    public void actualiseMap(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = EntitySlot.AIR.idx(); k <= EntitySlot.ANIMAL.idx(); k++) {
                    Entity entity = entityMap[i][j][k];
                    if (entity != null && entity.getScannedTime() > 0) {
                        entity.changeEnvironment(currentTime, this, j, i);
                    }
                }
            }
        }
    }

    public void feedAnimals(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Animal animal = (Animal) entityMap[i][j][EntitySlot.ANIMAL.idx()];
                if (animal != null && animal.getScannedTime() > 0) {
                    animal.feed(currentTime, this, j, i);
                }
            }
        }
    }

    public void moveAnimals(int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Animal animal = (Animal) entityMap[i][j][EntitySlot.ANIMAL.idx()];
                if (animal != null && animal.getScannedTime() > 0 && (currentTime - animal.getLastMovedTime()) % 2 == 0 &&
                        (currentTime != animal.getLastMovedTime())) {
                    animal.move(currentTime, this, j, i);
                }
            }
        }
    }
}
