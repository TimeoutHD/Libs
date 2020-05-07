package de.timeout.libs.items;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.timeout.libs.BukkitReflections;
import de.timeout.libs.Reflections;
import de.timeout.libs.profiles.GameProfileFetcher;
import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a PlayerSkull as ItemStack. Use this class only outside Main-Thread
 * @author timeout
 *
 */
public class PlayerSkull extends ItemStack {
	
	private static final Class<?> craftmetaskullClass = BukkitReflections.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Class<?> craftskullClass = BukkitReflections.getCraftBukkitClass("block.CraftSkull");
	
	private static final Field metaProfileField = Reflections.getField(craftmetaskullClass, "profile");
	private static final Field skullProfileField = Reflections.getField(craftskullClass, "profile");
	
	private static final Base64 base64 = new Base64();
	
	public static final ItemStack SKELETON = ItemStackAPI.createItemStack(Material.SKELETON_SKULL);
	public static final ItemStack WITHER_SKELETON = ItemStackAPI.createItemStack(Material.WITHER_SKELETON_SKULL);
	public static final ItemStack ZOMBIE = ItemStackAPI.createItemStack(Material.ZOMBIE_HEAD);
	public static final ItemStack CREEPER = ItemStackAPI.createItemStack(Material.CREEPER_HEAD);
	public static final ItemStack ENDERDRAGON = ItemStackAPI.createItemStack(Material.DRAGON_HEAD);
	public static final ItemStack STEVE = ItemStackAPI.createItemStack(Material.PLAYER_HEAD);
	
	private GameProfile profile;
	
	/**
	 * This Constructor creates a new Skull-ItemStack.
	 * @param displayname the displayname of this itemstack
	 * @param amount the amount of this skull item
	 * @throws InterruptedException if your Thread interrupts
	 * @throws ExecutionException if there was an error in this class. This should not happen. Send this report immediately
	 * @throws TimeoutException if the connection timed out and no GameProfile was avaiable
	 */
	public PlayerSkull(String displayname, int amount, UUID uuid) throws InterruptedException, ExecutionException, TimeoutException {
		super(ItemStackAPI.createItemStack(Material.PLAYER_HEAD, amount > 0 ? amount : 1, ChatColor.translateAlternateColorCodes('&', displayname)));
				
		Future<GameProfile> request = overrideGameProfile(uuid);
		// get Profile
		profile = request.get(5, TimeUnit.SECONDS);
		// Override profile field
		ItemMeta meta = getItemMeta();
		Reflections.setField(metaProfileField, meta, profile);
		setItemMeta(meta);
	}
	
	/**
	 * This Constructor creates a new Skull-ItemStack
	 * @param amount the amount of this itemstack
	 * @param owner the owner of the skull
	 * @throws InterruptedException if your Thread interrupts
	 * @throws ExecutionException if there was an error in this class. This should not happen. Send this report immediately
	 * @throws TimeoutException if the connection timed out and no GameProfile was avaiable
	 */
	public PlayerSkull(int amount, OfflinePlayer owner) throws InterruptedException, ExecutionException, TimeoutException {
		this(owner.getName(), amount, owner.getUniqueId());
	}
	
	public static ItemStack getCustomSkull(String url) {
		// create random GameProfile
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		// Validate
		Validate.notNull(profile.getProperties(), "Profile doesn't contain a property map");
		// encode data
        byte[] encodedData = base64.encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        // put values in profile
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        // create ItemStack and get ItemMeta
        ItemStack skull = ItemStackAPI.createItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();
        // write Profile in ItemMeta
		Reflections.setField(metaProfileField, meta, profile);
		// set meta in skull
		skull.setItemMeta(meta);
		// return skull
		return skull;
	}

	/**
	 * This method downloads the gameprofile asynchronously
	 */
	private static Future<GameProfile> overrideGameProfile(UUID uuid) {
		// start async task
		return CompletableFuture.supplyAsync(() -> new GameProfileFetcher(uuid).get());
	}
	
	/**
	 * This method converts the itemstack into a block and places the block at a certain location
	 * @param world the world of the location
	 * @param x the x coord of the block
	 * @param y the y coord of the block
	 * @param z the z coord of the block
	 * @return the block itself
	 */
	public Block toBlock(World world, double x, double y, double z) {
		return this.toBlock(world, (int) x, (int) y, (int) z);
	}
	
	/**
	 * This method converts the itemstack into a block and places the block at a certain location
	 * @param location the loation of the block
	 * @return the block itself
	 */
	public Block toBlock(Location location) {
		return this.toBlock(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	/**
	 * This method converts the itemstack into a block and places the block at a certain location
	 * @param world the world of the location
	 * @param x the x coord of the block
	 * @param y the y coord of the block
	 * @param z the z coord of the block
	 * @return the block itself
	 */
	public Block toBlock(World world, int x, int y, int z) {
		// get Block
		Block block = world.getBlockAt(x, y, z);
		// set type to skull
		block.setType(Material.PLAYER_HEAD);
		// cast to Skull
		Skull skull = (Skull) block.getState();
		// insert profile in Skull
		Reflections.setField(skullProfileField, skull, profile);
		// update Block
		skull.update();
		// return block
		return block;
	}
}
