package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.WaterInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;
import static java.lang.Math.abs;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Water extends Entity{
    private double salinity;
    private double pH;
    private double purity;
    private double turbidity;
    private double contaminantIndex;
    private boolean isFrozen;
    private double waterQuality;

    public void calculateWaterQuality() {
        double purity_score        = purity / 100;
        double pH_score            = 1 - abs(pH - 7.5) / 7.5;
        double salinity_score      = 1 - (salinity / 350);
        double turbidity_score     = 1 - (turbidity / 100);
        double contaminant_score   = 1 - (contaminantIndex / 100);
        double frozen_score        = isFrozen ? 0 : 1;
        waterQuality = (0.3 * purity_score + 0.2 * pH_score + 0.15 * salinity_score + 0.1 * turbidity_score
                        + 0.15 * contaminant_score + 0.2 * frozen_score) * 100;
    }

    public Water(WaterInput waterInput) {
        this.setMass(waterInput.getMass());
        this.setName(waterInput.getName());
        salinity = waterInput.getSalinity();
        pH = waterInput.getPH();
        purity = waterInput.getPurity();
        turbidity = waterInput.getTurbidity();
        contaminantIndex = waterInput.getContaminantIndex();
        isFrozen = waterInput.isFrozen();
    }

    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }





}
