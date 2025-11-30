package main.entity.animal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.Entity;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Herbivores extends Animal {
    private static final double HERBIVORE_ATTACKING_PROBABILITY = 1.5;

    public Herbivores(final AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(HERBIVORE_ATTACKING_PROBABILITY);
    }

    public Herbivores(final Herbivores other) {
        super(other);
    }

    @Override
    public final Entity createDeepCopy() {
        return new Herbivores(this);
    }
}
