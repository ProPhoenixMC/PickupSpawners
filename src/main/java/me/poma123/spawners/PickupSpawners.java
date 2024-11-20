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

import me.poma123.spawners.language.Language;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class PickupSpawners extends JavaPlugin implements org.bukkit.event.Listener {

    public static boolean debug = false;
    public static Material material = Material.getMaterial("SPAWNER");
    public static List<String> entities = new ArrayList<>();
    
    private static final Set<String> noSpawnEggEntities = Set.of("ILLUSIONER","GIANT","ENDER_DRAGON","WITHER");
    
    private static PickupSpawners instance;
    public int ID = 62455;
    SettingsManager s = SettingsManager.getInstance();
    private Metrics metrics;

    public PickupSpawners() {
        instance = this;
    }

    public static PickupSpawners getInstance() {
        return instance;
    }

    public static String generateRandomString(int length) {
    	return RandomStringUtils.random(length, true, true);
    }

    @Override
    public void onEnable() {
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
        Bukkit.getPluginManager().registerEvents(this, this);
        
        /*
         * Config saving
         */
        try {
            saveConfig();
        } catch (Exception e) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp()) {
                    p.sendMessage("§c[PickupSpawners] Plugin is disabled: Wrong breaker items. Please clear them, and setup the breaker items again.");
                }
            }
            getLogger().warning("§c[PickupSpawners] Plugin is disabled: Wrong breaker items. Please clear them, and setup the breaker items again.");
        }
        saveDefaultConfig();
        s.setup(PickupSpawners.getPlugin(PickupSpawners.class));

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
        if (s.getConfig().getConfigurationSection("item").getKeys(false).isEmpty()) {

            ItemStack is = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta im = is.getItemMeta();
            im.addEnchant(Enchantment.SILK_TOUCH, 1, false);
            is.setItemMeta(im);
            s.getConfig().set("item.default.itemstack", is);
            s.getConfig().set("item.default.material", "DIAMOND_PICKAXE");
            s.getConfig().set("item.default.enchants", Arrays.asList("SILK_TOUCH"));
            s.saveConfig();
        } else {
            for (String path : s.getConfig().getConfigurationSection("item").getKeys(false)) {
                if (s.getConfig().get("item." + path + ".itemstack") == null) {
                    String mat;
                    if (s.getConfig().getString("item." + path + ".material") != null) {
                        mat = s.getConfig().getString("item." + path + ".material");
                    } else {
                        mat = "";
                        continue;
                    }
                    ItemStack local = new ItemStack(Material.BEDROCK, 1);
                    if (Material.getMaterial(mat) == null && XMaterial.fromString(mat) == null && Material.valueOf(mat) == null) {
                        local = new ItemStack(Material.BEDROCK, 1);
                    } else {
                        if (Material.valueOf(mat) != null) {
                            local = new ItemStack(Material.valueOf(mat));
                        } else if (Material.getMaterial(mat) != null) {
                            local = new ItemStack(Material.getMaterial(mat));
                        } else if (Material.getMaterial(XMaterial.fromString(mat).parseMaterial().toString()) != null) {
                            local = new ItemStack(Material.getMaterial(XMaterial.fromString(mat).parseMaterial().toString()));
                        }
                    }

                    ItemMeta im = local.getItemMeta();
                    if (s.getConfig().get("item." + path + ".enchants") != null) {
                        for (String ench : s.getConfig().getStringList("item." + path + ".enchants")) {
                            if (ench.contains(":")) {
                                if (Enchantment.getByName(ench.split(":")[0].toUpperCase()) != null) {
                                    im.addEnchant(Enchantment.getByName(ench.split(":")[0].toUpperCase()), Integer.parseInt(ench.split(":")[1]), false);
                                }
                            } else {
                                if (Enchantment.getByName(ench.toUpperCase()) != null) {
                                    im.addEnchant(Enchantment.getByName(ench.toUpperCase()), 1, false);
                                }
                            }

                        }
                        local.setItemMeta(im);
                    }
                    s.getConfig().set("item." + path + ".itemstack", local);


                } else {
                    try {
                        ItemStack im = (ItemStack) s.getConfig().get("item." + path + ".itemstack");
                    } catch (Exception e) {
                        s.getConfig().set("item." + path + ".material_old", s.getConfig().getString("item." + path + ".itemstack.type"));
                        XMaterial xmat = XMaterial.requestXMaterial(s.getConfig().getString("item." + path + ".itemstack.type"), (Byte) s.getConfig().get("item." + path + ".itemstack.damage"));
                        s.getConfig().set("item." + path + ".itemstack", xmat.parseItem());

                    }

                }

            }
            s.saveConfig();


        }

        /*
         * Update check
         */
        if (s.getConfig().getBoolean("update-check")) {
            new Updater(this, ID, this.getFile(), Updater.UpdateType.VERSION_CHECK, true);
        }


        /*
         * Metrics setup
         */
        metrics = new Metrics(this);
        this.metrics.addCustomChart(new Metrics.SingleLineChart("spawners_broken", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int c = Listener.breakedSpawners;

                return c;

            }
        }));
        this.metrics.addCustomChart(new Metrics.SimplePie("auto_language_use", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String c;
                if (s.getConfig().getBoolean("auto-locale") == true) {
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

                if (s.getConfig().getConfigurationSection("item").getValues(false) != null) {
                    c = String.valueOf(s.getConfig().getConfigurationSection("item").getValues(false).size());
                }

                return c;

            }
        }));

    }
}
