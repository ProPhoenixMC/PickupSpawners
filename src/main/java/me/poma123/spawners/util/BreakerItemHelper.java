package me.poma123.spawners.util;

import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class BreakerItemHelper {
	private BreakerItemHelper() { }
	
    public static boolean matchingBreaker(ItemStack referenceBreakerItem, ItemStack currentBreakerItem) {
        if (currentBreakerItem.hasItemMeta() && referenceBreakerItem.hasItemMeta()) {
            ItemMeta referenceBreakerMeta = referenceBreakerItem.getItemMeta();
            ItemMeta currentBreakerMeta = currentBreakerItem.getItemMeta();

            if (!currentBreakerItem.getType().equals(referenceBreakerItem.getType())) {
            	return false;
            }
            if (referenceBreakerMeta.hasDisplayName()) {
                if (!currentBreakerMeta.hasDisplayName()) {
                	return false;
                } else {
                    if (!currentBreakerMeta.getDisplayName().equalsIgnoreCase(referenceBreakerMeta.getDisplayName())) {
                    	return false;
                    }
                }
            }
            if (referenceBreakerMeta.hasLore()) {
                if (!currentBreakerMeta.hasLore()) {
                	return false;
                } else {
                    if (!currentBreakerMeta.getLore().equals(referenceBreakerMeta.getLore())) {
                    	return false;
                    }
                }
            }

			if (referenceBreakerMeta instanceof Damageable && ((Damageable) referenceBreakerMeta).hasDamage()) {
				if (currentBreakerMeta instanceof Damageable && ((Damageable) currentBreakerMeta).hasDamage()) {
					if (((Damageable) currentBreakerMeta).getDamage() != ((Damageable) referenceBreakerMeta).getDamage()) {
						return false;
					}
				} else {
					return false;
				}
			}

            if (referenceBreakerMeta.hasEnchants()) {
                if (!currentBreakerMeta.hasEnchants()) {
                	return false;
                } else {
                	for (Entry<Enchantment, Integer> entry : referenceBreakerMeta.getEnchants().entrySet()) {
                		if (currentBreakerMeta.getEnchantLevel(entry.getKey()) < entry.getValue()) {
                			return false;
                		}
					}
                }
            }

            return true;
        } else {
            if (currentBreakerItem.equals(referenceBreakerItem)) {
                return true;
            }
        }
        
        return false;
    }
}
