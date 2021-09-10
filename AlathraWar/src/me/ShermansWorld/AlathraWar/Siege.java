package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class Siege {
	
	private War war;
	private Town town;
	private String attackers;
	private String defenders;
	private boolean side1AreAttackers = false;
	private boolean side2AreAttackers = false;
	
	private int attackerPoints;
	private int defenderPoints;
	private int MAXSIEGETICKS;
	private int siegeTicks;
	
	public ArrayList<String> attackerPlayers = new ArrayList<String>();
	public ArrayList<String> defenderPlayers = new ArrayList<String>();
	
	public Siege (War war, Town town, String attackers, String defenders, boolean side1AreAttackers, boolean side2AreAttackers) {
		
		this.war = war;
		this.town = town;
		this.attackers = attackers;
		this.defenders = defenders;
		this.side1AreAttackers = side1AreAttackers;
		this.side2AreAttackers = side2AreAttackers;
		
	}
	
	
	public void start() {
		attackerPoints = 0;
		defenderPoints = 0;
		MAXSIEGETICKS = 108000; // 108k = 90 minutes
		siegeTicks = 0;
		
		if (side1AreAttackers) {
			attackerPlayers = war.getSide1Players();
			defenderPlayers = war.getSide2Players();
		} else {
			attackerPlayers = war.getSide2Players();
			defenderPlayers = war.getSide1Players();
		}
		
		
		int[] id = { 0 };
		id[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			int homeBlockControl = 0; // 0 is default, 1 is contested, 2 is defenders, 3 is attackers
			public void run() {
				
				if (siegeTicks >= MAXSIEGETICKS) { // if the siege is over
					Bukkit.getServer().getScheduler().cancelTask(id[0]);
					if (attackerPoints > defenderPoints) {
						attackersWin();
					} else {
						defendersWin();
					}
				} else {
					boolean attackersAreOnHomeBlock = false;
					boolean defendersAreOnHomeBlock = false;
					siegeTicks += 200;
					for (String playerName : attackerPlayers) {
						try {
							if (WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().isHomeBlock()
									&& WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().getTown().equals(town)) {
								// if player is in a town homeblock and the town they are in matches the town being sieged
								if (!Main.getInstance().getServer().getPlayer(playerName).isDead()) {
									attackersAreOnHomeBlock = true;
								}
							}
						} catch (NotRegisteredException e) {
							// 
						}
					}
					for (String playerName : defenderPlayers) {
						try {
							if (WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().isHomeBlock()
									&& WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().getTown().equals(town)) {
								// if player is in a town homeblock and the town they are in matches the town being sieged
								if (!Main.getInstance().getServer().getPlayer(playerName).isDead()) {
									defendersAreOnHomeBlock = true;
								}
							}
						} catch (NotRegisteredException e) {
							// 
						}
					}
					
					// report homeblock control changes
					if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) {
						if (homeBlockControl != 1) {
							for (String playerName : attackerPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "HomeBlock at " + town.getName() + " contested!");
							}
							for (String playerName : defenderPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "HomeBlock at " + town.getName() + " contested!");
							}
						}
						homeBlockControl = 1;
					} else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) {
						if (homeBlockControl != 2) {
							for (String playerName : attackerPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attackers have captured the HomeBlock at " + town.getName() + "! +1 Attacker Points per second");
							}
							for (String playerName : defenderPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attackers have captured the HomeBlock at " + town.getName() + "! +1 Attacker Points per second");
							}
						}
						homeBlockControl = 2;
						addPointsToAttackers(10);
					} else {
						if (homeBlockControl != 3) {
							for (String playerName : attackerPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defenders retain control of the HomeBlock at " + town.getName() + "! +1 Defender Points per second");
							}
							for (String playerName : defenderPlayers) {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defenders retain control of the HomeBlock at " + town.getName() + "! +1 Defender Points per second");
							}
						}
						addPointsToDefenders(10);
						homeBlockControl = 3;
					}
					
					//report score every 5 minutes
					
					if (siegeTicks % 6000 == 0) {
						Bukkit.broadcastMessage(Helper.Chatlabel() + "Report on the siege of " + town.getName() + ":");
						Bukkit.broadcastMessage("Attacker Points - " + String.valueOf(attackerPoints));
						Bukkit.broadcastMessage("Defender Points - " + String.valueOf(defenderPoints));
					}
					
					
				}
			}
		}, 0L, 200L); //every 10 seconds
	}
	
	public void attackersWin() {
		Bukkit.broadcastMessage(Helper.Chatlabel() + "Attackers have won");
	}
	
	public void defendersWin() {
		Bukkit.broadcastMessage(Helper.Chatlabel() + "Defenders have won");
	}
	
	
	public void addPointsToAttackers(int points) {
		attackerPoints += points;
	}
	
	public void addPointsToDefenders(int points) {
		defenderPoints += points;
	}
	
	
	
	// Getters and Setters
	
	public War getWar() {
		return war;
	}
	
	public void setWar(War war) {
		this.war = war;
	}
	
	public Town getTown() {
		return town;
	}
	
	public void setTown(Town town) {
		this.town = town;
	}
	
	public String getAttackers() {
		return attackers;
	}
	
	public void setAttackers(String attackers) {
		this.attackers = attackers;
	}
	
	public String getDefenders() {
		return defenders;
	}
	
	public void setDefenders(String defenders) {
		this.attackers = defenders;
	}
	
	public int getAttackerPoints() {
		return attackerPoints;
	}
	
	public void attackerPoints(int attackerPoints) {
		this.attackerPoints = attackerPoints;
	}
	
	public int getDefenderPoints() {
		return defenderPoints;
	}
	
	public void defenderPoints(int defenderPoints) {
		this.defenderPoints = attackerPoints;
	}
	
	public boolean getSide1AreAttackers() {
		return side1AreAttackers;
	}
	
	public void setSide1AreAttackers(boolean side1AreAttackers) {
		this.side1AreAttackers = side1AreAttackers;
	}
	
	public boolean getSide2AreAttackers() {
		return side2AreAttackers;
	}
	
	public void setSide2AreAttackers(boolean side2AreAttackers) {
		this.side2AreAttackers = side2AreAttackers;
	}
	
	public ArrayList<String> getAttackerPlayers() {
		return attackerPlayers;
	}
	
	public void setAttackerPlayer(ArrayList<String> attackerPlayers) {
		this.attackerPlayers = attackerPlayers;
	}
	
	public ArrayList<String> getDefenderPlayers() {
		return defenderPlayers;
	}
	
	public void setDefenderPlayer(ArrayList<String> defenderPlayers) {
		this.defenderPlayers = attackerPlayers;
	}

}
