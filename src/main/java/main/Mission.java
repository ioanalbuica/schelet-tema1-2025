package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.SimulationInput;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.air.Air;
import main.entity.animal.Animal;
import main.entity.plant.Plant;
import main.entity.soil.Soil;
import main.entity.water.Water;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class Mission {
    private SimulationMap map = null;
    private TerraBot terraBot = null;

    private static final int SCAN_COST = 7;
    private static final int LEARN_FACT_COST = 2;
    private static final int IMPROVE_ENV_COST = 10;
    private static final double OXYGEN_PLANT_DELTA = 0.3;
    private static final double ORGANIC_FERTILIZE_DELTA = 0.3;
    private static final double HUMIDITY_DELTA = 0.2;
    private static final double MOISTURE_DELTA = 0.2;
    private static final double ROUND_FACTOR = 100.0;

    public void run(final ArrayList<SimulationInput> sims,
                    final ArrayList<CommandInput> cmds,
                    final ObjectMapper mapper,
                    final ArrayNode output) {
        int simIndex = -1;
        int cmdIndex = 0;
        int time = 1;
        boolean startedSim = false;
        boolean weatherChangeOver = true;
        int chargingStartTimestamp = -1;
        int chargingDuration = 0;
        int changedWeatherStart = 0;
        String weatherType = null;

        while (true) {
            /**
             * actualizam harta, apoi hranirea animalelor, apoi miscarea
             */
            if (map != null) {
                map.actualiseMap(time);
                map.feedAnimals();
                map.moveAnimals(time);
            }

            /**
             * daca este primul time dupa ce s-a dus efectul meteo ne intoarcem cum era inainte
             */
            if (!weatherChangeOver && time - changedWeatherStart >= 2) {
                weatherChangeOver = true;
                if (weatherType != null && map != null) {
                    map.changeMapWeather(weatherType, "0");
                }
            }

            CommandInput cmd = cmds.get(cmdIndex);
            if (cmd.getTimestamp() == time) {
                if (cmd.getCommand().equals("startSimulation")) {
                    if (startedSim) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", cmd.getCommand());
                        node.put("message",
                                "ERROR: Simulation already started. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    startedSim = true;
                    simIndex++;
                    SimulationInput sim = sims.get(simIndex);
                    map = new SimulationMap(sim);
                    terraBot = new TerraBot();
                    terraBot.setBatteryLevel(sim.getEnergyPoints());

                    ObjectNode startNode = mapper.createObjectNode();
                    startNode.put("command", "startSimulation");
                    startNode.put("message", "Simulation has started.");
                    startNode.put("timestamp", cmd.getTimestamp());
                    output.add(startNode);

                } else if (!startedSim) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", cmd.getCommand());
                    node.put("message",
                            "ERROR: Simulation not started. Cannot perform action");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getTimestamp() - chargingStartTimestamp < chargingDuration) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", cmd.getCommand());
                    node.put("message",
                            "ERROR: Robot still charging. Cannot perform action");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("printEnvConditions")) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "printEnvConditions");

                    ObjectNode env = mapper.createObjectNode();
                    Entity[] localEntities =
                            map.getEntityMap()[terraBot.getY()][terraBot.getX()];
                    for (Entity e : localEntities) {
                        if (e != null) {
                            e.printEntity(mapper, env);
                        }
                    }
                    node.set("output", env);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("printMap")) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "printMap");

                    ArrayNode mapArray = mapper.createArrayNode();
                    for (int y = 0; y < map.getHeight(); y++) {
                        for (int x = 0; x < map.getWidth(); x++) {
                            final ObjectNode sectionNode = mapper.createObjectNode();
                            final ArrayNode sectionCoords = mapper.createArrayNode();
                            sectionCoords.add(x);
                            sectionCoords.add(y);
                            sectionNode.set("section", sectionCoords);

                            int nr = 0;
                            if (map.getEntityMap()[y][x][EntitySlot.WATER.idx()] != null) {
                                nr++;
                            }
                            if (map.getEntityMap()[y][x][EntitySlot.PLANT.idx()] != null) {
                                nr++;
                            }
                            if (map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()] != null) {
                                nr++;
                            }
                            sectionNode.put("totalNrOfObjects", nr);
                            sectionNode.put("airQuality", map.getAirQualityLabel(x, y));
                            sectionNode.put("soilQuality", map.getSoilQualityLabel(x, y));
                            mapArray.add(sectionNode);
                        }
                    }

                    node.set("output", mapArray);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("endSimulation")) {
                    startedSim = false;
                    chargingStartTimestamp = -1;
                    chargingDuration = 0;

                    ObjectNode endNode = mapper.createObjectNode();
                    endNode.put("command", "endSimulation");
                    endNode.put("message", "Simulation has ended.");
                    endNode.put("timestamp", cmd.getTimestamp());
                    output.add(endNode);

                } else if (cmd.getCommand().equals("moveRobot")) {
                    int rez = terraBot.move(map);
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "moveRobot");
                    if (rez == 0) {
                        node.put("message",
                                "The robot has successfully moved to position ("
                                        + terraBot.getX() + ", " + terraBot.getY() + ").");
                    } else {
                        node.put("message",
                                "ERROR: Not enough battery left. Cannot perform action");
                    }
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("getEnergyStatus")) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "getEnergyStatus");
                    node.put("message",
                            "TerraBot has " + terraBot.getBatteryLevel()
                                    + " energy points left.");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("rechargeBattery")) {
                    chargingStartTimestamp = cmd.getTimestamp();
                    chargingDuration = cmd.getTimeToCharge();
                    terraBot.setBatteryLevel(terraBot.getBatteryLevel() + chargingDuration);

                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "rechargeBattery");
                    node.put("message", "Robot battery is charging.");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("changeWeatherConditions")) {
                    weatherChangeOver = false;
                    String value = "0";
                    weatherType = cmd.getType() != null ? cmd.getType().trim() : "";

                    if (weatherType.equals("rainfall")) {
                        value = Double.toString(cmd.getRainfall());
                    } else if (weatherType.equals("polarStorm")) {
                        value = Double.toString(cmd.getWindSpeed());
                    } else if (weatherType.equals("newSeason")) {
                        value = cmd.getSeason();
                    } else if (weatherType.equals("desertStorm")) {
                        value = Boolean.toString(cmd.isDesertStorm());
                    } else if (weatherType.equals("peopleHiking")) {
                        value = Integer.toString(cmd.getNumberOfHikers());
                    }

                    /**
                     * verificam daca exista tipul de aer pt efectul meteo respectiv
                     */
                    boolean airFound = false;
                    if (map != null) {
                        final Entity[][][] grid = map.getEntityMap();
                        for (int i = 0; i < map.getHeight() && !airFound; i++) {
                            for (int j = 0; j < map.getWidth() && !airFound; j++) {
                                final Air a = (Air) grid[i][j][EntitySlot.AIR.idx()];
                                if (a == null) {
                                    continue;
                                }
                                if (weatherType.equals("polarStorm")
                                        && a.getType().equals("PolarAir")) {
                                    airFound = true;
                                } else if (weatherType.equals("desertStorm")
                                        && a.getType().equals("DesertAir")) {
                                    airFound = true;
                                } else if (weatherType.equals("rainfall")
                                        && a.getType().equals("TropicalAir")) {
                                    airFound = true;
                                } else if (weatherType.equals("newSeason")
                                        && a.getType().equals("TemperateAir")) {
                                    airFound = true;
                                } else if (weatherType.equals("peopleHiking")
                                        && a.getType().equals("MountainAir")) {
                                    airFound = true;
                                }
                            }
                        }
                    }

                    if (!airFound) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", "changeWeatherConditions");
                        node.put("message",
                                "ERROR: The weather change does not affect the environment. "
                                        + "Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        weatherChangeOver = true;
                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    map.changeMapWeather(weatherType, value);
                    changedWeatherStart = cmd.getTimestamp();

                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "changeWeatherConditions");
                    node.put("message", "The weather has changed.");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("scanObject")) {
                    if (terraBot.getBatteryLevel() < SCAN_COST) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", "scanObject");
                        node.put("message", "ERROR: Not enough energy to perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    boolean found = false;
                    String type = null;
                    final Entity[] cellSlots = map.getEntityMap()[terraBot.getY()][terraBot.getX()];

                    /**
                     * animalele au sunet, plantele nu au sunet dar au restul, apa nu are nimic
                     */
                    if (!cmd.getSound().equals("none")) {
                        Animal a = (Animal) cellSlots[EntitySlot.ANIMAL.idx()];
                        if (a != null) {
                            a.setScannedTime(time);
                            type = "an animal.";
                            found = true;
                            terraBot.setBatteryLevel(terraBot.getBatteryLevel() - SCAN_COST);
                            terraBot.getInventory().add(a.createDeepCopy());
                            a.setLastMovedTime(time);
                        }
                    } else if (!cmd.getSmell().equals("none")) {
                        Plant p = (Plant) cellSlots[EntitySlot.PLANT.idx()];
                        if (p != null) {
                            p.setScannedTime(time);
                            type = "a plant.";
                            found = true;
                            terraBot.setBatteryLevel(terraBot.getBatteryLevel() - SCAN_COST);
                            terraBot.getInventory().add(p.createDeepCopy());
                        }
                    } else {
                        Water w = (Water) cellSlots[EntitySlot.WATER.idx()];
                        if (w != null) {
                            w.setScannedTime(time);
                            type = "water.";
                            found = true;
                            terraBot.setBatteryLevel(terraBot.getBatteryLevel() - SCAN_COST);
                            terraBot.getInventory().add(w.createDeepCopy());
                        }
                    }

                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "scanObject");
                    if (!found) {
                        node.put("message", "ERROR: Object not found. Cannot perform action");
                    } else {
                        node.put("message", "The scanned object is " + type);
                    }
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("learnFact")) {
                    if (terraBot.getBatteryLevel() < LEARN_FACT_COST) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", "learnFact");
                        node.put("message", "ERROR: Not enough battery left. "
                                + "Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    /**
                     * cautam daca avem enitatea in inventar
                     */
                    boolean isScanned = false;
                    for (Entity entity : terraBot.getInventory()) {
                        if (entity.getName().equals(cmd.getComponents())) {
                            isScanned = true;
                            break;
                        }
                    }

                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "learnFact");
                    if (isScanned) {
                        terraBot.setBatteryLevel(terraBot.getBatteryLevel() - LEARN_FACT_COST);
                        terraBot.addFact(cmd.getComponents(), cmd.getSubject());
                        node.put("message", "The fact has been successfully saved in "
                                + "the database.");
                    } else {
                        node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                    }
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("printKnowledgeBase")) {
                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "printKnowledgeBase");

                    ArrayNode outputArray = mapper.createArrayNode();

                    /**
                     * pt fiecare componenta afisam facts-urile despre ea
                     */
                    if (terraBot.getKnowledgeBase() != null) {
                        for (String topicName : terraBot.getKnowledgeBase().keySet()) {
                            ObjectNode topicNode = mapper.createObjectNode();
                            topicNode.put("topic", topicName);

                            ArrayNode factsArray = mapper.createArrayNode();
                            List<String> facts = terraBot.getKnowledgeBase().get(topicName);
                            for (String fact : facts) {
                                factsArray.add(fact);
                            }

                            topicNode.set("facts", factsArray);
                            outputArray.add(topicNode);
                        }
                    }

                    node.set("output", outputArray);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);

                } else if (cmd.getCommand().equals("improveEnvironment")) {
                    if (terraBot.getBatteryLevel() < IMPROVE_ENV_COST) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Not enough battery left. "
                                + "Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    String componentName = cmd.getName();
                    Entity componentFound = null;

                    /**
                     * cautam componenta in inventar
                     */
                    if (terraBot.getInventory() != null) {
                        for (Entity e : terraBot.getInventory()) {
                            if (e.getName().equals(componentName)) {
                                componentFound = e;
                                break;
                            }
                        }
                    }

                    if (componentFound == null) {
                        final ObjectNode node = mapper.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    String improvementType = cmd.getImprovementType();
                    String requiredFact = "";

                    /**
                     * pt fiecare tip de improvement vom cauta in facts daca exista Metoda aceea
                     */
                    switch (improvementType) {
                        case "plantVegetation":
                            requiredFact = "Method to plant " + componentName;
                            break;
                        case "fertilizeSoil":
                            requiredFact = "Method to fertilize with " + componentName;
                            break;
                        case "increaseHumidity":
                            requiredFact = "Method to increase humidity.";
                            break;
                        case "increaseMoisture":
                            requiredFact = "Method to increaseMoisture";
                            break;
                        default:
                            requiredFact = "";
                            break;
                    }

                    boolean knowsFact = false;
                    List<String> factsList = terraBot.getKnowledgeBase().get(componentName);
                    if (factsList != null && factsList.contains(requiredFact)) {
                        knowsFact = true;
                    }

                    if (!knowsFact) {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Fact not yet saved. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);

                        cmdIndex++;
                        if (cmdIndex == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    terraBot.setBatteryLevel(terraBot.getBatteryLevel() - IMPROVE_ENV_COST);
                    Entity[] cellSlots = map.getEntityMap()[terraBot.getY()][terraBot.getX()];
                    String successMessage = "";

                    if (improvementType.equals("plantVegetation")) {
                        Air a = (Air) cellSlots[EntitySlot.AIR.idx()];
                        if (a != null) {
                            a.setOxygenLevel(Math.round((a.getOxygenLevel()
                                    + OXYGEN_PLANT_DELTA) * ROUND_FACTOR) / ROUND_FACTOR);
                            a.calculateAirQuality();
                            a.setBlockingPossibility(a.getToxicityLevel());
                        }
                        terraBot.getInventory().remove(componentFound);
                        successMessage = "The " + componentName + " was planted successfully.";

                    } else if (improvementType.equals("fertilizeSoil")) {
                        Soil s = (Soil) cellSlots[EntitySlot.SOIL.idx()];
                        if (s != null) {
                            s.setOrganicMatter(Math.round((s.getOrganicMatter()
                                    + ORGANIC_FERTILIZE_DELTA) * ROUND_FACTOR) / ROUND_FACTOR);
                            s.calculateSoilQuality();
                            s.calculateBlockingPossibility();
                        }
                        terraBot.getInventory().remove(componentFound);
                        successMessage = "The soil was successfully fertilized using "
                                + componentName;

                    } else if (improvementType.equals("increaseHumidity")) {
                        Air a = (Air) cellSlots[EntitySlot.AIR.idx()];
                        if (a != null) {
                            a.setHumidity(Math.round((a.getHumidity()
                                    + HUMIDITY_DELTA) * ROUND_FACTOR) / ROUND_FACTOR);
                            a.calculateAirQuality();
                            a.setBlockingPossibility(a.getToxicityLevel());
                        }
                        terraBot.getInventory().remove(componentFound);
                        successMessage = "The humidity was successfully increased using "
                                + componentName;

                    } else if (improvementType.equals("increaseMoisture")) {
                        Soil s = (Soil) cellSlots[EntitySlot.SOIL.idx()];
                        if (s != null) {
                            s.setWaterRetention(Math.round((s.getWaterRetention()
                                    + MOISTURE_DELTA) * ROUND_FACTOR) / ROUND_FACTOR);
                            s.calculateSoilQuality();
                            s.calculateBlockingPossibility();
                        }
                        terraBot.getInventory().remove(componentFound);
                        successMessage = "The moisture was successfully increased using "
                                + componentName;
                    }

                    ObjectNode node = mapper.createObjectNode();
                    node.put("command", "improveEnvironment");
                    node.put("message", successMessage);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);
                }

                cmdIndex++;
                if (cmdIndex == cmds.size()) {
                    break;
                }
            }
            time++;
        }
    }
}
