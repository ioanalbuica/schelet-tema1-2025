package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class Mission {
    private SimulationMap map = null;
    private TerraBot terraBot = null;

    public void run(ArrayList<SimulationInput> sims, ArrayList<CommandInput> cmds, ObjectMapper MAPPER, ArrayNode output) {
        int sim_index = -1, cmd_index = 0, time = 1;
        boolean startedSim = false, weatherChangeOver = true;
        int chargingStartTimestemp = -1, chargingDuration = 0, changedWeatherStart = 0;
        String weatherType = null;
        while (true) {
            if (map != null) {
                map.actualiseMap(time);
                map.feedAnimals();
                map.moveAnimals(time);
            }

            CommandInput cmd = cmds.get(cmd_index);
            if (cmd.getTimestamp() == time) {
                if (cmd.getCommand().equals("startSimulation")) {
                    if (startedSim) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", cmd.getCommand());
                        node.put("message", "ERROR: Simulation already started. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }
                    startedSim = true;
                    sim_index++;
                    SimulationInput sim = sims.get(sim_index);
                    map = new SimulationMap(sim);
                    terraBot = new TerraBot();
                    terraBot.setBatteryLevel(sim.getEnergyPoints());

                    ObjectNode startNode = MAPPER.createObjectNode();
                    startNode.put("command", "startSimulation");
                    startNode.put("message", "Simulation has started.");
                    startNode.put("timestamp", cmd.getTimestamp());
                    output.add(startNode);
                } else if (!startedSim){
                    ObjectNode node = MAPPER.createObjectNode();
                    node.put("command", cmd.getCommand());
                    node.put("message", "ERROR: Simulation not started. Cannot perform action");
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);
                } else if (cmd.getTimestamp() - chargingStartTimestemp < chargingDuration) {
                    ObjectNode Node = MAPPER.createObjectNode();
                    Node.put("command", cmd.getCommand());
                    Node.put("message", "ERROR: Robot still charging. Cannot perform action");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                } else if (cmd.getCommand().equals("printEnvConditions")) {
                    ObjectNode node = MAPPER.createObjectNode();
                    node.put("command", "printEnvConditions");

                    ObjectNode env = MAPPER.createObjectNode();
                    for (Entity e : map.getEntityMap().get(terraBot.getY()).get(terraBot.getX())) {
                        e.printEntity(MAPPER, env);
                    }
                    node.set("output", env);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);
                } else if (cmd.getCommand().equals("printMap")) {
                    ObjectNode node = MAPPER.createObjectNode();
                    node.put("command", "printMap");

                    ArrayNode mapArray = MAPPER.createArrayNode();

                    for (int y = 0; y < map.getHeight(); y++) {
                        for (int x = 0; x < map.getWidth(); x++) {
                            ObjectNode sectionNode = MAPPER.createObjectNode();

                            ArrayNode sectionCoords = MAPPER.createArrayNode();
                            sectionCoords.add(x);
                            sectionCoords.add(y);
                            sectionNode.set("section", sectionCoords);

                            int nr = 0;
                            java.util.List<Entity> entities = map.getEntityMap().get(y).get(x);
                            for (Entity e : entities) {
                                if (e instanceof Plant || e instanceof Animal || e instanceof Water) {
                                    nr++;
                                }
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
                    chargingStartTimestemp = -1;
                    chargingDuration = 0;
                    ObjectNode endNode = MAPPER.createObjectNode();
                    endNode.put("command", "endSimulation");
                    endNode.put("message", "Simulation has ended.");
                    endNode.put("timestamp", cmd.getTimestamp());
                    output.add(endNode);
                } else if (cmd.getCommand().equals("moveRobot")) {
                    int rez = terraBot.move(map);
                    if (rez == 0) {
                        ObjectNode Node = MAPPER.createObjectNode();
                        Node.put("command", "moveRobot");

                        // Add the debug map here
                        // Node.set("blockingMap", fullMapBlockingNode);

                        Node.put("message", "The robot has successfully moved to position (" + terraBot.getX() + ", " + terraBot.getY() + ").");
                        Node.put("timestamp", cmd.getTimestamp());
                        output.add(Node);
                    } else {
                        ObjectNode Node = MAPPER.createObjectNode();
                        Node.put("command", "moveRobot");

                        // Add the debug map here as well (useful to see why it might be stuck)
                        // Node.set("blockingMap", fullMapBlockingNode);

                        Node.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        Node.put("timestamp", cmd.getTimestamp());
                        output.add(Node);
                    }
                }
                else if (cmd.getCommand().equals("getEnergyStatus")) {
                    ObjectNode Node = MAPPER.createObjectNode();
                    Node.put("command", "getEnergyStatus");
                    Node.put("message", "TerraBot has " + terraBot.getBatteryLevel() + " energy points left.");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                } else if (cmd.getCommand().equals("rechargeBattery")) {
                    chargingStartTimestemp = cmd.getTimestamp();
                    chargingDuration = cmd.getTimeToCharge();
                    terraBot.setBatteryLevel(terraBot.getBatteryLevel() + chargingDuration);

                    ObjectNode Node = MAPPER.createObjectNode();
                    Node.put("command", "rechargeBattery");
                    Node.put("message", "Robot battery is charging.");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                } else if (cmd.getCommand().equals("changeWeatherConditions")) {
                    weatherChangeOver = false;
                    String value = "0";
                    weatherType = cmd.getType();
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
                    map.changeMapWeather(weatherType, value);
                    changedWeatherStart = cmd.getTimestamp();

                    ObjectNode Node = MAPPER.createObjectNode();
                    Node.put("command", "changeWeatherConditions");
                    Node.put("message", "The weather has changed.");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                } else if (!weatherChangeOver && time - changedWeatherStart >= 2) {
                    weatherChangeOver = true;
                    map.changeMapWeather(weatherType, "0");
                } else if (cmd.getCommand().equals("scanObject")) {
                    if (terraBot.getBatteryLevel() < 7) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "scanObject");
                        node.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }
                    boolean found = false;
                    String type = null;
                    if (!cmd.getSound().equals("none")) {
                        for (Entity entity : map.getEntityMap().get(terraBot.getY()).get(terraBot.getX())) {
                            if (entity instanceof Animal) {
                                entity.setScannedTime(time);
                                type = "an animal.";
                                found = true;
                                terraBot.setBatteryLevel(terraBot.getBatteryLevel() - 7);
                                terraBot.getInventory().add(entity.createDeepCopy());
                            }
                        }
                    } else if (!cmd.getSmell().equals("none")) {
                        for (Entity entity : map.getEntityMap().get(terraBot.getY()).get(terraBot.getX())) {
                            if (entity instanceof Plant) {
                                entity.setScannedTime(time);
                                type = "a plant.";
                                found = true;
                                terraBot.setBatteryLevel(terraBot.getBatteryLevel() - 7);
                                terraBot.getInventory().add(entity.createDeepCopy());
                            }
                        }
                    } else {
                        for (Entity entity : map.getEntityMap().get(terraBot.getY()).get(terraBot.getX())) {
                            if (entity instanceof Water) {
                                entity.setScannedTime(time);
                                type = "water.";
                                found = true;
                                terraBot.setBatteryLevel(terraBot.getBatteryLevel() - 7);
                                terraBot.getInventory().add(entity.createDeepCopy());
                            }
                        }
                    }
                    if (!found) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "scanObject");
                        node.put("message", "ERROR: Object not found. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                    } else {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "scanObject");
                        node.put("message", "The scanned object is " + type);
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                    }
                } else if (cmd.getCommand().equals("learnFact")) {
                    if (terraBot.getBatteryLevel() < 2) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "learnFact");
                        node.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    boolean isScanned = false;
                    for (Entity entity : terraBot.getInventory()) {
                        if (entity.getName().equals(cmd.getComponents())) {
                            isScanned = true;
                            break;
                        }
                    }
                    if (isScanned) {
                        terraBot.setBatteryLevel(terraBot.getBatteryLevel() - 2);
                        terraBot.addFact(cmd.getComponents(), cmd.getSubject());

                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "learnFact");
                        node.put("message", "The fact has been successfully saved in the database.");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                    } else {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "learnFact");
                        node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                    }
                } else if (cmd.getCommand().equals("printKnowledgeBase")) {
                    ObjectNode node = MAPPER.createObjectNode();
                    node.put("command", "printKnowledgeBase");

                    ArrayNode outputArray = MAPPER.createArrayNode();
                    List<String> processedTopics = new ArrayList<>();

                    if (terraBot.getInventory() != null) {
                        for (Entity entity : terraBot.getInventory()) {
                            String topicName = entity.getName();

                            if (!processedTopics.contains(topicName) && terraBot.getKnowledgeBase().containsKey(topicName)) {
                                ObjectNode topicNode = MAPPER.createObjectNode();
                                topicNode.put("topic", topicName);

                                ArrayNode factsArray = MAPPER.createArrayNode();
                                List<String> facts = terraBot.getKnowledgeBase().get(topicName);
                                for (String fact : facts) {
                                    factsArray.add(fact);
                                }

                                topicNode.set("facts", factsArray);
                                outputArray.add(topicNode);
                                processedTopics.add(topicName);
                            }
                        }
                    }
                    node.set("output", outputArray);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);
                } else if (cmd.getCommand().equals("improveEnvironment")) {
                    if (terraBot.getBatteryLevel() < 10) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    String componentName = cmd.getName();
                    Entity componentFound = null;

                    if (terraBot.getInventory() != null) {
                        for (Entity e : terraBot.getInventory()) {
                            if (e.getName().equals(componentName)) {
                                componentFound = e;
                                break;
                            }
                        }
                    }

                    if (componentFound == null) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    String improvementType = cmd.getImprovementType();
                    String requiredFact = "";

                    switch (improvementType) {
                        case "plantVegetation":
                            requiredFact = "Method to plant " + componentName;
                            break;
                        case "fertilizeSoil":
                            requiredFact = "Method to fertilize " + componentName;
                            break;
                        case "increaseHumidity":
                            requiredFact = "Method to increase humidity " + componentName;
                            break;
                        case "increaseMoisture":
                            requiredFact = "Method to increaseMoisture";
                            break;
                    }

                    boolean knowsFact = false;
                    if (terraBot.getKnowledgeBase().containsKey(componentName)) {
                        List<String> facts = terraBot.getKnowledgeBase().get(componentName);
                        if (facts.contains(requiredFact)) {
                            knowsFact = true;
                        }
                    }

                    if (!knowsFact) {
                        ObjectNode node = MAPPER.createObjectNode();
                        node.put("command", "improveEnvironment");
                        node.put("message", "ERROR: Fact not yet saved. Cannot perform action");
                        node.put("timestamp", cmd.getTimestamp());
                        output.add(node);
                        cmd_index++;
                        if (cmd_index == cmds.size()) {
                            break;
                        }
                        time++;
                        continue;
                    }

                    terraBot.setBatteryLevel(terraBot.getBatteryLevel() - 10);
                    LinkedList<Entity> cellEntities = map.getEntityMap().get(terraBot.getY()).get(terraBot.getX());
                    String successMessage = "";

                    if (improvementType.equals("plantVegetation")) {
                        for (Entity e : cellEntities) {
                            if (e instanceof Air a) {
                                a.setOxygenLevel(Math.round((a.getOxygenLevel() + 0.3) * 100.0) / 100.0);
                                a.calculateAirQuality();
                                a.setBlockingPossibility(a.getToxicityLevel());
                            }
                        }
                        successMessage = "The " + componentName + " was planted successfully.";
                    } else if (improvementType.equals("fertilizeSoil")) {
                        for (Entity e : cellEntities) {
                            if (e instanceof Soil s) {
                                s.setOrganicMatter(Math.round((s.getOrganicMatter() + 0.3) * 100.0) / 100.0);
                                s.calculateSoilQuality();
                                s.calculateBlockingPossibility();
                            }
                        }
                        successMessage = "The soil was successfully fertilized using " + componentName + ".";
                    } else if (improvementType.equals("increaseHumidity")) {
                        for (Entity e : cellEntities) {
                            if (e instanceof Air a) {
                                a.setHumidity(Math.round((a.getHumidity() + 0.2) * 100.0) / 100.0);
                                a.calculateAirQuality();
                                a.setBlockingPossibility(a.getToxicityLevel());
                            }
                        }
                        successMessage = "The humidity was successfully increased using " + componentName + ".";
                    } else if (improvementType.equals("increaseMoisture")) {
                        for (Entity e : cellEntities) {
                            if (e instanceof Soil s) {
                                s.setWaterRetention(Math.round((s.getWaterRetention() + 0.2) * 100.0) / 100.0);
                                s.calculateSoilQuality();
                                s.calculateBlockingPossibility();
                            }
                        }
                        successMessage = "The moisture was successfully increased using " + componentName;
                    }

                    ObjectNode node = MAPPER.createObjectNode();
                    node.put("command", "improveEnvironment");
                    node.put("message", successMessage);
                    node.put("timestamp", cmd.getTimestamp());
                    output.add(node);
                }
                cmd_index++;
                if (cmd_index == cmds.size()) {
                    break;
                }
            }
            time++;
        }
    }
}
