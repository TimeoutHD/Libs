package de.timeout.libs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

public final class BukkitReflections extends Reflections {
	
	private static final Class<?> packetClass = getNMSClass("Packet");
	
	/**
	 * This method return an NMS-Class, which has a certain name
	 * @param nmsClass the name of the NMS-Class
	 * @return the CLass itself. Null if the class cannot be found.
	 */
	public static Class<?> getNMSClass(String nmsClass) {
		try {
			String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "net.minecraft.server." + version + nmsClass;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Logger.getGlobal().log(Level.WARNING, "Could not find NMS-Class " + nmsClass, e);
		}
		return null;
	}

	/**
	 * This method returns a CraftBukkit-Class 
	 * @param clazz the Craftbukkit-Class
	 * @return the CraftBukkit-Class. Null if the class cannot be found
	 */
	public static Class<?> getCraftBukkitClass(String clazz) {
		try {
			String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
			String name = "org.bukkit.craftbukkit." + version + clazz;
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			Logger.getGlobal().log(Level.WARNING, "Could not find CraftBukkit-Class " + clazz, e);
		}
		return null;
	}
	
	/**
	 * This Method returns the player's GameProfile
	 * @param player the owner of the GameProfile
	 * @return the Gameprofile
	 */
	public static GameProfile getGameProfile(Player player) {
		 try {
			Class<?> craftplayerClass = getCraftBukkitClass("entity.CraftPlayer");
			return craftplayerClass != null ? (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player) : null;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			Logger.getGlobal().log(Level.INFO, "Could not get GameProfile from Player " + player.getName(), e);
		}
		 return new GameProfile(player.getUniqueId(), player.getName());
	}
	
	/**
	 * This method returns an EntityPlayer-Object of a player
	 * @param player the player
	 * @return the EntityPlayer as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	public static Object getEntityPlayer(Player player) throws ReflectiveOperationException {
		Method getHandle = player.getClass().getMethod("getHandle");
		return getHandle.invoke(player);
	}
	
	/**
	 * This method returns the PlayerConnection as an Object
	 * @param player the owner of the player connection
	 * @return the PlayerConnection as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	public static Object getPlayerConnection(Player player) throws ReflectiveOperationException {
		Object nmsp = getEntityPlayer(player);
		Field con = nmsp.getClass().getField("playerConnection");
		return con.get(nmsp);
	}
	
	/**
	 * This method sends a Packet to a Player
	 * @param player the Player
	 * @param packet the packet
	 * @throws ReflectiveOperationException if the object is not a packet
	 */
	public static void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
		Object playerConnection = getPlayerConnection(player);
		playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
	}
	
}
