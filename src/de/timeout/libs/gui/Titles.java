package de.timeout.libs.gui;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import de.timeout.libs.BukkitReflections;
import de.timeout.libs.Players;
import de.timeout.libs.Reflections;
import org.jetbrains.annotations.NotNull;

public class Titles {
	
	private static final String SENDPACKET = "sendPacket";
	
	private static final Class<?> packetplayouttitleClass = BukkitReflections.getNMSClass("PacketPlayOutTitle");
	private static final Class<?> entityplayerClass = BukkitReflections.getNMSClass("EntityPlayer");
	private static final Class<?> enumtitleactionClass = Reflections.getSubClass(packetplayouttitleClass, "EnumTitleAction");
	private static final Class<?> ichatbasecomponentClass = BukkitReflections.getNMSClass("IChatBaseComponent");
	private static final Class<?> chatserializerClass = Reflections.getSubClass(ichatbasecomponentClass, "ChatSerializer");
	private static final Class<?> packetClass = BukkitReflections.getNMSClass("Packet");
	private static final Class<?> playerconnectionClass = BukkitReflections.getNMSClass("PlayerConnection");
	private static final Class<?> packetplayoutchatClass = BukkitReflections.getNMSClass("PacketPlayOutChat");
	
	private static final @NotNull Field playerconnectionField = Objects.requireNonNull(Reflections.getField(entityplayerClass, "playerConnection"));
	
	private Titles() {
		/* EMPTY, cause Util-Class */
	}
	
	public static void sendTitle(Player p, String msg, int fadein, int stay, int fadeout) {
		sendTitlePacket(p, msg, "TITLE", fadein, stay, fadeout);
	}
	
	public static void sendSubTitle(Player p, String msg, int fadein, int stay, int fadeout) {
		sendTitlePacket(p, msg, "SUBTITLE", fadein, stay, fadeout);
	}
	
	private static void sendTitlePacket(Player p, String msg, String type, int fadein, int stay, int fadeout) {
		try {
			// Define Packet
			Object packet = packetplayouttitleClass.getConstructor(enumtitleactionClass, ichatbasecomponentClass, int.class, int.class, int.class)
					.newInstance(Objects.requireNonNull(enumtitleactionClass).getField(type).get(enumtitleactionClass),
							Objects.requireNonNull(chatserializerClass).getMethod("a", String.class).invoke(chatserializerClass, createJsonObject(msg)), fadein, stay, fadeout);
			// Send packet
			playerconnectionClass.getMethod(SENDPACKET, packetClass).invoke(Reflections.getValue(playerconnectionField, Players.getEntityPlayer(p)), packet);
		} catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot send Packet", e);
		}

	}
	
	public static void sendActionBar(Player p, String msg) {
		try {
			Object cbc = Objects.requireNonNull(chatserializerClass).getMethod("a", String.class).invoke(chatserializerClass, createJsonObject(msg));
			Object packet = packetplayoutchatClass.getConstructor(ichatbasecomponentClass, byte.class).newInstance(cbc, (byte) 2);
			// Send Packet
			playerconnectionClass.getMethod(SENDPACKET, packetClass).invoke(Reflections.getValue(playerconnectionField, Players.getEntityPlayer(p)), packet);
		} catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot send ActionBar", e);
		}
	}
	
	private static String createJsonObject(String msg) {
		JsonObject obj = new JsonObject();
		obj.addProperty("text", msg);
		return obj.toString();
	}
}
