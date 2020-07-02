package de.timeout.libs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

public final class Players {

	private static final Class<?> packetClass = BukkitReflections.getNMSClass("Packet");
	private static final Class<?> entityhumanClass = BukkitReflections.getNMSClass("EntityHuman");
	
	private static final Field activeContainer = Reflections.getField(entityhumanClass, "activeContainer");
	
	private Players() {
		/* UTIL-Classes does not need any instantiations */
	}
	
	/**
	 * This Method returns the player's GameProfile
	 * @param player the owner of the GameProfile
	 * @return the Gameprofile
	 */
	public static GameProfile getGameProfile(Player player) {
		 try {
			Class<?> craftplayerClass = BukkitReflections.getCraftBukkitClass("entity.CraftPlayer");
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
	 * This method returns the active container of the player.
	 * @param player the player itself
	 * @return the active container of the player
	 */
	public static Object getActiveContainer(@Nonnull Player player) {
		// Validate
		Validate.notNull(player, "Player cannot be null");
		
		try {
			// get EntityPlayer
			Object entityPlayer = getEntityPlayer(player);
			
			// return active container
			return Reflections.getValue(activeContainer, entityPlayer);
		} catch (ReflectiveOperationException e) {
			Bukkit.getLogger().log(Level.WARNING, String.format("Unable to get active container of player %s", player.getName()), e);
		}
		
		return null;
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
