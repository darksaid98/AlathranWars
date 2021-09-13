package me.ShermansWorld.AlathraWar;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Title {
	public void sendPacket(Player player, Object packet) {
	    try {
	        Object handle = player.getClass().getMethod("getHandle").invoke(player);
	        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
	        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public Class<?> getNMSClass(String name) {
	    try {
	        return Class.forName("net.minecraft.server."
	                + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public void send(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
	    try {
	        Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
	                .invoke(null, "{\"text\": \"" + title + "\"}");
	        Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
	                int.class, int.class, int.class);
	        Object packet = titleConstructor.newInstance(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
	                fadeInTime, showTime, fadeOutTime);

	        Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
	                .invoke(null, "{\"text\": \"" + subtitle + "\"}");
	        Constructor<?> timingTitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
	                int.class, int.class, int.class);
	        Object timingPacket = timingTitleConstructor.newInstance(
	                getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
	                fadeInTime, showTime, fadeOutTime);

	        sendPacket(player, packet);
	        sendPacket(player, timingPacket);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
