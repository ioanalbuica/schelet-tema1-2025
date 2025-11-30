package main.entity.air;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AirInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.SimulationMap;
import main.entity.Entity;
import main.entity.EntitySlot;
import main.entity.animal.Animal;

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

    public Air(final AirInput airInput) {
        type = airInput.getType();
        setName(airInput.getName());
        setMass(airInput.getMass());
        humidity = airInput.getHumidity();
        temperature = airInput.getTemperature();
        oxygenLevel = airInput.getOxygenLevel();
    }

    public abstract void calculateAirQuality();

    /**
     *
     * @param weatherType tipul efectului de schimbat vremea
     * @param value va fi convertit la tipul de date folosit de efectul respectiv
     * fiecare subclasa va avea deja inclus in forma pt calitatea aerului efectul meteo
     * acesta va setat default pe 0 si se va actualiza cand se va comanda changeWeather
     */
    public abstract void changeWeather(String weatherType, String value);

    @Override
    public final void changeEnvironment(final int currentTime, final SimulationMap map,
                                        final int x, final int y) {
        Animal animal = (Animal) map.getEntityMap()[y][x][EntitySlot.ANIMAL.idx()];
        if (animal != null && animal.getScannedTime() != 0 && isToxic) {
            animal.setState("sick");
        }
    }

    /**
     * nu creeam copy pt aer pt ca nu il bagam in inventar
     */
    @Override
    public final Entity createDeepCopy() {
        return null;
    }
}
