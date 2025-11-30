package main.entity.animal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.Entity;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Detritivores extends Animal {
    private static final double DETRIVORE_ATTACKING_PROBABILITY = 1.0;

    public Detritivores(final AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(DETRIVORE_ATTACKING_PROBABILITY);
    }

    public Detritivores(final Detritivores other) {
        super(other);
    }

    @Override
    public final Entity createDeepCopy() {
        return new Detritivores(this);
    }
}
