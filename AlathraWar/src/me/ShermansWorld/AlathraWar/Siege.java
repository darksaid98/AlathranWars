package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.inviteobjects.TownJoinNationInvite;

public class Siege {
	
	private War war;
	private Town town;
	private String attackers;
	private String defenders;
	private boolean side1AreAttackers = false;
	private boolean side2AreAttackers = false;
	private int id;
	
	private int attackerPoints;
	private int defenderPoints;
	private int MAXSIEGETICKS;
	private Player owner;
	
	private int siegeTicks = 0;
	int[] bukkitId = { 0 };
	
	public ArrayList<String> attackerPlayers = new ArrayList<String>();
	public ArrayList<String> defenderPlayers = new ArrayList<String>();
	
	public Siege (int id, War war, Town town, String attackers, String defenders, boolean side1AreAttackers, boolean side2AreAttackers) {
		
		this.war = war;
		this.town = town;
		this.attackers = attackers;
		this.defenders = defenders;
		this.side1AreAttackers = side1AreAttackers;
		this.side2AreAttackers = side2AreAttackers;
		this.id = id;
		
	}
	
	public void start() {
		attackerPoints = Main.data2.getConfig().getInt("Sieges." + String.valueOf(id) + ".attackerpoints");
		defenderPoints = Main.data2.getConfig().getInt("Sieges." + String.valueOf(id) + ".defenderpoints");
		MAXSIEGETICKS = 108000; // 108k = 90 minutes
		siegeTicks = Main.data2.getConfig().getInt("Sieges." + String.valueOf(id) + ".siegeticks");
		owner = Bukkit.getPlayer(Main.data2.getConfig().getString("Sieges." + String.valueOf(id) + ".owner")); //the player who started the siege
		
		if (side1AreAttackers) {
			attackerPlayers = war.getSide2Players();
			defenderPlayers = war.getSide1Players();
		} else {
			attackerPlayers = war.getSide1Players();
			defenderPlayers = war.getSide2Players();
		}
		
		bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			int homeBlockControl = 0; // 0 is default, 1 is contested, 2 is defenders, 3 is attackers
			public void run() {
				if (siegeTicks >= MAXSIEGETICKS) { // if the siege is over
					Bukkit.getServer().getScheduler().cancelTask(bukkitId[0]);
					if (attackerPoints > defenderPoints) {
						attackersWin(owner);
					} else {
						defendersWin();
					}
				} else {
					boolean attackersAreOnHomeBlock = false;
					boolean defendersAreOnHomeBlock = false;
					siegeTicks += 200;
					Main.data2.getConfig().set("Sieges." + String.valueOf(id) + ".siegeticks", siegeTicks);
					Main.data2.saveConfig();
					for (String playerName : attackerPlayers) {
						try {
							if (WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().isHomeBlock()
									&& WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName)).getTownBlock().getTown().equals(town)) {
								// if player is in a town homeblock and the town they are in matches the town being sieged
								if (!Main.getInstance().getServer().getPlayer(playerName).isDead()) {
									attackersAreOnHomeBlock = true;
								}
							}
						} catch (NotRegisteredException | NullPointerException e) {
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
						} catch (NotRegisteredException | NullPointerException e) {
							// 
						}
					}
					
					// report homeblock control changes
					if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) {
						if (homeBlockControl != 1) {
							for (String playerName : attackerPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "HomeBlock at " + town.getName() + " contested!");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
							for (String playerName : defenderPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "HomeBlock at " + town.getName() + " contested!");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
						}
						homeBlockControl = 1;
					} else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) {
						if (homeBlockControl != 2) {
							for (String playerName : attackerPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attackers have captured the HomeBlock at " + town.getName() + "! +1 Attacker Points per second");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
							for (String playerName : defenderPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attackers have captured the HomeBlock at " + town.getName() + "! +1 Attacker Points per second");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
						}
						homeBlockControl = 2;
						addPointsToAttackers(10);
						Main.data2.getConfig().set("Sieges." + String.valueOf(id) + ".attackerpoints", attackerPoints);
						Main.data2.saveConfig();
					} else {
						if (homeBlockControl != 3) {
							for (String playerName : attackerPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defenders retain control of the HomeBlock at " + town.getName() + "! +1 Defender Points per second");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
							for (String playerName : defenderPlayers) {
								try {
									Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defenders retain control of the HomeBlock at " + town.getName() + "! +1 Defender Points per second");
								} catch (NullPointerException e) {
									//player is offline or filler
								}
							}
						}
						addPointsToDefenders(10);
						Main.data2.getConfig().set("Sieges." + String.valueOf(id) + ".defenderpoints", defenderPoints);
						Main.data2.saveConfig();
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
	
	public void stop() {
		Bukkit.getScheduler().cancelTask(bukkitId[0]);
		SiegeCommands.sieges.remove(this);
		Main.data2.getConfig().set("Sieges." + String.valueOf(id), null);
		Main.data2.saveConfig();
	}
	
	public void attackersWin(Player owner) {
		Resident resident = TownyAPI.getInstance().getResident(owner);
		Nation nation = null;
		Bukkit.broadcastMessage(Helper.Chatlabel() + "The attackers from " + attackers + " have won the siege of " + town.getName() + "!");
		try {
			nation = resident.getTown().getNation();
		} catch (NotRegisteredException e) {
			for (String playerName : attackerPlayers) {
				try {
					Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "The town of " + town.getName() + " could not be occupied because the player who started the siege is not part of a nation");
				} catch (NullPointerException e2) {
					//player is offline or filler
				}
			}
			for (String playerName : defenderPlayers) {
				try {
					Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "The town of " + town.getName() + " could not be occupied because the player who started the siege is not part of a nation");
				} catch (NullPointerException e2) {
					//player is offline or filler
				}
			}
		}
		if (nation != null) {
			TownOccupationController.setTownOccupation(town, nation);
			Bukkit.broadcastMessage(Helper.Chatlabel() + "The town of " + town.getName() + " has been placed under occupation by " + nation.getName() + "!");
		}
		
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner.getName());
		Main.econ.depositPlayer(offlinePlayer, 20000.0);
		double amt = 0.0;
		if (town.getAccount().getHoldingBalance() > 80000.0) {
			amt = Math.floor(town.getAccount().getHoldingBalance())/4;
			town.getAccount().withdraw(amt, "war loot");
		} else {
			if (town.getAccount().getHoldingBalance() < 20000.0) {
				amt = town.getAccount().getHoldingBalance();
				Bukkit.broadcastMessage(Helper.Chatlabel() + "The town of " + town.getName() + " has been destroyed by " + getAttackers() + "!");
				TownyUniverse.getInstance().getDataSource().deleteTown(town);
				Main.econ.depositPlayer(offlinePlayer, amt);
				return;
			} else {
				town.getAccount().withdraw(20000.0, "war loot");
				amt = 20000.0;
			}
		}
		Bukkit.broadcastMessage("The town of " + town.getName() + " has been sacked by " + getAttackers() + ", valuing $" + String.valueOf(amt));
		Main.econ.depositPlayer(offlinePlayer, amt);
		
			
	}
	
	public void defendersWin() {
		town.getAccount().deposit(20000, "War chest");
		Bukkit.broadcastMessage(Helper.Chatlabel() + "The defenders from " + defenders + " have won the siege of " + town.getName() + "!");
		Bukkit.broadcastMessage(Helper.Chatlabel() + town.getName() + " has recovered the attackers' war chest, valued at $20,000");
		
	}
	
	
	public void addPointsToAttackers(int points) {
		attackerPoints += points;
	}
	
	public void addPointsToDefenders(int points) {
		defenderPoints += points;
	}
	
	
	
	// Getters and Setters
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
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
