package me.ShermansWorld.AlathraWar;

import java.lang.Math;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;


public final class KillsListener implements Listener {
	@EventHandler
	public void onPlayerKilled(PlayerDeathEvent event) {
		
		if (event.getEntity().getPlayer() == null || !(event.getEntity().getKiller() instanceof Player)) {
			return;
		}
		
		Player killed = event.getEntity();
		Player killer = event.getEntity().getKiller();	
		Town town = null;
		
		try {
			town = WorldCoord.parseWorldCoord(killed).getTownBlock().getTown();
		} catch (NotRegisteredException e) {
			// do nothing, town is null
		}
		
		boolean playerCloseToHomeBlock = false;
		
		try {
			for (Siege siege : SiegeCommands.sieges) {
				int homeBlockXCoord = siege.getTown().getHomeBlock().getCoord().getX()*16; // getCoord gives chunks, multiply by 16 to get center coord of HomeBlock
				int homeBlockZCoord = siege.getTown().getHomeBlock().getCoord().getZ()*16;
				if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoord) <= 300 &&
						Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoord) <= 300) {
					playerCloseToHomeBlock = true;
				}
				if (siege.getAttackerPlayers().contains(killer.getName()) && siege.getDefenderPlayers().contains(killed.getName())) {
					//if someone from attacking side kills a player from defending side
					if ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlock) {
						siege.addPointsToAttackers(20);
						for (String playerName : siege.getAttackerPlayers()) {
							try {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defender killed! + 20 Attacker Points");
							} catch (NullPointerException e) {
								// offline player or template
							}
						}
						for (String playerName : siege.getDefenderPlayers()) {
							try {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Defender killed! + 20 Attacker Points");
							} catch (NullPointerException e) {
								// offline player or template
							}
						}
						siegeKill(killed, event);
						return;
					}
 				}
				if (siege.getDefenderPlayers().contains(killer.getName()) && siege.getAttackerPlayers().contains(killed.getName())) {
					//if someone from defending side kills a player from the attacking side
					if ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlock) {
						siege.addPointsToDefenders(20);
						for (String playerName : siege.getAttackerPlayers()) {
							try {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attacker killed! + 20 Defender Points");
							} catch (NullPointerException e) {
								// offline player or template
							}
						}
						for (String playerName : siege.getDefenderPlayers()) {
							try {
								Bukkit.getPlayer(playerName).sendMessage(Helper.Chatlabel() + "Attacker killed! + 20 Defender Points");
							} catch (NullPointerException e) {
								// offline player or template
							}
						}
						siegeKill(killed, event);
						return;
					}
				}
			}
		} catch (NullPointerException | TownyException e) {
			return;
		}
		
	}
	
	private void siegeKill(Player killed, PlayerDeathEvent event) {
		Inventory inv = killed.getInventory();

		for(int i = 0; i < inv.getSize(); i++) {
		    ItemStack is = inv.getItem(i);
		    if(is != null) {
		        // if an itemstack is null it is empty
		        if(is.getType() == Material.NETHERITE_AXE || is.getType() == Material.NETHERITE_SWORD 
		        		|| is.getType() == Material.DIAMOND_AXE || is.getType() == Material.DIAMOND_SWORD 
		        		|| is.getType() == Material.IRON_AXE || is.getType() == Material.IRON_SWORD) {
		        	ItemMeta meta = is.getItemMeta();
		        	Damageable damageable = (Damageable) meta;
		        	if (is.getType() == Material.NETHERITE_SWORD || is.getType() == Material.NETHERITE_AXE) {
		        		damageable.setDamage(damageable.getDamage() + 507);
		        		if (damageable.getDamage() > 2000) {
		        			damageable.setDamage(2030);
		        		}
		        	} else if (is.getType() == Material.DIAMOND_SWORD || is.getType() == Material.DIAMOND_AXE) {
		        		damageable.setDamage(damageable.getDamage() + 390);
		        		if (damageable.getDamage() > 1500) {
		        			damageable.setDamage(1560);
		        		}
		        	} else if (is.getType() == Material.IRON_SWORD || is.getType() == Material.IRON_AXE) {
		        		damageable.setDamage(damageable.getDamage() + 62);
		        		if (damageable.getDamage() > 200) {
		        			damageable.setDamage(249);
		        		}
		        	}
		        	meta = (ItemMeta) damageable;
		        	is.setItemMeta((ItemMeta) meta);
		         }
		    }
		}
		
		ItemStack [] armor = killed.getInventory().getArmorContents();
		
		for (int i = 0; i < armor.length; i++) {
			if (armor[i] != null) {
				ItemStack is = armor[i];
				if(is.getType() == Material.NETHERITE_HELMET || is.getType() == Material.NETHERITE_CHESTPLATE
						|| is.getType() == Material.NETHERITE_LEGGINGS || is.getType() == Material.NETHERITE_BOOTS 
						|| is.getType() == Material.DIAMOND_HELMET || is.getType() == Material.DIAMOND_CHESTPLATE
						|| is.getType() == Material.DIAMOND_LEGGINGS || is.getType() == Material.NETHERITE_BOOTS 
						|| is.getType() == Material.IRON_HELMET || is.getType() == Material.IRON_CHESTPLATE
						|| is.getType() == Material.IRON_LEGGINGS || is.getType() == Material.IRON_BOOTS ) {
					ItemMeta meta = is.getItemMeta();
		        	Damageable damageable = (Damageable) meta;
		        	if (is.getType() == Material.NETHERITE_HELMET) {
		        		damageable.setDamage(damageable.getDamage() + 101);
		        		if (damageable.getDamage() > 400) {
		        			damageable.setDamage(406);
		        		}
		        	}
		        	if (is.getType() == Material.NETHERITE_CHESTPLATE) {
		        		damageable.setDamage(damageable.getDamage() + 148);
		        		if (damageable.getDamage() > 450) {
		        			damageable.setDamage(591);
		        		}
		        	}
		        	if (is.getType() == Material.NETHERITE_LEGGINGS) {
		        		damageable.setDamage(damageable.getDamage() + 138);
		        		if (damageable.getDamage() > 420) { // blaze it
		        			damageable.setDamage(554);
		        		}
		        	}
		        	if (is.getType() == Material.NETHERITE_BOOTS) {
		        		damageable.setDamage(damageable.getDamage() + 120);
		        		if (damageable.getDamage() > 370) {
		        			damageable.setDamage(480);
		        		}
		        	}
		        	if (is.getType() == Material.DIAMOND_HELMET) {
		        		damageable.setDamage(damageable.getDamage() + 90);
		        		if (damageable.getDamage() > 280) {
		        			damageable.setDamage(362);
		        		}
		        	}
		        	if (is.getType() == Material.DIAMOND_CHESTPLATE) {
		        		damageable.setDamage(damageable.getDamage() + 132);
		        		if (damageable.getDamage() > 410) {
		        			damageable.setDamage(527);
		        		}
		        	}
		        	if (is.getType() == Material.DIAMOND_LEGGINGS) {
		        		damageable.setDamage(damageable.getDamage() + 123);
		        		if (damageable.getDamage() > 380) {
		        			damageable.setDamage(494);
		        		}
		        	}
		        	if (is.getType() == Material.DIAMOND_BOOTS) {
		        		damageable.setDamage(damageable.getDamage() + 107);
		        		if (damageable.getDamage() > 330) {
		        			damageable.setDamage(428);
		        		}
		        	}
		        	if (is.getType() == Material.IRON_HELMET) {
		        		damageable.setDamage(damageable.getDamage() + 41);
		        		if (damageable.getDamage() > 125) {
		        			damageable.setDamage(164);
		        		}
		        	}
		        	if (is.getType() == Material.IRON_CHESTPLATE) {
		        		damageable.setDamage(damageable.getDamage() + 60);
		        		if (damageable.getDamage() > 190) {
		        			damageable.setDamage(239);
		        		}
		        	}
		        	if (is.getType() == Material.IRON_LEGGINGS) {
		        		damageable.setDamage(damageable.getDamage() + 56);
		        		if (damageable.getDamage() > 180) {
		        			damageable.setDamage(224);
		        		}
		        	}
		        	if (is.getType() == Material.IRON_BOOTS) {
		        		damageable.setDamage(damageable.getDamage() + 48);
		        		if (damageable.getDamage() > 150) {
		        			damageable.setDamage(194);
		        		}
		        	}
		        	meta = (ItemMeta) damageable;
		        	is.setItemMeta((ItemMeta) meta);
		         }
			}
		}
		
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + killed.getName());
		event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
		
	}
}
