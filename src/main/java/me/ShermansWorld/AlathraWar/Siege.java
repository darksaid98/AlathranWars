package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;

import me.ShermansWorld.AlathraWar.data.SiegeData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

public class Siege {

    public static final int maxSiegeTicks = 72000; // 60 minute tick constant

	private War war; // War which siege belongs to
	private Town town; // Town of the siege
	private boolean side1AreAttackers; // bool of if side1 being attacker
	private int attackerPoints;
	private int defenderPoints;
	private OfflinePlayer siegeLeader; 
	private int siegeTicks;
	private TownBlock homeBlock;
	private Location townSpawn;
	int[] bukkitId;
	public ArrayList<String> attackerPlayers;
	public ArrayList<String> defenderPlayers;
	public ArrayList<Location> beaconLocs;

	public Siege(final War war, final Town town, OfflinePlayer siegeLeader) {
		this.siegeTicks = 0;
		this.bukkitId = new int[1];
		this.attackerPlayers = new ArrayList<String>();
		this.defenderPlayers = new ArrayList<String>();
		this.beaconLocs = new ArrayList<Location>();
		this.war = war;
		this.town = town;
        this.siegeLeader = siegeLeader;
        if (war.getSide(town.getName()) == 2) side1AreAttackers = true;
        else side1AreAttackers = false;
	}

    /** Starts a siege */
	public void start() {
        attackerPoints = 0;
        defenderPoints = 0;
		this.siegeTicks = 0;
        homeBlock = town.getHomeBlockOrNull();
        townSpawn = town.getSpawnOrNull();

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
						if (Siege.this.siegeTicks >= maxSiegeTicks) {
							Bukkit.getServer().getScheduler().cancelTask(Siege.this.bukkitId[0]);
                            SiegeData.removeSiege(Siege.this);
							if (Siege.this.attackerPoints > Siege.this.defenderPoints) {
								Siege.this.attackersWin(Siege.this.siegeLeader);
							} else {
								Siege.this.defendersWin();
							}
						} else {
							boolean attackersAreOnHomeBlock = false;
							boolean defendersAreOnHomeBlock = false;
                            siegeTicks = siegeTicks + 200;
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
							if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) { // Contested
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
							} else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) { // Attackers
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
							} else { // Defenders / None
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
								this.homeBlockControl = 3;
							}
							if (Siege.this.siegeTicks % 6000 == 0) { // Updates every 5 minutes
								Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "Report on the siege of "
										+ Siege.this.town.getName() + ":");
								Bukkit.broadcastMessage(
										"Attacker Points - " + String.valueOf(Siege.this.attackerPoints));
								Bukkit.broadcastMessage(
										"Defender Points - " + String.valueOf(Siege.this.defenderPoints));
							}
                            if (siegeTicks % 1200 == 0) { // Saves every minute
                                save();
                            }
						}
					}
				}, 0L, 200L);
	}

    /** Resumes a siege (after a server restart e.t.c.)*/
    public void resume(int resumeTick) {
        siegeTicks = resumeTick;
    }

    /** Stops a siege */
	public void stop() {
		Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
		SiegeData.removeSiege(this);
	}

	public void attackersWin(final OfflinePlayer siegeLeader) {
		final Resident resident = TownyAPI.getInstance().getResident(siegeLeader.getUniqueId());
		Nation nation = null;
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The attackers have won the siege of " + this.town.getName() + "!");
		try {
			nation = resident.getTown().getNation();
		} catch (Exception e) {}
		if (nation != null) {
			Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The town of " + this.town.getName()
					+ " has been placed under occupation by " + nation.getName() + "!");
		}
		Main.econ.depositPlayer(siegeLeader, 2500.0);
		double amt = 0.0;
		if (this.town.getAccount().getHoldingBalance() > 10000.0) {
			amt = Math.floor(this.town.getAccount().getHoldingBalance()) / 4.0;
			this.town.getAccount().withdraw(amt, "war loot");
		} else {
			if (this.town.getAccount().getHoldingBalance() < 2500.0) {
				amt = this.town.getAccount().getHoldingBalance();
				Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The town of " + this.town.getName()
						+ " has been destroyed by " + this.getAttackers() + "!");
				TownyUniverse.getInstance().getDataSource().deleteTown(this.town);
				Main.econ.depositPlayer(siegeLeader, amt);
				return;
			}
			this.town.getAccount().withdraw(2500.0, "war loot");
			amt = 2500.0;
		}
		Bukkit.broadcastMessage("The town of " + this.town.getName() + " has been sacked by " + this.getAttackers()
				+ ", valuing $" + String.valueOf(amt));
		Main.warLogger.log("The town of " + this.town.getName() + " has been sacked by " + this.getAttackers()
				+ ", valuing $" + String.valueOf(amt));
		Main.econ.depositPlayer(siegeLeader, amt);

        if (side1AreAttackers) {
            war.addSide1Points(50);
        } else {
            war.addSide2Points(50);
        }

		stop();
		clearBeacon();
	}

	public void defendersWin() {
		this.town.getAccount().deposit(2500.0, "War chest");
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders have won the siege of " + this.town.getName() + "!");
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + this.town.getName()
				+ " has recovered the attackers' war chest, valued at $2,500");
		Main.warLogger
				.log(war.getName() + ": The defenders have won the siege of " + this.town.getName() + "!");

        if (side1AreAttackers) {
            war.addSide2Points(50);
        } else {
            war.addSide1Points(50);
        }

		stop();
		clearBeacon();
	}

	/**
	 * No winnder declared
	 */
	public void noWinner() {
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The siege of " + this.town.getName() + " was a draw!");
		Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "No money has been recovered.");
		Main.warLogger
				.log(war.getName() + ": No one won the siege of " + this.town.getName() + "!");
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
            save();
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

	public OfflinePlayer getSiegeOwner() {
		return siegeLeader;
	}

	public void setSiegeOwner(OfflinePlayer siegeOwner) {
		this.siegeLeader = siegeOwner;
	}

    /** Gets attacker name string */
	public String getAttackers() {
		if (side1AreAttackers) {
            return war.getSide1();
        } else {
            return war.getSide2();
        }
	}

    /** Gets defender name string */
	public String getDefenders() {
		if (side1AreAttackers) {
            return war.getSide2();
        } else {
            return war.getSide1();
        }
	}

	public int getAttackerPoints() {
		return this.attackerPoints;
	}

	public int getDefenderPoints() {
		return this.defenderPoints;
	}

	public int getMaxSiegeTicks() {
		return maxSiegeTicks;
	}

	public void setSiegeTicks(int siegeTicks) {
		this.siegeTicks = siegeTicks;
	}

	public TownBlock getHomeBlock() {
		return homeBlock;
	}

	public void setHomeBlock(TownBlock homeBlock) {
		this.homeBlock = homeBlock;
	}

	public Location getTownSpawn() {
		return townSpawn;
	}

	public void setTownSpawn(Location townSpawn) {
		this.townSpawn = townSpawn;
	}

	public void setAttackerPoints(int points) {
		this.attackerPoints = points;
	}

	public void setDefenderPoints(int points) {
		this.defenderPoints = points;
	}

	public boolean getSide1AreAttackers() {
		return this.side1AreAttackers;
	}

	public void setSide1AreAttackers(final boolean side1AreAttackers) {
		this.side1AreAttackers = side1AreAttackers;
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

    public int getSiegeTicks() {
        return siegeTicks;
    }

    public OfflinePlayer getSiegeLeader() {
        return siegeLeader;
    }

	public void save() {
        SiegeData.saveSiege(this);
    }

	public String getName() {
		return this.getWar().getName() + "-" + this.getTown();
	}
}
