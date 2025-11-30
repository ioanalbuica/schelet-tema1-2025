package main.entity;

public enum EntitySlot {
    AIR(0),
    SOIL(1),
    WATER(2),
    PLANT(3),
    ANIMAL(4);

    private final int index;

    EntitySlot(final int index) {
        this.index = index;
    }

    public int idx() {
        return index;
    }
}
