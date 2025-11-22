package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.InputLoader;
import fileio.SimulationInput;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {

    private Main() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final ObjectWriter WRITER = MAPPER.writer().withDefaultPrettyPrinter();

    /**
     * @param inputPath input file path
     * @param outputPath output file path
     * @throws IOException when files cannot be loaded.
     */
    public static void action(final String inputPath,
                              final String outputPath) throws IOException {

        InputLoader inputLoader = new InputLoader(inputPath);
        ArrayNode output = MAPPER.createArrayNode();
        ArrayList<SimulationInput> sims = inputLoader.getSimulations();
        ArrayList<CommandInput> cmds = inputLoader.getCommands();

        Map map = null;
        TerraBot terraBot = null;
        int sim_index = -1;
        boolean startedSim = false;
        for (CommandInput cmd : cmds) {
            // inside your for (CommandInput cmd : cmds) loop
            if (cmd.getCommand().equals("startSimulation")) {
                startedSim = true;
                sim_index++;
                SimulationInput sim = sims.get(sim_index);
                map = new Map(sim);
                terraBot = new TerraBot();
                terraBot.setBatteryLevel(sim.getEnergyPoints());
                // add startSimulation output
                ObjectNode startNode = MAPPER.createObjectNode();
                startNode.put("command", "startSimulation");
                startNode.put("message", "Simulation has started.");
                startNode.put("timestamp", cmd.getTimestamp());
                output.add(startNode);
            } else if (startedSim == false){
                ObjectNode endNode = MAPPER.createObjectNode();
                endNode.put("command", cmd.getCommand());
                endNode.put("message", "ERROR: Simulation not started. Cannot perform action");
                endNode.put("timestamp", cmd.getTimestamp());
                output.add(endNode);
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

                // iterate through all sections (assumes map has getWidth/getHeight)
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

                        // Example: compute / fetch airQuality and soilQuality for the (x,y) section.
                        // Replace with your actual computation / accessors:
                        sectionNode.put("airQuality", map.getAirQualityLabel(x, y));   // e.g. "good","moderate"
                        sectionNode.put("soilQuality", map.getSoilQualityLabel(x, y)); // e.g. "poor","good"

                        mapArray.add(sectionNode);
                    }
                }

                node.set("output", mapArray);
                node.put("timestamp", cmd.getTimestamp());
                output.add(node);
            } else if (cmd.getCommand().equals("endSimulation")) {
                startedSim = false;
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
                    Node.put("message", "The robot has successfully moved to position (" + terraBot.getX() + ", " + terraBot.getY() + ").");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                } else {
                    ObjectNode Node = MAPPER.createObjectNode();
                    Node.put("command", "moveRobot");
                    Node.put("message", "ERROR: Not enough battery left. Cannot perform action");
                    Node.put("timestamp", cmd.getTimestamp());
                    output.add(Node);
                }
            }

        }
        /*
         * TODO Implement your function here
         *
         * How to add output to the output array?
         * There are multiple ways to do this, here is one example:
         *
         *
         * ObjectNode objectNode = MAPPER.createObjectNode();
         * objectNode.put("field_name", "field_value");
         *
         * ArrayNode arrayNode = MAPPER.createArrayNode();
         * arrayNode.add(objectNode);
         *
         * output.add(arrayNode);
         * output.add(objectNode);
         *
         */

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        WRITER.writeValue(outputFile, output);
    }
}
