package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.AirInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Air extends Entity {
    private String type;
    private double humidity;
    private double temperature;
    private double oxygenLevel;
    private double airQuality;
    private double toxicityLevel;
    private boolean isToxic;

    public Air(AirInput airInput) {
        type = airInput.getType();
        this.setName(airInput.getName());
        this.setMass(airInput.getMass());
        humidity = airInput.getHumidity();
        temperature = airInput.getTemperature();
        oxygenLevel = airInput.getOxygenLevel();
    }

    abstract public void calculateAirQuality();
    abstract public void changeWeather(String type, String value);

    @Override
    public void changeEnvironment(int currentTime, LinkedList<Entity> entitiesList) {
        for (Entity entity : entitiesList) {
            if (entity instanceof Animal && entity.getScannedTime() != 0) {
                if (isToxic) {
                    ((Animal)entity).setState("sick");
                }
            }
        }
    }

    @Override
    public Entity createDeepCopy() {
        return null;
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TropicalAir extends Air {
    private double co2Level = 0.0;
    private double rainfall = 0;

    public TropicalAir(AirInput airInput) {
        super(airInput);
        co2Level = Math.round(airInput.getCo2Level() * 100.0) / 100.0;
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double rawScore = (oxygen * 2.0) + (humidity * 0.5) - (co2Level * 0.01);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore + rainfall * 0.3);

        double maxScore = 82.0;
        double toxicityAQ = 100.0 * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(100.0, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode airNode = MAPPER.createObjectNode();
        airNode.put("type", getType());
        airNode.put("name", getName());
        airNode.put("mass", getMass());
        airNode.put("humidity", getHumidity());
        airNode.put("temperature", getTemperature());
        airNode.put("oxygenLevel", getOxygenLevel());
        airNode.put("airQuality", getAirQuality());
        airNode.put("co2Level", co2Level);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(String type, String value) {
        if (type.equals("rainfall")) {
            rainfall = Double.parseDouble(value);
            calculateAirQuality();
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PolarAir extends Air {
    private double iceCrystalConcentration;
    private double windSpeed = 0;

    public PolarAir(AirInput airInput) {
        super(airInput);
        iceCrystalConcentration = airInput.getIceCrystalConcentration();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double temperature = getTemperature();
        double rawScore = (oxygen * 2.0) + (100.0 - Math.abs(temperature)) - (iceCrystalConcentration * 0.05);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore - windSpeed * 0.2);

        double maxScore = 142.0;
        double toxicityAQ = 100.0 * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(100.0, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode airNode = MAPPER.createObjectNode();
        airNode.put("type", getType());
        airNode.put("name", getName());
        airNode.put("mass", getMass());
        airNode.put("humidity", getHumidity());
        airNode.put("temperature", getTemperature());
        airNode.put("oxygenLevel", getOxygenLevel());
        airNode.put("airQuality", getAirQuality());
        airNode.put("iceCrystalConcentration", iceCrystalConcentration);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(String type, String value) {
        if (type.equals("polarStorm")) {
            windSpeed = Double.parseDouble(value);
            calculateAirQuality();
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TemperateAir extends Air {
    private double pollenLevel;
    private String season = "nothing";

    public TemperateAir(AirInput airInput) {
        super(airInput);
        pollenLevel = airInput.getPollenLevel();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double rawScore = (oxygen * 2.0) + (humidity * 0.7) - (pollenLevel * 0.1);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        double seasonPenality = season.equalsIgnoreCase("Spring")  ? 15 : 0;
        setAirQuality(finalScore - seasonPenality);

        double maxScore = 84.0;
        double toxicityAQ = 100.0 * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(100.0, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode airNode = MAPPER.createObjectNode();
        airNode.put("type", getType());
        airNode.put("name", getName());
        airNode.put("mass", getMass());
        airNode.put("humidity", getHumidity());
        airNode.put("temperature", getTemperature());
        airNode.put("oxygenLevel", getOxygenLevel());
        airNode.put("airQuality", getAirQuality());
        airNode.put("pollenLevel", pollenLevel);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(String type, String value) {
        if (type.equals("newSeason")) {
            season = value;
            calculateAirQuality();
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class DesertAir extends Air {
    private double dustParticles;
    private boolean desertStorm = false;

    public DesertAir(AirInput airInput) {
        super(airInput);
        dustParticles = airInput.getDustParticles();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double temperature = getTemperature();
        double rawScore = (oxygen * 2.0) - (dustParticles * 0.2) - (temperature * 0.3);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore - (desertStorm ? 30 : 0));

        double maxScore = 65.0;
        double toxicityAQ = 100.0 * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(100.0, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode airNode = MAPPER.createObjectNode();
        airNode.put("type", getType());
        airNode.put("name", getName());
        airNode.put("mass", getMass());
        airNode.put("humidity", getHumidity());
        airNode.put("temperature", getTemperature());
        airNode.put("oxygenLevel", getOxygenLevel());
        airNode.put("airQuality", getAirQuality());
        airNode.put("desertStorm", desertStorm);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(String type, String value) {
        if (type.equals("desertStorm")) {
            desertStorm = Boolean.parseBoolean(value);
            calculateAirQuality();
        }
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class MountainAir extends Air {
    private double altitude;
    private int numberOfHikers = 0;

    public MountainAir(AirInput airInput) {
        super(airInput);
        altitude = airInput.getAltitude();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double oxygenFactor = oxygen - ((altitude / 1000.0) * 0.5);
        double rawScore = (oxygenFactor * 2.0) + (humidity * 0.6);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore - (double)numberOfHikers * 0.1);

        double maxScore = 78.0;
        double toxicityAQ = 100.0 * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(100.0, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }

    @Override
    public void printEntity(ObjectMapper MAPPER, ObjectNode env) {
        ObjectNode airNode = MAPPER.createObjectNode();
        airNode.put("type", getType());
        airNode.put("name", getName());
        airNode.put("mass", getMass());
        airNode.put("humidity", getHumidity());
        airNode.put("temperature", getTemperature());
        airNode.put("oxygenLevel", getOxygenLevel());
        airNode.put("airQuality", getAirQuality());
        airNode.put("altitude", altitude);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(String type, String value) {
        if (type.equals("peopleHiking")) {
            numberOfHikers = Integer.parseInt(value);
            calculateAirQuality();
        }
    }
}

