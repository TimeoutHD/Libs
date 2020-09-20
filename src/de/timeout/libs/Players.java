package de.timeout.libs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

public final class Players {

	private static final Class<?> packetClass = BukkitReflections.getNMSClass("Packet");
	private static final Class<?> entityhumanClass = BukkitReflections.getNMSClass("EntityHuman");
	
	private static final @NotNull Field activeContainer = Objects.requireNonNull(Reflections.getField(entityhumanClass, "activeContainer"));
	
	private Players() {
		/* UTIL-Classes does not need any instantiations */
	}
	
	/**
	 * This Method returns the player's GameProfile
	 * @param player the owner of the GameProfile
	 * @return the Gameprofile
	 */
	public static GameProfile getGameProfile(@NotNull Player player) {
		 try {
			Class<?> craftplayerClass = BukkitReflections.getCraftBukkitClass("entity.CraftPlayer");
			return craftplayerClass != null ? (GameProfile) craftplayerClass.getMethod("getProfile").invoke(player) : null;
		 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Logger.getGlobal().log(Level.INFO, e, () -> "Could not get GameProfile from Player " + player.getName());
		 }
		 return new GameProfile(player.getUniqueId(), player.getName());
	}
	
	/**
	 * This method returns an EntityPlayer-Object of a player
	 * @param player the player
	 * @return the EntityPlayer as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	public static Object getEntityPlayer(@NotNull Player player) throws ReflectiveOperationException {
		Method getHandle = player.getClass().getMethod("getHandle");
		return getHandle.invoke(player);
	}
	
	/**
	 * This method returns the active container of the player.
	 * @param player the player itself
	 * @return the active container of the player. Is null if there is no active container
	 * @throws ReflectiveOperationException If an internal Exception happens. Please report this exception
	 * @throws IllegalArgumentException if the player is null
	 */
	public static Object getActiveContainer(@NotNull Player player) throws ReflectiveOperationException {
		// get EntityPlayer
		Object entityPlayer = getEntityPlayer(player);
			
		// return active container
		return Reflections.getValue(activeContainer, entityPlayer);
	}
	
	/**
	 * This method returns the PlayerConnection as an Object
	 * @param player the owner of the player connection
	 * @return the PlayerConnection as Object
	 * @throws ReflectiveOperationException if there was an error
	 */
	@NotNull
	public static Object getPlayerConnection(@NotNull Player player) throws ReflectiveOperationException {
		// Validate
		Validate.notNull(player, "Player cannot be null");
		
		Object nmsp = getEntityPlayer(player);
		Field con = nmsp.getClass().getField("playerConnection");
		return con.get(nmsp);
	}
	
	/**
	 * This method sends a Packet to a Player
	 * @param player the Player
	 * @param packet the packet
	 */
	@NotNull
	public static void sendPacket(@NotNull Player player, @NotNull Object packet) {
		// Validate
		Validate.notNull(player, "Player cannot be null");
		Validate.notNull(packet, "Packet cannot be null");
		
		CompletableFuture.runAsync(() -> {
			try {
				Object playerConnection = getPlayerConnection(player);
				playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
			} catch (ReflectiveOperationException e) {
				Logger.getGlobal().log(Level.WARNING, "Unable to send packet to player", e);
			}
		});
	}
	
}
