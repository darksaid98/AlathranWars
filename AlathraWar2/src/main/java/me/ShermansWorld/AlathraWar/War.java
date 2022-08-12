package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

public class War {
    private String name;
    private String side1;
    private String side2;
    private ArrayList<String> side1Players;
    private ArrayList<String> side2Players;
    
    public War(final String name, final String side1, final String side2) {
        this.side1Players = new ArrayList<String>();
        this.side2Players = new ArrayList<String>();
        this.name = name;
        this.side1 = side1;
        this.side2 = side2;
    }
    
    public void addPlayerSide1(final String playerName) {
        boolean duplicate = false;
        for (int i = 0; i < this.side1Players.size(); ++i) {
            if (playerName.equalsIgnoreCase(this.side1Players.get(i))) {
                duplicate = true;
            }
        }
        if (!duplicate) {
            this.side1Players.add(playerName);
        }
    }
    
    public void removePlayerSide1(final String playerName) {
        for (int i = 0; i < this.side1Players.size(); ++i) {
            if (playerName.equalsIgnoreCase(this.side1Players.get(i))) {
                this.side1Players.remove(i);
                return;
            }
        }
    }
    
    public void addPlayerSide2(final String playerName) {
        boolean duplicate = false;
        for (int i = 0; i < this.side2Players.size(); ++i) {
            if (playerName.equalsIgnoreCase(this.side2Players.get(i))) {
                duplicate = true;
            }
        }
        if (!duplicate) {
            this.side2Players.add(playerName);
        }
    }
    
    public void removePlayerSide2(final String playerName) {
        for (int i = 0; i < this.side2Players.size(); ++i) {
            if (playerName.equalsIgnoreCase(this.side2Players.get(i))) {
                this.side2Players.remove(i);
                return;
            }
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getSide1() {
        return this.side1;
    }
    
    public void setSide1(final String side1) {
        this.side1 = side1;
    }
    
    public String getSide2() {
        return this.side2;
    }
    
    public void setSide2(final String side2) {
        this.side2 = this.side1;
    }
    
    public ArrayList<String> getSide1Players() {
        return this.side1Players;
    }
    
    public void setSide1Players(final ArrayList<String> side1Players) {
        this.side1Players = side1Players;
    }
    
    public ArrayList<String> getSide2Players() {
        return this.side2Players;
    }
    
    public void setSide2Players(final ArrayList<String> side2Players) {
        this.side2Players = side2Players;
    }
}
