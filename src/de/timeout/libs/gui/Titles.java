package de.timeout.libs.gui;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import de.timeout.libs.BukkitReflections;

public class Titles {
	
	private static final String SENDPACKET = "sendPacket";
	
	private static final Class<?> packetplayouttitleClass = BukkitReflections.getNMSClass("PacketPlayOutTitle");
	private static final Class<?> entityplayerClass = BukkitReflections.getNMSClass("EntityPlayer");
	private static final Class<?> enumtitleactionClass = BukkitReflections.getSubClass(packetplayouttitleClass, "EnumTitleAction");
	private static final Class<?> ichatbasecomponentClass = BukkitReflections.getNMSClass("IChatBaseComponent");
	private static final Class<?> chatserializerClass = BukkitReflections.getSubClass(ichatbasecomponentClass, "ChatSerializer");
	private static final Class<?> packetClass = BukkitReflections.getNMSClass("Packet");
	private static final Class<?> playerconnectionClass = BukkitReflections.getNMSClass("PlayerConnection");
	private static final Class<?> packetplayoutchatClass = BukkitReflections.getNMSClass("PacketPlayOutChat");
	
	private static final Field playerconnectionField = BukkitReflections.getField(entityplayerClass, "playerConnection");
	
	private Titles() {
		/* EMPTY, cause Util-Class */
	}
	
	public static void sendTitle(Player p, String msg, int fadein, int stay, int fadeout) {
		try {
			// Define Packet
			Object packet = packetplayouttitleClass.getConstructor(enumtitleactionClass, ichatbasecomponentClass, int.class, int.class, int.class)
					.newInstance(enumtitleactionClass.getField("TITLE").get(enumtitleactionClass),
							chatserializerClass.getMethod("a", String.class).invoke(chatserializerClass, createJsonObject(msg)), fadein, stay, fadeout);
			// Send packet
			playerconnectionClass.getMethod(SENDPACKET, packetClass).invoke(BukkitReflections.getValue(playerconnectionField, BukkitReflections.getEntityPlayer(p)), packet);
		} catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot send Title", e);
		}
	}
	
	public static void sendSubTitle(Player p, String msg, int fadein, int stay, int fadeout) {
		try {
			// Define Packet
			Object packet = packetplayouttitleClass.getConstructor(enumtitleactionClass, ichatbasecomponentClass, int.class, int.class, int.class)
					.newInstance(enumtitleactionClass.getField("SUBTITLE").get(enumtitleactionClass),
							chatserializerClass.getMethod("a", String.class).invoke(chatserializerClass, createJsonObject(msg)), fadein, stay, fadeout);
			// Send packet
			playerconnectionClass.getMethod(SENDPACKET, packetClass).invoke(BukkitReflections.getValue(playerconnectionField, BukkitReflections.getEntityPlayer(p)), packet);
		} catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot send Subtitle", e);
		}
	}
	
	public static void sendActionBar(Player p, String msg) {
		try {
			Object cbc = chatserializerClass.getMethod("a", String.class).invoke(chatserializerClass, createJsonObject(msg));
			Object packet = packetplayoutchatClass.getConstructor(ichatbasecomponentClass, byte.class).newInstance(cbc, (byte) 2);
			// Send Packet
			playerconnectionClass.getMethod(SENDPACKET, packetClass).invoke(BukkitReflections.getValue(playerconnectionField, BukkitReflections.getEntityPlayer(p)), packet);
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
