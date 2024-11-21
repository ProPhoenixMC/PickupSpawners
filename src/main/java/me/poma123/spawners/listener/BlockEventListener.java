package me.poma123.spawners.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.poma123.spawners.PickupSpawners;
import me.poma123.spawners.SettingsManager;
import me.poma123.spawners.SettingsManager.SettingKey;
import me.poma123.spawners.event.SpawnerBreakEvent;
import me.poma123.spawners.language.Language;
import me.poma123.spawners.language.Language.LocalePath;
import me.poma123.spawners.util.BreakerItemHelper;

public class BlockEventListener implements Listener {
	private final PickupSpawners pickupSpawners;
	private final SettingsManager settingsManager;
	
	public BlockEventListener(PickupSpawners pickupSpawners) {
		this.pickupSpawners = pickupSpawners;
		settingsManager = pickupSpawners.getSettingsManager();
	}
	
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Block s = e.getBlock();
        
        if (!s.getType().equals(Material.SPAWNER)) {
        	return;
        }
        
        if (!e.getPlayer().hasPermission("spawnerlimit.bypass")) {
        	int limit = pickupSpawners.getLimitManager().getLimit(e.getPlayer());
            int current = pickupSpawners.getLimitManager().getCurrentBreaks(e.getPlayer().getName());
            
            if (current >= limit) {
            	e.getPlayer().sendMessage(Language.getReplacedLocale(e.getPlayer(), Language.LocalePath.LIMIT_REACH, "%limit%", String.valueOf(limit)));
            	
            	e.setCancelled(true);
                return;
            }
        }

        ItemStack currentBreakerItem = e.getPlayer().getInventory().getItemInMainHand();
        if (currentBreakerItem == null) {
            return;
        }
        
        if (holdingBreakerItem(e.getPlayer())) {
        	SpawnerBreakEvent spawnerBreakEvent = new SpawnerBreakEvent(e.getPlayer(), s, currentBreakerItem);
        	Bukkit.getPluginManager().callEvent(spawnerBreakEvent);
        	
        	if (spawnerBreakEvent.isCancelled()) {
        		//e.setCancelled(true); TODO: rename to spawnerPICKUPevent
        		return;
        	}
        	
        	CreatureSpawner creatureSpawner = (CreatureSpawner) s.getState();
            if (creatureSpawner.getSpawnedType() != null) {
            	e.setExpToDrop(0);
            	
            	ItemStack spawnerItemStack = new ItemStack(Material.SPAWNER, 1);
                ItemMeta spawnerMeta = spawnerItemStack.getItemMeta();
                
                spawnerMeta.setDisplayName("§e" + creatureSpawner.getSpawnedType().name().toLowerCase() + " §7Spawner");
                spawnerMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                spawnerItemStack.setItemMeta(spawnerMeta);
                
                s.getWorld().dropItemNaturally(s.getLocation(), spawnerItemStack);
                e.getPlayer().sendMessage(Language.getReplacedLocale(e.getPlayer(), LocalePath.BREAK, "%type%", creatureSpawner.getSpawnedType().name().toLowerCase()));
            }
        	
        	
            if (!e.getPlayer().hasPermission("spawnerlimit.bypass")) {
            	pickupSpawners.getLimitManager().incrementBreaks(e.getPlayer().getName());
            }
        	
        } else {
            if (!e.getPlayer().hasPermission("pickupspawners.bypasspickupblock")) {
                e.getPlayer().sendMessage(Language.getLocale(e.getPlayer(), Language.LocalePath.CANNOT_PICKUP));
                e.setCancelled(true);
            }
        }
    }
    
    private boolean holdingBreakerItem(Player player) {
    	ItemStack currentBreakerItem = player.getInventory().getItemInMainHand();
        if (currentBreakerItem == null) {
            return false;
        }
        
        for (String itemKey : settingsManager.getKeys(SettingKey.ITEM)) {
        	ItemStack referenceBreakerItem = (ItemStack) settingsManager.getConfig().get("item." + itemKey + ".itemstack");
        	
			if (referenceBreakerItem == null) {
				return false;
			}
			
			if (BreakerItemHelper.matchingBreaker(referenceBreakerItem, currentBreakerItem)) {
				String permission = settingsManager.getStringValue("item." + itemKey + ".permission");
				if (permission != null) {
					if (player.hasPermission(permission)) {
						return true;
					} else {
						player.sendMessage(Language.getLocale(player, LocalePath.NO_PERM));
						return false;
					}
				} else {
					return true;
				}
			}
		}
        
        return false;
    }
}
