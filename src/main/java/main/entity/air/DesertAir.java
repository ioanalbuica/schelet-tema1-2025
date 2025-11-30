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
public class DesertAir extends Air {
    private double dustParticles;
    private boolean desertStorm = false;

    public DesertAir(final AirInput airInput) {
        super(airInput);
        dustParticles = airInput.getDustParticles();
        calculateAirQuality();
        setBlockingPossibility(getToxicityLevel());
    }

    @Override
    public final void calculateAirQuality() {
        final double oxygenMultiplier = 2.0;
        final double dustParticlesMultiplier = 0.2;
        final double temperatureMultiplier = 0.3;
        final double maxPercent = 100.0;
        final double desertStormEffect = 30.0;
        final double desertAirMaxScore = 65.0;
        final double airToxicityThreshold = 0.8;

        double oxygen = getOxygenLevel();
        double temperature = getTemperature();

        double rawScore = (oxygen * oxygenMultiplier)
                - (dustParticles * dustParticlesMultiplier)
                - (temperature * temperatureMultiplier);

        double normalizeScore = Math.max(0.0, Math.min(maxPercent, rawScore));
        double finalScore = Math.round(normalizeScore * maxPercent) / maxPercent;

        setAirQuality(finalScore - (desertStorm ? desertStormEffect : 0));

        double toxicityAQ = maxPercent * (1.0 - getAirQuality() / desertAirMaxScore);
        double normalizeToxicity = Math.max(0.0, Math.min(maxPercent, toxicityAQ));
        double toxicityFinal = Math.round(normalizeToxicity * maxPercent) / maxPercent;

        setToxicityLevel(toxicityFinal);
        setToxic(toxicityFinal > (airToxicityThreshold * desertAirMaxScore));
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
        airNode.put("desertStorm", desertStorm);
        env.set("air", airNode);
    }

    /**
     * se recalculeaza mereu calitate aerului dupa aplicarea efectului
     */
    @Override
    public final void changeWeather(final String type, final String value) {
        if (type.equals("desertStorm")) {
            desertStorm = Boolean.parseBoolean(value);
            calculateAirQuality();
        }
    }
}
