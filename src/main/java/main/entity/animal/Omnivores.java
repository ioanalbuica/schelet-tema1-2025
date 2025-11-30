package main.entity.animal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.entity.Entity;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Omnivores extends Animal {
    private static final double OMNIVORE_ATTACKING_PROBABILITY = 4.0;

    public Omnivores(final AnimalInput animalInput) {
        super(animalInput);
        setBlockingPossibility(OMNIVORE_ATTACKING_PROBABILITY);
    }

    public Omnivores(final Omnivores other) {
        super(other);
    }

    @Override
    public final Entity createDeepCopy() {
        return new Omnivores(this);
    }
}
