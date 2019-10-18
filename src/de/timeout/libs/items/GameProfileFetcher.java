package de.timeout.libs.items;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import de.timeout.libs.Reflections;

/**
 * 
 * @author timeout
 *
 * NOTICE: Signature key is in {@link YggdrasilMinecraftSessionService#publicKey}
 */
class GameProfileFetcher implements Supplier<GameProfile> {
			
	private static final Class<?> tileentityskullClass = Reflections.getNMSClass("TileEntitySkull");
	
	@SuppressWarnings("unchecked")
	private static final LoadingCache<String, GameProfile> skinCache = (LoadingCache<String, GameProfile>) Reflections.getValue(Reflections.getField(tileentityskullClass, "skinCache"), tileentityskullClass);
	private static final Executor executor = (Executor) Reflections.getValue(Reflections.getField(tileentityskullClass, "executor"), tileentityskullClass);
	
	private OfflinePlayer owner;
	
	public GameProfileFetcher(OfflinePlayer owner) {
		this.owner = owner;
	}

	@Override
	public GameProfile get() {
		// get Profile from cache
		GameProfile localProfile = lookUpGameProfileFromCache();
		// if GameProfile is not in cache
		if(localProfile == null) {
			// if owner is not online
			if(!owner.isOnline()) {
				// get GameProfile from other thread
				CompletableFuture<GameProfile> future = CompletableFuture.supplyAsync(() -> 
					// load and return from cache
					skinCache.getUnchecked(owner.getName())
				, executor);
				// return value
				try {
					return future.get(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					Bukkit.getLogger().log(Level.SEVERE, "Fatal error by look up GameProfile of " + owner.getName() + ". Thread interrupted", e);
				} catch (ExecutionException e) {
					Bukkit.getLogger().log(Level.WARNING, "Unchecked exception while reading GameProfile of " + owner.getName(), e);
				} catch (TimeoutException e) {
					Bukkit.getLogger().log(Level.WARNING, "Unable to get GameProfile of " + owner.getName() + ". Connection timed out...");
				}
			// get GameProfile from owner
			} else localProfile = Reflections.getGameProfile(owner.getPlayer());
		}
		// return profile
		return localProfile;
	}
	
	/**
	 * This Method returns the GameProfile from the cache. If the value is not in the cache it will return null
	 * @return the value or null if it does not exist.
	 */
	private GameProfile lookUpGameProfileFromCache() {
		// create current local copy of cache
		Map<String, GameProfile> profile = new HashMap<>(skinCache.asMap());
		// return value from map
		return profile.get(owner.getName());
	}
}
