// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import java.lang.reflect.Constructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Title
{
    public void sendPacket(final Player player, final Object packet) {
        try {
            final Object handle = player.getClass().getMethod("getHandle", (Class<?>[])new Class[0]).invoke(player, new Object[0]);
            final Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", this.getNMSClass("Packet")).invoke(playerConnection, packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Class<?> getNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void send(final Player player, final String title, final String subtitle, final int fadeInTime, final int showTime, final int fadeOutTime) {
        try {
            final Object chatTitle = this.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + title + "\"}");
            final Constructor<?> titleConstructor = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            final Object packet = titleConstructor.newInstance(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle, fadeInTime, showTime, fadeOutTime);
            final Object chatsTitle = this.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + subtitle + "\"}");
            final Constructor<?> timingTitleConstructor = this.getNMSClass("PacketPlayOutTitle").getConstructor(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], this.getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            final Object timingPacket = timingTitleConstructor.newInstance(this.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle, fadeInTime, showTime, fadeOutTime);
            this.sendPacket(player, packet);
            this.sendPacket(player, timingPacket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
