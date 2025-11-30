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
public final class TropicalAir extends Air {
    private double co2Level = 0.0;
    private double rainfall = 0.0;

    public TropicalAir(final AirInput airInput) {
        super(airInput);
        final double roundFactor = 100.0;
        co2Level = Math.round(airInput.getCo2Level() * roundFactor) / roundFactor;
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        final double oxygenMultiplier = 2.0;
        final double humidityMultiplier = 0.5;
        final double co2Multiplier = 0.01;
        final double rainfallMultiplier = 0.3;
        final double maxPercent = 100.0;
        final double tropicalAirMaxScore = 82.0;
        final double airToxicityThreshold = 0.8;

        double oxygen = getOxygenLevel();
        double humidity = getHumidity();

        double rawScore = (oxygen * oxygenMultiplier)
                + (humidity * humidityMultiplier)
                - (co2Level * co2Multiplier);

        double normalizeScore = Math.max(0.0, Math.min(maxPercent, rawScore));
        double finalScore = Math.round(normalizeScore * maxPercent) / maxPercent;

        setAirQuality(finalScore + rainfall * rainfallMultiplier);

        double toxicityAQ = maxPercent * (1.0 - getAirQuality() / tropicalAirMaxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(maxPercent, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * maxPercent) / maxPercent;

        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (airToxicityThreshold * tropicalAirMaxScore));
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
        airNode.put("co2Level", co2Level);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(final String type, final String value) {
        if (type.equals("rainfall")) {
            rainfall = Double.parseDouble(value);
            calculateAirQuality();
        }
    }
}
