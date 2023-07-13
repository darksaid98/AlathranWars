package me.ShermansWorld.AlathranWars.conflict.battle;

public class Battle {
    private int attackerPoints;
    private int defenderPoints;

    public int getAttackerPoints() {
        return this.attackerPoints;
    }

    public void setAttackerPoints(int points) {
        this.attackerPoints = points;
    }

    public int getDefenderPoints() {
        return this.defenderPoints;
    }

    public void setDefenderPoints(int points) {
        this.defenderPoints = points;
    }

    public void addPointsToAttackers(final int points) {
        this.attackerPoints += points;
    }

    public void addPointsToDefenders(final int points) {
        this.defenderPoints += points;
    }
}
