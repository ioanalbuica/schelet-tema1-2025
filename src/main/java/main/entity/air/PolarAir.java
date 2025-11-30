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
public final class PolarAir extends Air {
    private double iceCrystalConcentration;
    private double windSpeed = 0;

    public PolarAir(final AirInput airInput) {
        super(airInput);
        iceCrystalConcentration = airInput.getIceCrystalConcentration();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        final double oxygenMultiplier = 2.0;
        final double baseTemperatureScore = 100.0;
        final double iceCrystalMultiplier = 0.05;
        final double maxPercent = 100.0;
        final double windSpeedMultiplier = 0.2;
        final double maxScore = 142.0;
        final double airToxicityThreshold = 0.8;

        double oxygen = getOxygenLevel();
        double temperature = getTemperature();

        double rawScore = (oxygen * oxygenMultiplier)
                + (baseTemperatureScore - Math.abs(temperature))
                - (iceCrystalConcentration * iceCrystalMultiplier);

        double normalizeScore = Math.max(0.0, Math.min(maxPercent, rawScore));
        double finalScore = Math.round(normalizeScore * maxPercent) / maxPercent;

        setAirQuality(finalScore - windSpeed * windSpeedMultiplier);

        double toxicityAQ = maxPercent * (1.0 - getAirQuality() / maxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(maxPercent, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * maxPercent) / maxPercent;

        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (airToxicityThreshold * maxScore));
    }

    @Override
    public void printEntity(final ObjectMapper mapper, final ObjectNode env) {
        ObjectNode airNode = mapper.createObjectNode();
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
    public void changeWeather(final String type, final String value) {
        if (type.equals("polarStorm")) {
            windSpeed = Double.parseDouble(value);
            calculateAirQuality();
        }
    }
}
