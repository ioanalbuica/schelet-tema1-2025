package main;

import fileio.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.air.Air;
import main.entity.air.DesertAir;
import main.entity.air.MountainAir;
import main.entity.air.PolarAir;
import main.entity.air.TemperateAir;
import main.entity.air.TropicalAir;
import main.entity.animal.Carnivores;
import main.entity.animal.Parasites;
import main.entity.animal.Herbivores;
import main.entity.animal.Detritivores;
import main.entity.animal.Omnivores;
import main.entity.animal.Animal;
import main.entity.plant.GymnospermsPlants;
import main.entity.plant.Ferns;
import main.entity.plant.Plant;
import main.entity.plant.FloweringPlants;
import main.entity.plant.Algae;
import main.entity.plant.Mosses;
import main.entity.soil.Soil;
import main.entity.soil.DesertSoil;
import main.entity.soil.ForestSoil;
import main.entity.soil.TundraSoil;
import main.entity.soil.GrasslandSoil;
import main.entity.soil.SwampSoil;
import main.entity.water.Water;


@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SimulationMap {
    private int height, width;
    private final int entitiesOnCell = 5;
    private Entity[][][] entityMap = null;

    private static final double GOOD_AIR_QUALITY = 70.0;
    private static final double MODERATE_AIR_QUALITY = 40.0;
    private static final double GOOD_SOIL_QUALITY = 70.0;
    private static final double MODERATE_SOIL_QUALITY = 40.0;

    public SimulationMap(final SimulationInput simInput) {
        int xPos = simInput.getTerritoryDim().indexOf('x');
        height = Integer.parseInt(simInput.getTerritoryDim().substring(0, xPos));
        width = Integer.parseInt(simInput.getTerritoryDim().substring(xPos + 1));
        entityMap = new Entity[height][width][entitiesOnCell];

        for (AirInput airInput: simInput.getTerritorySectionParams().getAir()) {
            for (PairInput pos : airInput.getSections()) {
                Air air;
                switch (airInput.getType()) {
                    case "TemperateAir" -> air = new TemperateAir(airInput);
                    case "TropicalAir" -> air = new TropicalAir(airInput);
                    case "PolarAir" -> air = new PolarAir(airInput);
                    case "DesertAir" -> air = new DesertAir(airInput);
                    case "MountainAir" -> air = new MountainAir(airInput);
                    default -> air = null;
                }
                if (air != null) {
                    air.setScannedTime(1);
                    entityMap[pos.getY()][pos.getX()][EntitySlot.AIR.idx()] = air;
                }
            }
        }

        for (SoilInput si : simInput.getTerritorySectionParams().getSoil()) {
            for (PairInput pos : si.getSections()) {
                Soil soil;
                switch (si.getType()) {
                    case "ForestSoil" -> soil = new ForestSoil(si);
                    case "SwampSoil" -> soil = new SwampSoil(si);
                    case "DesertSoil" -> soil = new DesertSoil(si);
                    case "GrasslandSoil" -> soil = new GrasslandSoil(si);
                    case "TundraSoil" -> soil = new TundraSoil(si);
                    default -> soil = null;
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
                Plant plant;
                switch (plantInput.getType()) {
                    case "FloweringPlants" -> plant = new FloweringPlants(plantInput);
                    case "GymnospermsPlants" -> plant = new GymnospermsPlants(plantInput);
                    case "Ferns" -> plant = new Ferns(plantInput);
                    case "Mosses" -> plant = new Mosses(plantInput);
                    case "Algae" -> plant = new Algae(plantInput);
                    default -> plant = null;
                }
                if (plant != null) {
                    entityMap[pos.getY()][pos.getX()][EntitySlot.PLANT.idx()] = plant;
                }
            }
        }

        for (AnimalInput animalInput : simInput.getTerritorySectionParams().getAnimals()) {
            for (PairInput pos : animalInput.getSections()) {
                Animal animal;
                switch (animalInput.getType()) {
                    case "Herbivores" -> animal = new Herbivores(animalInput);
                    case "Carnivores" -> animal = new Carnivores(animalInput);
                    case "Omnivores" -> animal = new Omnivores(animalInput);
                    case "Detritivores" -> animal = new Detritivores(animalInput);
                    case "Parasites" -> animal = new Parasites(animalInput);
                    default -> animal = null;
                }
                if (animal != null) {
                    entityMap[pos.getY()][pos.getX()][EntitySlot.ANIMAL.idx()] = animal;
                }
            }
        }
    }

    /**
     * ne returneaza calitatea solului sub forma good, moderate, poor
     */
    public final String getSoilQualityLabel(final int x, final int y) {
        if (entityMap[y][x][EntitySlot.SOIL.idx()] != null) {
            Soil s = (Soil) entityMap[y][x][EntitySlot.SOIL.idx()];
            double sq = s.getSoilQuality();
            if (sq >= GOOD_SOIL_QUALITY) {
                return "good";
            } else if (sq >= MODERATE_SOIL_QUALITY) {
                return "moderate";
            } else {
                return "poor";
            }
        }
        return null;
    }

    /**
     * ne returneaza calitatea aerului sub forma good, moderate, poor
     */
    public final String getAirQualityLabel(final int x, final int y) {
        Air a = (Air) entityMap[y][x][EntitySlot.AIR.idx()];
        if (a != null) {
            double aq = a.getAirQuality();
            if (aq >= GOOD_AIR_QUALITY) {
                return "good";
            } else if (aq >= MODERATE_AIR_QUALITY) {
                return "moderate";
            } else {
                return "poor";
            }
        }
        return null;
    }

    /**
     * aplicam schimbariile pt fiecare aer
     * daca type nu va corespunde cu aerul respectiv se va trece mai departe
     * value va fi convertit la tipul de date corespunzator schimbarii meteo
     */
    public final void changeMapWeather(final String type, final String value) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Air a = (Air) entityMap[i][j][EntitySlot.AIR.idx()];
                if (a != null) {
                    a.changeWeather(type, value);
                }
            }
        }
    }

    /**
     * fiecare entitate scanata isi va afecta entitatile scanate din celula sa
     */
    public final void actualiseMap(final int currentTime) {
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

    /**
     * aplicam feed pt fiecare animal scanat
     */
    public void feedAnimals() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Animal animal = (Animal) entityMap[i][j][EntitySlot.ANIMAL.idx()];
                if (animal != null && animal.getScannedTime() > 0) {
                    animal.feed(this, j, i);
                }
            }
        }
    }

    /**
     * aplicam move pt fiecare animal scanat
     */
    public void moveAnimals(final int currentTime) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Animal animal = (Animal) entityMap[i][j][EntitySlot.ANIMAL.idx()];
                if (animal != null && animal.getScannedTime() > 0) {
                    animal.move(currentTime, this, j, i);
                }
            }
        }
    }
}
