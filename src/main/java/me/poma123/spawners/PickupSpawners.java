/*******************************************************************************
 * This file is part of PickupSpawners.
 *
 *       PickupSpawners is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       PickupSpawners is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with PickupSpawners.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package me.poma123.spawners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.poma123.spawners.language.Language;
import me.poma123.spawners.listener.BlockEventListener;
import me.poma123.spawners.listener.Listener;
import net.md_5.bungee.api.ChatColor;

public class PickupSpawners extends JavaPlugin {

    public static boolean debug = false;
    public static Material material = Material.getMaterial("SPAWNER");
    public static List<String> entities = new ArrayList<>();
    
    private static final Set<String> noSpawnEggEntities = Set.of("ILLUSIONER","GIANT","ENDER_DRAGON","WITHER");
    
    private static PickupSpawners instance;
    private SettingsManager settingsManager;
    private LimitManager limitManager;
    
    public int ID = 62455;
    private Metrics metrics;

    public static PickupSpawners getInstance() {
    	if (instance == null) throw new IllegalStateException("The plugin is not in initalized state");
        return instance;
    }
    
    public SettingsManager getSettingsManager() {
    	return settingsManager;
    }
    
    public LimitManager getLimitManager() {
    	return limitManager;
    }

    public static String generateRandomString(int length) {
    	return RandomStringUtils.random(length, true, true);
    }

    @Override
    public void onEnable() {
    	instance = this;
    	settingsManager = new SettingsManager(this);
    	limitManager = new LimitManager(this);
    	
        Language.saveLocales();

        getLogger().info("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
        getLogger().info("-+      PickupSpawners       +-");
        getLogger().info("-+        by poma123         +-");
        getLogger().info("-+                           +-");
        getLogger().info("-+        Made with <3       +-");
        getLogger().info("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");

        /*
         * Getting and saving available entities
         */

		for (EntityType entity : EntityType.values()) {
			if (Material.getMaterial(entity.toString().toUpperCase() + "_SPAWN_EGG") != null || noSpawnEggEntities.contains(entity.toString().toUpperCase())) {
				if (debug) {
					getLogger().info("[Debug] " + ChatColor.GREEN + entity.toString() + " added to the entities list.");
				}
				entities.add(entity.toString().toLowerCase());
			} else {
				if (debug) {
					getLogger().info("[Debug] " + ChatColor.RED + entity.toString() + " NOT added to the entities list.");
				}
			}
		}

        /*
         * Command registering
         */
        getCommand("pickupspawners").setExecutor(new PSCommand());
        getCommand("pickupspawners").setTabCompleter(new PSCommand());

        /*
         * Listener registering
         */
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(this), this);


        /*
         * Setting new configuration sections
         */
        if (getConfig().get("break-limits") == null) {

            if (getConfig().get("daily-broke-limit") != null) {
                getConfig().set("break-limits.default", getConfig().getInt("daily-broke-limit"));
            } else {
                getConfig().set("break-limits.default", 0);
            }

            saveConfig();

        }

        /*
         * Setting the default spawner breaker item if the list is empty
         */
        if (settingsManager.getConfig().getConfigurationSection("item").getKeys(false).isEmpty()) {

            ItemStack is = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta im = is.getItemMeta();
            im.addEnchant(Enchantment.SILK_TOUCH, 1, false);
            is.setItemMeta(im);
            settingsManager.getConfig().set("item.default.itemstack", is);
            settingsManager.getConfig().set("item.default.material", "DIAMOND_PICKAXE");
            settingsManager.getConfig().set("item.default.enchants", Arrays.asList("SILK_TOUCH"));
            settingsManager.saveConfig();
        } else {
            for (String path : settingsManager.getConfig().getConfigurationSection("item").getKeys(false)) {
                if (settingsManager.getConfig().get("item." + path + ".itemstack") == null) {
                	getLogger().warning("Invalid breaker item: " + path + ". Removed.");
                	settingsManager.getConfig().set("item." + path, null);
                }
            }
            settingsManager.saveConfig();
        }

        /*
         * Update check
         */
        if (settingsManager.getConfig().getBoolean("update-check")) {
            new Updater(this, ID, this.getFile(), Updater.UpdateType.VERSION_CHECK, true);
        }


        /*
         * Metrics setup
         */
        metrics = new Metrics(this);
        this.metrics.addCustomChart(new Metrics.SingleLineChart("spawners_broken", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int c = limitManager.getBreakedSpawners();

                return c;

            }
        }));
        this.metrics.addCustomChart(new Metrics.SimplePie("auto_language_use", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String c;
                if (settingsManager.getConfig().getBoolean("auto-locale") == true) {
                    c = "using";
                } else {
                    c = "not using";
                }

                return c;
            }
        }));
        this.metrics.addCustomChart(new Metrics.SimplePie("spawner_breaker_items", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String c = "N/A";

                if (settingsManager.getConfig().getConfigurationSection("item").getValues(false) != null) {
                    c = String.valueOf(settingsManager.getConfig().getConfigurationSection("item").getValues(false).size());
                }

                return c;

            }
        }));

    }
}
