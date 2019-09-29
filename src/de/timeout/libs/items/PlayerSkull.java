package de.timeout.libs.items;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.authlib.GameProfile;

import de.timeout.libs.Reflections;
import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a PlayerSkull as ItemStack. Use this class only outside Main-Thread
 * @author timeout
 *
 */
public class PlayerSkull extends ItemStack {
	
	private static final Class<?> craftmetaskullClass = Reflections.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Class<?> craftskullClass = Reflections.getCraftBukkitClass("block.CraftSkull");
	
	private static final Field metaProfileField = Reflections.getField(craftmetaskullClass, "profile");
	private static final Field skullProfileField = Reflections.getField(craftskullClass, "profile");
	
	public static final ItemStack SKELETON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 0);
	public static final ItemStack WITHER_SKELETON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 1);
	public static final ItemStack ZOMBIE = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 2);
	public static final ItemStack CREEPER = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 4);
	public static final ItemStack ENDERDRAGON = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 5);
	public static final ItemStack STEVE = ItemStackAPI.createItemStack(Material.SKULL_ITEM, 1, (short) 3);
	
	private OfflinePlayer owner;
	private GameProfile profile;
	
	/**
	 * This Constructor creates a new Skull-ItemStack.
	 * @param displayname the displayname of this itemstack
	 * @param amount the amount of this skull item
	 * @param owner the owner of this skull item
	 * @throws InterruptedException if your Thread interrupts
	 * @throws ExecutionException if there was an error in this class. This should not happen. Send this report immediately
	 * @throws TimeoutException if the connection timed out and no GameProfile was avaiable
	 */
	public PlayerSkull(String displayname, int amount, OfflinePlayer owner) throws InterruptedException, ExecutionException, TimeoutException {
		super(ItemStackAPI.createItemStack(Material.SKULL_ITEM, amount > 0 ? amount : 1, (short) 3, ChatColor.translateAlternateColorCodes('&', displayname)));
		
		this.owner = owner;
		
		Future<GameProfile> request = overrideGameProfile();
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
		this(owner.getName(), amount, owner);
	}
	
	/**
	 * This Constructor creates a new Skull-ItemStack
	 * @param displayname the displayname of the skull
	 * @param amount the amount of the itemstack
	 * @param owner the owner of the skull
	 * @throws InterruptedException if your Thread interrupts
	 * @throws ExecutionException if there was an error in this class. This should not happen. Send this report immediately
	 * @throws TimeoutException if the connection timed out and no GameProfile was avaiable
	 * @throws IllegalStateException if the thread is the main thread
	 */
	public PlayerSkull(String displayname, int amount, UUID owner) throws InterruptedException, ExecutionException, TimeoutException {
		this(displayname, amount, Bukkit.getOfflinePlayer(owner));
	}
	
	/**
	 * This constructor create a new Skul-ItemStack
	 * @param username the displayname and name of the owner
	 * @throws InterruptedException if your Thread interrupts
	 * @throws ExecutionException if there was an error in this class. This should not happen. Send this report immediately
	 * @throws TimeoutException if the connection timed out and no GameProfile was avaiable
	 */
	@SuppressWarnings("deprecation")
	public PlayerSkull(String username) throws InterruptedException, ExecutionException, TimeoutException {
		this(username, 1, Bukkit.getOfflinePlayer(username));
	}

	/**
	 * This method downloads the gameprofile asynchronously
	 */
	private Future<GameProfile> overrideGameProfile() {
		// start async task
		return CompletableFuture.supplyAsync(() -> new GameProfileFetcher(owner).get());
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
		block.setType(Material.SKULL);
		// cast to Skull
		Skull skull = (Skull) block.getState();
		// set type to player skull
		skull.setSkullType(SkullType.PLAYER);
		// insert profile in Skull
		Reflections.setField(skullProfileField, skull, profile);
		// update Block
		skull.update();
		// return block
		return block;
	}
}
