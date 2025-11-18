package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AirInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Air extends Entity{
    private double humidity;
    private double temperature;
    private double oxygenLevel;
    private double airQuality;
    private double toxicityLevel;
    private boolean isToxic;

    public Air(AirInput airInput) {
        this.setName(airInput.getName());
        this.setMass(airInput.getMass());
        humidity = airInput.getHumidity();
        temperature = airInput.getTemperature();
        oxygenLevel = airInput.getOxygenLevel();
    }

    abstract void calculateAirQuality();
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TropicalAir extends Air {
    private double co2Level;

    public TropicalAir(AirInput airInput) {
        super(airInput);
        co2Level = airInput.getCo2Level();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    @Override
    void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double rawScore = (oxygen * 2.0) + (humidity * 0.5) - (co2Level * 0.01);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore);

        double maxScore = 82.0;
        double toxicityAQ = 100.0 * (1.0 - rawScore / maxScore);
        double toxicityFinal = Math.round(toxicityAQ * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PolarAir extends Air {
    private double iceCrystalConcentration;

    public PolarAir(AirInput airInput) {
        super(airInput);
        iceCrystalConcentration = airInput.getIceCrystalConcentration();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    @Override
    void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double temperature = getTemperature();
        double rawScore = (oxygen * 2.0) + (100.0 - Math.abs(temperature)) - (iceCrystalConcentration * 0.05);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore);

        double maxScore = 142.0;
        double toxicityAQ = 100.0 * (1.0 - rawScore / maxScore);
        double toxicityFinal = Math.round(toxicityAQ * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TemperateAir extends Air {
    private double pollenLevel;

    public TemperateAir(AirInput airInput) {
        super(airInput);
        pollenLevel = airInput.getPollenLevel();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    @Override
    void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double rawScore = (oxygen * 2.0) + (humidity * 0.7) - (pollenLevel * 0.1);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore);

        double maxScore = 84.0;
        double toxicityAQ = 100.0 * (1.0 - rawScore / maxScore);
        double toxicityFinal = Math.round(toxicityAQ * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class DesertAir extends Air {
    private double dustParticles;

    public DesertAir(AirInput airInput) {
        super(airInput);
        dustParticles = airInput.getDustParticles();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    @Override
    void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double temperature = getTemperature();
        double rawScore = (oxygen * 2.0) - (dustParticles * 0.2) - (temperature * 0.3);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore);

        double maxScore = 65.0;
        double toxicityAQ = 100.0 * (1.0 - rawScore / maxScore);
        double toxicityFinal = Math.round(toxicityAQ * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class MountainAir extends Air {
    private double altitude;

    public MountainAir(AirInput airInput) {
        super(airInput);
        altitude = airInput.getAltitude();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }

    @Override
    void calculateAirQuality() {
        double oxygen = getOxygenLevel();
        double humidity = getHumidity();
        double oxygenFactor = oxygen - ((altitude / 1000.0) * 0.5);
        double rawScore = (oxygenFactor * 2.0) + (humidity * 0.6);
        double normalizeScore = Math.max(0.0, Math.min(100.0, rawScore));
        double finalScore = Math.round(normalizeScore * 100.0) / 100.0;
        setAirQuality(finalScore);

        double maxScore = 78.0;
        double toxicityAQ = 100.0 * (1.0 - rawScore / maxScore);
        double toxicityFinal = Math.round(toxicityAQ * 100.0) / 100.0;
        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (0.8 * maxScore));
    }
}

