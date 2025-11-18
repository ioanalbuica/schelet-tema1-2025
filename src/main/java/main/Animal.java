package main;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.AnimalInput;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class Animal extends Entity {
    private String type;
    private String state;
    private double attackProbability;

    public Animal(AnimalInput animalInput) {
        type = animalInput.getType();
        setName(animalInput.getName());
        setMass(animalInput.getMass());
    }
    abstract void calculateAttackProbability();
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Herbivores extends Animal {
    public Herbivores(AnimalInput animalInput) {
        super(animalInput);
    }

    public void calculateAttackProbability() {
        setAttackProbability(1.5);
    }
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Carnivores extends Animal {
    public Carnivores(AnimalInput animalInput) {
        super(animalInput);
    }

    public void calculateAttackProbability() {
        setAttackProbability(7);
    }
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Omnivores extends Animal {
    public Omnivores(AnimalInput animalInput) {
        super(animalInput);
    }

    public void calculateAttackProbability() {
        setAttackProbability(4);
    }
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Detritivores extends Animal {
    public Detritivores(AnimalInput animalInput) {
        super(animalInput);
    }

    public void calculateAttackProbability() {
        setAttackProbability(1);
    }
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }
}

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Parasites extends Animal {
    public Parasites(AnimalInput animalInput) {
        super(animalInput);
    }

    public void calculateAttackProbability() {
        setAttackProbability(9);
    }
    @Override
    public void changeEnvironment(LinkedList<Entity> entitiesList) {

    }
}



