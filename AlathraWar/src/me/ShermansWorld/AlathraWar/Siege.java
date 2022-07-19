// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.gmail.goosius.siegewar.TownOccupationController;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

public class Siege {
	private War war;
	private Town town;
	private String attackers;
	private String defenders;
	private boolean side1AreAttackers;
	private boolean side2AreAttackers;
	private int id;
	private int attackerPoints;
	private int defenderPoints;
	private int MAXSIEGETICKS;
	private Player owner;
	private int siegeTicks;
	private TownBlock homeBlock;
	private Location townSpawn;
	int[] bukkitId;
	public ArrayList<String> attackerPlayers;
	public ArrayList<String> defenderPlayers;
	public ArrayList<Location> beaconLocs;

	public Siege(final int id, final War war, final Town town, final String attackers, final String defenders,
			final boolean side1AreAttackers, final boolean side2AreAttackers) {
		this.siegeTicks = 0;
		this.bukkitId = new int[1];
		this.attackerPlayers = new ArrayList<String>();
		this.defenderPlayers = new ArrayList<String>();
		this.beaconLocs = new ArrayList<Location>();
		this.war = war;
		this.town = town;
		this.attackers = attackers;
		this.defenders = defenders;
		this.side1AreAttackers = side1AreAttackers;
		this.side2AreAttackers = side2AreAttackers;
		this.id = id;
	}

	public void start() {
		this.attackerPoints = Main.data2.getConfig().getInt("Sieges." + String.valueOf(this.id) + ".attackerpoints");
		this.defenderPoints = Main.data2.getConfig().getInt("Sieges." + String.valueOf(this.id) + ".defenderpoints");
		this.side1AreAttackers = Main.data2.getConfig()
				.getBoolean("Sieges." + String.valueOf(this.id) + ".side1areattackers");
		this.side2AreAttackers = Main.data2.getConfig()
				.getBoolean("Sieges." + String.valueOf(this.id) + ".side2areattackers");
		this.MAXSIEGETICKS = 108000;
		this.siegeTicks = Main.data2.getConfig().getInt("Sieges." + String.valueOf(this.id) + ".siegeticks");
		this.owner = Bukkit.getPlayer(Main.data2.getConfig().getString("Sieges." + String.valueOf(this.id) + ".owner"));
		if (this.side1AreAttackers) {
			this.attackerPlayers = this.war.getSide1Players();
			this.defenderPlayers = this.war.getSide2Players();
		} else {
			this.attackerPlayers = this.war.getSide2Players();
			this.defenderPlayers = this.war.getSide1Players();
		}
		try {
			homeBlock = town.getHomeBlock();
			townSpawn = town.getSpawn();
		} catch (TownyException e) {
		}

		this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
				(Runnable) new Runnable() {
					int homeBlockControl = 0;

					@Override
					public void run() {
						if (homeBlock != null) {
							town.setHomeBlock(homeBlock);
							town.setSpawn(townSpawn);
						}
						if (Siege.this.side1AreAttackers) {
							Siege.this.attackerPlayers = Siege.this.war.getSide1Players();
							Siege.this.defenderPlayers = Siege.this.war.getSide2Players();
						} else {
							Siege.this.attackerPlayers = Siege.this.war.getSide2Players();
							Siege.this.defenderPlayers = Siege.this.war.getSide1Players();
						}
						if (Siege.this.siegeTicks >= Siege.this.MAXSIEGETICKS) {
							Bukkit.getServer().getScheduler().cancelTask(Siege.this.bukkitId[0]);
							if (Siege.this.attackerPoints > Siege.this.defenderPoints) {
								Siege.this.attackersWin(Siege.this.owner);
							} else {
								Siege.this.defendersWin();
							}
						} else {
							boolean attackersAreOnHomeBlock = false;
							boolean defendersAreOnHomeBlock = false;
							final Siege this$0 = Siege.this;
							Siege.access$7(this$0, this$0.siegeTicks + 200);
							Main.data2.getConfig().set("Sieges." + String.valueOf(Siege.this.id) + ".siegeticks",
									(Object) Siege.this.siegeTicks);
							Main.data2.saveConfig();
							for (final String playerName : Siege.this.attackerPlayers) {
								try {
									if (WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName))
											.getTownBlock().isHomeBlock()
											&& WorldCoord
													.parseWorldCoord(
															Main.getInstance().getServer().getPlayer(playerName))
													.getTownBlock().getTown().equals(town)
											&& !Bukkit.getPlayer(playerName).isDead()
											&& (Math.abs(
													Bukkit.getServer().getPlayer(playerName).getLocation().getBlockY()
															- townSpawn.getBlockY())) < 10) {
										attackersAreOnHomeBlock = true;
									}
								} catch (NotRegisteredException ex) {
								} catch (NullPointerException ex2) {
								}
							}
							for (final String playerName : Siege.this.defenderPlayers) {
								try {
									if (WorldCoord.parseWorldCoord(Main.getInstance().getServer().getPlayer(playerName))
											.getTownBlock().isHomeBlock()
											&& WorldCoord
													.parseWorldCoord(
															Main.getInstance().getServer().getPlayer(playerName))
													.getTownBlock().getTown().equals(town)
											&& !Bukkit.getPlayer(playerName).isDead()
											&& (Math.abs(
													Bukkit.getServer().getPlayer(playerName).getLocation().getBlockY()
															- townSpawn.getBlockY())) < 10) {
										defendersAreOnHomeBlock = true;
									}
								} catch (NotRegisteredException ex3) {
								} catch (NullPointerException ex4) {
								}
							}
							if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) {
								if (this.homeBlockControl != 1) {
									for (final String playerName : Siege.this.attackerPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "HomeBlock at " + Siege.this.town.getName() + " contested!");
										} catch (NullPointerException ex5) {
										}
									}
									for (final String playerName : Siege.this.defenderPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "HomeBlock at " + Siege.this.town.getName() + " contested!");
										} catch (NullPointerException ex6) {
										}
									}
								}
								this.homeBlockControl = 1;
							} else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) {
								if (this.homeBlockControl != 2) {
									for (final String playerName : Siege.this.attackerPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "Attackers have captured the HomeBlock at "
													+ Siege.this.town.getName() + "! +1 Attacker Points per second");
										} catch (NullPointerException ex7) {
										}
									}
									for (final String playerName : Siege.this.defenderPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "Attackers have captured the HomeBlock at "
													+ Siege.this.town.getName() + "! +1 Attacker Points per second");
										} catch (NullPointerException ex8) {
										}
									}
								}
								this.homeBlockControl = 2;
								Siege.this.addPointsToAttackers(10);
								Main.data2.getConfig().set(
										"Sieges." + String.valueOf(Siege.this.id) + ".attackerpoints",
										(Object) Siege.this.attackerPoints);
								Main.data2.saveConfig();
							} else {
								if (this.homeBlockControl != 3) {
									for (final String playerName : Siege.this.attackerPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "Defenders retain control of the HomeBlock at "
													+ Siege.this.town.getName() + "! +1 Defender Points per second");
										} catch (NullPointerException ex9) {
										}
									}
									for (final String playerName : Siege.this.defenderPlayers) {
										try {
											Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel())
													+ "Defenders retain control of the HomeBlock at "
													+ Siege.this.town.getName() + "! +1 Defender Points per second");
										} catch (NullPointerException ex10) {
										}
									}
								}
								Siege.this.addPointsToDefenders(10);
								Main.data2.getConfig().set(
										"Sieges." + String.valueOf(Siege.this.id) + ".defenderpoints",
										(Object) Siege.this.defenderPoints);
								Main.data2.saveConfig();
								this.homeBlockControl = 3;
							}
							if (Siege.this.siegeTicks % 6000 == 0) {
								Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "Report on the siege of "
										+ Siege.this.town.getName() + ":");
								Bukkit.broadcastMessage(
										"Attacker Points - " + String.valueOf(Siege.this.attackerPoints));
								Bukkit.broadcastMessage(
										"Defender Points - " + String.valueOf(Siege.this.defenderPoints));
							}
						}
					}
				}, 0L, 200L);
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
		SiegeCommands.sieges.remove(this);
		Main.data2.getConfig().set("Sieges." + String.valueOf(this.id), (Object) null);
		Main.data2.saveConfig();
	}

	public void attackersWin(final Player owner) {
		final Resident resident = TownyAPI.getInstance().getResident(owner);
		Nation nation = null;
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The attackers from " + this.attackers
				+ " have won the siege of " + this.town.getName() + "!");
		try {
			nation = resident.getTown().getNation();
		} catch (NotRegisteredException e) {
			for (final String playerName : this.attackerPlayers) {
				try {
					Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "The town of "
							+ this.town.getName()
							+ " could not be occupied because the player who started the siege is not part of a nation");
				} catch (NullPointerException ex) {
				}
			}
			for (final String playerName : this.defenderPlayers) {
				try {
					Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "The town of "
							+ this.town.getName()
							+ " could not be occupied because the player who started the siege is not part of a nation");
				} catch (NullPointerException ex2) {
				}
			}
		}
		if (nation != null) {
			TownOccupationController.setTownOccupation(this.town, nation);
			Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The town of " + this.town.getName()
					+ " has been placed under occupation by " + nation.getName() + "!");
		}
		final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner.getUniqueId());
		Main.econ.depositPlayer(offlinePlayer, 20000.0);
		double amt = 0.0;
		if (this.town.getAccount().getHoldingBalance() > 80000.0) {
			amt = Math.floor(this.town.getAccount().getHoldingBalance()) / 4.0;
			this.town.getAccount().withdraw(amt, "war loot");
		} else {
			if (this.town.getAccount().getHoldingBalance() < 20000.0) {
				amt = this.town.getAccount().getHoldingBalance();
				Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The town of " + this.town.getName()
						+ " has been destroyed by " + this.getAttackers() + "!");
				TownyUniverse.getInstance().getDataSource().deleteTown(this.town);
				Main.econ.depositPlayer(offlinePlayer, amt);
				return;
			}
			this.town.getAccount().withdraw(20000.0, "war loot");
			amt = 20000.0;
		}
		Bukkit.broadcastMessage("The town of " + this.town.getName() + " has been sacked by " + this.getAttackers()
				+ ", valuing $" + String.valueOf(amt));
		Main.warLogger.log("The town of " + this.town.getName() + " has been sacked by " + this.getAttackers()
				+ ", valuing $" + String.valueOf(amt));
		Main.econ.depositPlayer(offlinePlayer, amt);

		stop();
		clearBeacon();
	}

	public void defendersWin() {
		this.town.getAccount().deposit(20000.0, "War chest");
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders from " + this.defenders
				+ " have won the siege of " + this.town.getName() + "!");
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + this.town.getName()
				+ " has recovered the attackers' war chest, valued at $20,000");
		Main.warLogger
				.log("The defenders from " + this.defenders + " have won the siege of " + this.town.getName() + "!");
		stop();
		clearBeacon();
	}

	public void createBeacon() {
		try {
			World world = town.getWorld();
			int homeBlockX = this.town.getHomeBlock().getX() * 16;
			int homeBlockZ = (this.town.getHomeBlock().getZ() + 1) * 16;
			if (homeBlockX > 0) {
				homeBlockX -= 8;
			}
			if (homeBlockZ > 0) {
				homeBlockZ -= 8;
			}
			if (homeBlockX < 0) {
				homeBlockX += 8;
			}
			if (homeBlockZ < 0) {
				homeBlockZ += 8;
			}
			int homeBlockY = 253;
			final Location beaconLoc = new Location(this.town.getWorld(), (double) homeBlockX,
					(double) (homeBlockY - 2), (double) homeBlockZ);
			while (beaconLoc.getBlock().getType().equals((Object) Material.AIR)) {
				--homeBlockY;
				beaconLoc.setY((double) homeBlockY);
			}
			homeBlockY += 2;
			this.beaconLocs.add(new Location(world, (double) homeBlockX, (double) homeBlockY, (double) homeBlockZ));
			this.beaconLocs
					.add(new Location(world, (double) homeBlockX, (double) (homeBlockY + 1), (double) homeBlockZ));
			this.beaconLocs
					.add(new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) homeBlockZ));
			this.beaconLocs.add(
					new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1), (double) homeBlockZ));
			this.beaconLocs.add(
					new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1), (double) homeBlockZ));
			this.beaconLocs.add(
					new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) (homeBlockZ + 1)));
			this.beaconLocs.add(
					new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) (homeBlockZ - 1)));
			this.beaconLocs.add(new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1),
					(double) (homeBlockZ + 1)));
			this.beaconLocs.add(new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1),
					(double) (homeBlockZ - 1)));
			this.beaconLocs.add(new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1),
					(double) (homeBlockZ + 1)));
			this.beaconLocs.add(new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1),
					(double) (homeBlockZ - 1)));
			for (int i = 2; i < this.beaconLocs.size(); ++i) {
				if (this.beaconLocs.get(i).getBlock().getType() != Material.AIR) {
					i = 1;
					homeBlockY++;
					for (final Location loc : this.beaconLocs) {
						loc.setY(loc.getY() + 1.0);
					}
				}
			}
			this.beaconLocs.get(0).getBlock().setType(Material.BEACON);
			this.beaconLocs.get(1).getBlock().setType(Material.RED_STAINED_GLASS);
			for (int i = 2; i < this.beaconLocs.size(); ++i) {
				this.beaconLocs.get(i).getBlock().setType(Material.IRON_BLOCK);
			}
			Main.data2.getConfig().set("Sieges." + String.valueOf(this.id) + ".world", world.getName());
			Main.data2.getConfig().set("Sieges." + String.valueOf(this.id) + ".homeblockx", homeBlockX);
			Main.data2.getConfig().set("Sieges." + String.valueOf(this.id) + ".homeblocky", homeBlockY);
			Main.data2.getConfig().set("Sieges." + String.valueOf(this.id) + ".homeblockz", homeBlockZ);
			Main.data2.saveConfig();
		} catch (TownyException ex) {
		}
	}

	public void resetBeacon(World world, int homeBlockX, int homeBlockY, int homeBlockZ) {
		this.beaconLocs.add(new Location(world, (double) homeBlockX, (double) homeBlockY, (double) homeBlockZ));
		this.beaconLocs.add(new Location(world, (double) homeBlockX, (double) (homeBlockY + 1), (double) homeBlockZ));
		this.beaconLocs.add(new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) homeBlockZ));
		this.beaconLocs
				.add(new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1), (double) homeBlockZ));
		this.beaconLocs
				.add(new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1), (double) homeBlockZ));
		this.beaconLocs
				.add(new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) (homeBlockZ + 1)));
		this.beaconLocs
				.add(new Location(world, (double) homeBlockX, (double) (homeBlockY - 1), (double) (homeBlockZ - 1)));
		this.beaconLocs.add(
				new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1), (double) (homeBlockZ + 1)));
		this.beaconLocs.add(
				new Location(world, (double) (homeBlockX + 1), (double) (homeBlockY - 1), (double) (homeBlockZ - 1)));
		this.beaconLocs.add(
				new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1), (double) (homeBlockZ + 1)));
		this.beaconLocs.add(
				new Location(world, (double) (homeBlockX - 1), (double) (homeBlockY - 1), (double) (homeBlockZ - 1)));
	}

	public void clearBeacon() {
		for (final Location loc : this.beaconLocs) {
			loc.getBlock().setType(Material.AIR);
		}
	}

	public void addPointsToAttackers(final int points) {
		this.attackerPoints += points;
	}

	public void addPointsToDefenders(final int points) {
		this.defenderPoints += points;
	}

	public int getID() {
		return this.id;
	}

	public void setID(final int id) {
		this.id = id;
	}

	public War getWar() {
		return this.war;
	}

	public void setWar(final War war) {
		this.war = war;
	}

	public Town getTown() {
		return this.town;
	}

	public void setTown(final Town town) {
		this.town = town;
	}

	public String getAttackers() {
		return this.attackers;
	}

	public void setAttackers(final String attackers) {
		this.attackers = attackers;
	}

	public String getDefenders() {
		return this.defenders;
	}

	public void setDefenders(final String defenders) {
		this.attackers = defenders;
	}

	public int getAttackerPoints() {
		return this.attackerPoints;
	}

	public void attackerPoints(final int attackerPoints) {
		this.attackerPoints = attackerPoints;
	}

	public int getDefenderPoints() {
		return this.defenderPoints;
	}

	public void defenderPoints(final int defenderPoints) {
		this.defenderPoints = this.attackerPoints;
	}

	public boolean getSide1AreAttackers() {
		return this.side1AreAttackers;
	}

	public void setSide1AreAttackers(final boolean side1AreAttackers) {
		this.side1AreAttackers = side1AreAttackers;
	}

	public boolean getSide2AreAttackers() {
		return this.side2AreAttackers;
	}

	public void setSide2AreAttackers(final boolean side2AreAttackers) {
		this.side2AreAttackers = side2AreAttackers;
	}

	public ArrayList<String> getAttackerPlayers() {
		return this.attackerPlayers;
	}

	public void setAttackerPlayer(final ArrayList<String> attackerPlayers) {
		this.attackerPlayers = attackerPlayers;
	}

	public ArrayList<String> getDefenderPlayers() {
		return this.defenderPlayers;
	}

	public void setDefenderPlayer(final ArrayList<String> defenderPlayers) {
		this.defenderPlayers = this.attackerPlayers;
	}

	static /* synthetic */ void access$7(final Siege siege, final int siegeTicks) {
		siege.siegeTicks = siegeTicks;
	}
}
