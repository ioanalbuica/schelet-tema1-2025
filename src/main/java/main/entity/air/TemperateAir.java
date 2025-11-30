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
public final class TemperateAir extends Air {
    private double pollenLevel;
    private String season = "nothing";

    public TemperateAir(final AirInput airInput) {
        super(airInput);
        pollenLevel = airInput.getPollenLevel();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public void calculateAirQuality() {
        final double oxygenMultiplier = 2.0;
        final double humidityMultiplier = 0.7;
        final double pollenMultiplier = 0.1;
        final double maxPercent = 100.0;
        final double springSeasonPenalty = 15.0;
        final double temperateAirMaxScore = 84.0;
        final double airToxicityThreshold = 0.8;

        double oxygen = getOxygenLevel();
        double humidity = getHumidity();

        double rawScore = (oxygen * oxygenMultiplier)
                + (humidity * humidityMultiplier)
                - (pollenLevel * pollenMultiplier);

        double normalizeScore = Math.max(0.0, Math.min(maxPercent, rawScore));
        double finalScore = Math.round(normalizeScore * maxPercent) / maxPercent;

        double seasonPenalty = season.equalsIgnoreCase("Spring") ? springSeasonPenalty : 0.0;
        setAirQuality(finalScore - seasonPenalty);

        double toxicityAQ = maxPercent * (1.0 - getAirQuality() / temperateAirMaxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(maxPercent, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * maxPercent) / maxPercent;

        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (airToxicityThreshold * temperateAirMaxScore));
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
        airNode.put("pollenLevel", pollenLevel);
        env.set("air", airNode);
    }

    @Override
    public void changeWeather(final String type, final String value) {
        if (type.equals("newSeason")) {
            season = value;
            calculateAirQuality();
        }
    }
}
