package me.ShermansWorld.AlathraWar.listeners;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.object.WorldCoord;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.commands.SiegeCommands;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.Listener;

public final class KillsListener implements Listener
{
    @EventHandler
    public void onPlayerKilled(final PlayerDeathEvent event) {
        if (event.getEntity().getPlayer() == null || !(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        final Player killed = event.getEntity();
        final Player killer = event.getEntity().getKiller();
        Town town = null;
        try {
            town = WorldCoord.parseWorldCoord((Entity)killed).getTownBlock().getTown();
        }
        catch (NotRegisteredException ex2) {}
        boolean playerCloseToHomeBlock = false;
        try {
            for (final Siege siege : SiegeCommands.sieges) {
                final int homeBlockXCoord = siege.getTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoord = siege.getTown().getHomeBlock().getCoord().getZ() * 16;
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoord) <= 300 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoord) <= 300) {
                    playerCloseToHomeBlock = true;
                }
                if (siege.getAttackerPlayers().contains(killer.getName()) && ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlock)) {
                	if (siege.getDefenderPlayers().contains(killed.getName())) {
                		siege.addPointsToAttackers(20);
                        for (final String playerName : siege.getAttackerPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ex5) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ex6) {}
                        }
                	}
                    this.siegeKill(killed, event);
                    return;
                }
                if (siege.getDefenderPlayers().contains(killer.getName()) && ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlock)) {
                	if (siege.getAttackerPlayers().contains(killed.getName())) {
                		siege.addPointsToDefenders(20);
                        for (final String playerName : siege.getAttackerPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
                            }
                            catch (NullPointerException ex3) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
                            }
                            catch (NullPointerException ex4) {}
                        }
                	}
                	this.siegeKill(killed, event);
                    return;
                }
                if ( (town != null && town.equals(siege.getTown()) || playerCloseToHomeBlock) ) {
                	this.siegeKill(killed, event);
                }
            }
        }
        catch (NullPointerException | TownyException ex7) {

        }
    }
    
    private void siegeKill(final Player killed, final PlayerDeathEvent event) {
        final Inventory inv = (Inventory)killed.getInventory();
        for (int i = 0; i < inv.getSize(); ++i) {
            final ItemStack is = inv.getItem(i);
            if (is != null && (is.getType() == Material.NETHERITE_AXE || is.getType() == Material.NETHERITE_SWORD || is.getType() == Material.DIAMOND_AXE || is.getType() == Material.DIAMOND_SWORD || is.getType() == Material.IRON_AXE || is.getType() == Material.IRON_SWORD)) {
                ItemMeta meta = is.getItemMeta();
                final Damageable damageable = (Damageable)meta;
                if (is.getType() == Material.NETHERITE_SWORD || is.getType() == Material.NETHERITE_AXE) {
                    damageable.setDamage(damageable.getDamage() + 507);
                    if (damageable.getDamage() > 2000) {
                        damageable.setDamage(2030);
                    }
                }
                else if (is.getType() == Material.DIAMOND_SWORD || is.getType() == Material.DIAMOND_AXE) {
                    damageable.setDamage(damageable.getDamage() + 390);
                    if (damageable.getDamage() > 1500) {
                        damageable.setDamage(1560);
                    }
                }
                else if (is.getType() == Material.IRON_SWORD || is.getType() == Material.IRON_AXE) {
                    damageable.setDamage(damageable.getDamage() + 62);
                    if (damageable.getDamage() > 200) {
                        damageable.setDamage(249);
                    }
                }
                meta = (ItemMeta)damageable;
                is.setItemMeta(meta);
            }
        }
        final ItemStack[] armor = killed.getInventory().getArmorContents();
        for (int j = 0; j < armor.length; ++j) {
            if (armor[j] != null) {
                final ItemStack is2 = armor[j];
                if (is2.getType() == Material.NETHERITE_HELMET || is2.getType() == Material.NETHERITE_CHESTPLATE || is2.getType() == Material.NETHERITE_LEGGINGS || is2.getType() == Material.NETHERITE_BOOTS || is2.getType() == Material.DIAMOND_HELMET || is2.getType() == Material.DIAMOND_CHESTPLATE || is2.getType() == Material.DIAMOND_LEGGINGS || is2.getType() == Material.NETHERITE_BOOTS || is2.getType() == Material.IRON_HELMET || is2.getType() == Material.IRON_CHESTPLATE || is2.getType() == Material.IRON_LEGGINGS || is2.getType() == Material.IRON_BOOTS) {
                    ItemMeta meta2 = is2.getItemMeta();
                    final Damageable damageable2 = (Damageable)meta2;
                    if (is2.getType() == Material.NETHERITE_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 101);
                        if (damageable2.getDamage() > 400) {
                            damageable2.setDamage(406);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 148);
                        if (damageable2.getDamage() > 450) {
                            damageable2.setDamage(591);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 138);
                        if (damageable2.getDamage() > 420) {
                            damageable2.setDamage(554);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 120);
                        if (damageable2.getDamage() > 370) {
                            damageable2.setDamage(480);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 90);
                        if (damageable2.getDamage() > 280) {
                            damageable2.setDamage(362);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 132);
                        if (damageable2.getDamage() > 410) {
                            damageable2.setDamage(527);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 123);
                        if (damageable2.getDamage() > 380) {
                            damageable2.setDamage(494);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 107);
                        if (damageable2.getDamage() > 330) {
                            damageable2.setDamage(428);
                        }
                    }
                    if (is2.getType() == Material.IRON_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 41);
                        if (damageable2.getDamage() > 125) {
                            damageable2.setDamage(164);
                        }
                    }
                    if (is2.getType() == Material.IRON_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 60);
                        if (damageable2.getDamage() > 190) {
                            damageable2.setDamage(239);
                        }
                    }
                    if (is2.getType() == Material.IRON_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 56);
                        if (damageable2.getDamage() > 180) {
                            damageable2.setDamage(224);
                        }
                    }
                    if (is2.getType() == Material.IRON_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 48);
                        if (damageable2.getDamage() > 150) {
                            damageable2.setDamage(194);
                        }
                    }
                    meta2 = (ItemMeta)damageable2;
                    is2.setItemMeta(meta2);
                }
            }
        }
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + killed.getName());
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
    }
    
}
