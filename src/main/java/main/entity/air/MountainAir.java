package main.entity.air;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.AirInput;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MountainAir extends Air {
    private double altitude;
    private int numberOfHikers = 0;

    public MountainAir(final AirInput airInput) {
        super(airInput);
        altitude = airInput.getAltitude();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public final void calculateAirQuality() {
        final double altitudeDivide = 1000.0;
        final double altitudeMultiplier = 0.5;
        final double oxygenMultiplier = 2.0;
        final double humidityMultiplier = 0.6;
        final double maxPercent = 100.0;
        final double numberHikersMultiplier = 0.1;
        final double mountainAirMaxScore = 78.0;
        final double airToxicityThreshold = 0.8;

        double oxygen = getOxygenLevel();
        double humidity = getHumidity();

        double oxygenFactor = oxygen - ((altitude / altitudeDivide) * altitudeMultiplier);
        double rawScore = (oxygenFactor * oxygenMultiplier)
                + (humidityMultiplier * humidity);

        double normalizeScore = Math.max(0.0, Math.min(maxPercent, rawScore));
        double finalScore = Math.round(normalizeScore * maxPercent) / maxPercent;

        setAirQuality(finalScore - (double) numberOfHikers * numberHikersMultiplier);

        double toxicityAQ = maxPercent * (1.0 - getAirQuality() / mountainAirMaxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(maxPercent, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * maxPercent) / maxPercent;

        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (airToxicityThreshold * mountainAirMaxScore));
    }

    @Override
    public final void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        ObjectNode airNode = mapper.createObjectNode();
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
    public final void changeWeather(final String type, final String value) {
        if (type.equals("peopleHiking")) {
            numberOfHikers = Integer.parseInt(value);
            calculateAirQuality();
        }
    }
}
