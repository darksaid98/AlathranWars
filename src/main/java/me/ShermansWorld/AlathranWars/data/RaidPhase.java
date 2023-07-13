package me.ShermansWorld.AlathranWars.data;

public enum RaidPhase {
    START(0, 0),
    GATHER(1, 0),
    TRAVEL(2, 18000),
    COMBAT(3, 24000),
    END(4, 72000);

    //Start phase starts when the command to start a raid is ran, it allows the raiders to join, and for people to getInstance geared up
    //after 15 minutes travel phase begins, where the raiders start travelling and moving to the new town.
    //no killing can occur during these two phases.
    //Once at least one player has stepped into the defending town, or 5 min have passed during the travel phase,
    //the combat begins and lasts 40 minutes.

    public final int id;
    public final int startTick;

    RaidPhase(int id, int startTick) {
        this.id = id;
        this.startTick = startTick;
    }

    /**
     * Grabs the phase by name, ignores case
     *
     * @param name
     * @return
     */
    public static RaidPhase getByName(String name) {
        for (RaidPhase p : RaidPhase.values()) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /**
     * Grabs phase by id number for easy storage reading
     *
     * @param id
     * @return
     */
    public static RaidPhase getByID(int id) {
        for (RaidPhase p : RaidPhase.values()) {
            if (p.id == id) return p;
        }
        return null;
    }

    public static RaidPhase getNext(RaidPhase phase) {
        switch (phase) {
            case START -> {
                return GATHER;
            }
            case GATHER -> {
                return TRAVEL;
            }
            case TRAVEL -> {
                return COMBAT;
            }
            case COMBAT -> {
                return END;
            }
            default -> {
                return null;
            }
        }
    }
}