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

package me.poma123.spawners.listener;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.poma123.spawners.PickupSpawners;
import me.poma123.spawners.SettingsManager;
import me.poma123.spawners.Updater;
import me.poma123.spawners.event.SpawnerPlaceEvent;
import me.poma123.spawners.gui.PickupGui;
import me.poma123.spawners.language.Language;
import me.poma123.spawners.language.Language.LocalePath;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Listener implements org.bukkit.event.Listener {
    PickupSpawners ps = PickupSpawners.getInstance();
    SettingsManager sett = PickupSpawners.getInstance().getSettingsManager();
    private Plugin plugin = PickupSpawners.getInstance();

    public static String getLang(Player p) {
            String locale;
            try {
                Method getLocale = Player.Spigot.class.getMethod("getLocale");
                locale = (String) getLocale.invoke(p.spigot());
            } catch (Exception e) {
                locale = p.getLocale();
            }

            String[] s = StringUtils.split(locale, '_');
            return s[0];
    }

    public static String getLangExact(Player p) {

        String locale;
        try {
            Method getLocale = Player.Spigot.class.getMethod("getLocale");
            locale = (String) getLocale.invoke(p.spigot());
        } catch (Exception e) {
            locale = p.getLocale();
        }

        return locale;
    }

    public static TextComponent getHoverClick(String message, String hover, String click) {
        TextComponent text = new TextComponent(message);
        text.setClickEvent(
                new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, click));
        text.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()));
        return text;
    }

    public static TextComponent getHoverSuggest(String message, String hover, String suggestedcommand) {
        TextComponent text = new TextComponent(message);
        text.setClickEvent(
                new net.md_5.bungee.api.chat.ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ChatColor.stripColor(suggestedcommand)));
        text.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()));
        return text;
    }

    public static TextComponent getHoverClickcmd(String message, String hover, String click) {
        TextComponent text = new TextComponent(message);
        text.setClickEvent(
                new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, click));
        text.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()));
        return text;
    }

    public static <K, V extends Comparable<V>> NavigableMap<K, V> sortByValues(final Map<K, V> map) {
        final Comparator<K> valueComparator = new Comparator<K>() {
            @Override
            public int compare(final K k1, final K k2) {
                final int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) {
                    return 1;
                }
                return compare;
            }
        };
        final NavigableMap<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll((Map<? extends K, ? extends V>) map);
        return sortedByValues;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        Inventory open = e.getClickedInventory();
        ItemStack items = e.getCurrentItem();

        int slot = e.getSlot();
        if (open == null) {
            return;
        }
        if (e.getView().getTitle().equalsIgnoreCase("§1PickupSpawners Main Page")) {
            e.setCancelled(true);
            if (e.isShiftClick()) {
                return;
            }
            if ((items == null) || (!items.hasItemMeta())) {
                return;
            }
            if (!items.getItemMeta().hasDisplayName()) {
                return;
            }
            if (e.getCurrentItem() != null) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§6§lGive spawners")) {
                    PickupGui gui = new PickupGui();
                    e.getWhoClicked().closeInventory();
                    gui.spawnerGiveList((Player) e.getWhoClicked(), 1);


                }
                if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§c§lBreaker items")) {
                    PickupGui gui = new PickupGui();
                    e.getWhoClicked().closeInventory();
                    gui.breakerItems((Player) e.getWhoClicked(), 1);


                }
            }


        }
        if (e.getView().getTitle().equalsIgnoreCase("§1PickupSpawners > Breaker Items")) {
            e.setCancelled(true);
            if (e.isShiftClick()) {
                return;
            }
            if ((items == null) || (!items.hasItemMeta())) {
                return;
            }


            if (items.getItemMeta().getLore().toString().contains("BreakerID")) {

                String str = ChatColor.stripColor(items.getItemMeta().getLore().get(items.getItemMeta().getLore().size() - 1)).replace("BreakerID: ", "");
                player.closeInventory();
                sendBreakerItemCommands(player, str);
            }
            if (!items.getItemMeta().hasDisplayName()) {
                return;
            }
            if (items.getItemMeta().getDisplayName().contains("Back")) {

                PickupGui gui = new PickupGui();
                e.getWhoClicked().closeInventory();
                String lore = items.getItemMeta().getLore().get(0);
                if (lore.contains("Back to page")) {
                    int page = Integer.parseInt(lore.replace("§7Back to page ", ""));
                    e.getWhoClicked().closeInventory();
                    gui.breakerItems((Player) e.getWhoClicked(), page);
                } else {
                    e.getWhoClicked().closeInventory();
                    gui.mainSpawnersGui((Player) e.getWhoClicked());
                }


            }


            if (items.getItemMeta().getDisplayName().contains("Next")) {
                PickupGui gui = new PickupGui();
                e.getWhoClicked().closeInventory();
                String lore = items.getItemMeta().getLore().get(0);
                if (lore.contains("Go to page")) {
                    int page = Integer.parseInt(lore.replace("§7Go to page ", ""));
                    e.getWhoClicked().closeInventory();
                    gui.breakerItems((Player) e.getWhoClicked(), page);
                }
            }
        }
        if (e.getView().getTitle().equalsIgnoreCase("§1PickupSpawners > Give")) {
            e.setCancelled(true);
            if (e.isShiftClick()) {
                return;
            }
            if ((items == null) || (!items.hasItemMeta())) {
                return;
            }
            if (!items.getItemMeta().hasDisplayName()) {
                return;
            }

            if (items.getItemMeta().getDisplayName().contains("Back")) {

                PickupGui gui = new PickupGui();
                e.getWhoClicked().closeInventory();
                String lore = items.getItemMeta().getLore().get(0);
                if (lore.contains("Back to page")) {
                    int page = Integer.parseInt(lore.replace("§7Back to page ", ""));
                    e.getWhoClicked().closeInventory();
                    gui.spawnerGiveList((Player) e.getWhoClicked(), page);
                } else {
                    e.getWhoClicked().closeInventory();
                    gui.mainSpawnersGui((Player) e.getWhoClicked());
                }


            }


            if (items.getItemMeta().getDisplayName().contains("Next")) {
                PickupGui gui = new PickupGui();
                e.getWhoClicked().closeInventory();
                String lore = items.getItemMeta().getLore().get(0);
                if (lore.contains("Go to page")) {
                    int page = Integer.parseInt(lore.replace("§7Go to page ", ""));
                    e.getWhoClicked().closeInventory();
                    gui.spawnerGiveList((Player) e.getWhoClicked(), page);
                }
            }
            if (items.getItemMeta().getDisplayName().contains("§6§l")) {
                String type = ChatColor.stripColor(items.getItemMeta().getDisplayName().split(" ")[0].toLowerCase());


                if (ps.entities.contains(type)) {
                    player.closeInventory();

                    ItemStack spawner = new ItemStack(me.poma123.spawners.PickupSpawners.material, 1);
                    ItemMeta swmeta = spawner.getItemMeta();
                    // swmeta.setLocalizedName();
                    swmeta.setDisplayName("§e" + type.toLowerCase() + " §7Spawner");

                    spawner.setItemMeta(swmeta);

                    player.getInventory().addItem(spawner);

                    player.sendMessage(Language.getReplacedLocale(player, LocalePath.GIVE, "%count% %type%", 1 + " " + type.toLowerCase()));

                } else {
                    player.sendMessage(me.poma123.spawners.listener.Listener.getLang(player).equals("hu")
                            ? "§cA megadott entitás típus nem létezik."
                            : "§cThis entity type is invalid.");
                }
            }


        }
    }

    /**
     * @param player
     * @param breakerID
     */
    private void sendBreakerItemCommands(Player player, String breakerID) {
        player.sendMessage("§b#------------§6PickupSpawners§b------------#\n\n§cBreaker item commands:\n ");
        player.spigot().sendMessage(getHoverSuggest("§e [*] §6Edit item §8§o(Click here)", "§7/pspawners §bupdateitem " + breakerID, "/pspawners updateitem " + breakerID));
        player.spigot().sendMessage(getHoverSuggest("§e [*] §3Set permission §8§o(Click here)", "§7/pspawners §bsetitempermission " + breakerID + " <permission>", "/pspawners setitempermission " + breakerID + " <permission>"));
        if (sett.getConfig().get("item." + breakerID + ".permission") != null) {
            player.spigot().sendMessage(getHoverSuggest("§c [-] Remove permission §8§o(Click here)", "§7/pspawners §bremoveitempermission " + breakerID, "/pspawners removeitempermission " + breakerID));
        }
        player.spigot().sendMessage(getHoverSuggest("§c [-] Remove item §8§o(Click here)", "§7/pspawners §bremoveitem " + breakerID, "/pspawners removeitem " + breakerID));

        player.sendMessage("\n\n§b#-------------------------------------#\n");
    }

    @EventHandler
    public void onOpJoin(PlayerJoinEvent e) {
        if (sett.getConfig().getBoolean("update-check")) {

            Player p = e.getPlayer();
            if (p.isOp()) {
                if (Updater.version == null) {
                    return;
                }
                if (!Updater.version.equalsIgnoreCase(plugin.getDescription().getVersion())) {
                    p.spigot().sendMessage(getLang(p).equalsIgnoreCase("hu") ? getHoverClick(
                            "§6[PickupSpawners] §7Elérhető egy frissítés a pluginhoz. \n§6[PickupSpawners] §7Jelenlegi §cv"
                                    + plugin.getDescription().getVersion() + "§7, legfrissebb §av" + Updater.version,
                            ChatColor.GREEN + Updater.SPIGOT_DOWNLOAD + Updater.SPIGOT_DOWNLOAD_VERSION
                                    + Updater.downloadID,
                            Updater.SPIGOT_DOWNLOAD + Updater.SPIGOT_DOWNLOAD_VERSION + Updater.downloadID)
                            : getHoverClick(
                            "§6[PickupSpawners] §7There is a new update available. \n§6[PickupSpawners] §7Running §cv"
                                    + plugin.getDescription().getVersion() + "§7, latest §av" + Updater.version,
                            ChatColor.GREEN + Updater.SPIGOT_DOWNLOAD + Updater.SPIGOT_DOWNLOAD_VERSION
                                    + Updater.downloadID,
                            Updater.SPIGOT_DOWNLOAD + Updater.SPIGOT_DOWNLOAD_VERSION + Updater.downloadID));
                }
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        ItemStack stack = event.getItemInHand();
        String lang = getLang(event.getPlayer());
        if (block.getState() instanceof CreatureSpawner && stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();

            if (meta.getDisplayName().contains("§7Spawner")) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                String name = meta.getDisplayName();

                if (!name.isEmpty()) {
                    String spawnerName = ChatColor.stripColor(name.toUpperCase());
                    spawnerName = spawnerName.replaceAll("SPAWNER", "");
                    spawnerName = spawnerName.replaceAll(" ", "");
                    try {
                        EntityType.valueOf(spawnerName.toUpperCase());
                    } catch (Exception e) {

                        return;

                    }

                    String type = spawnerName;
                    if (EntityType.values().toString().toLowerCase().contains(type.toLowerCase())) {

                    } else {
                        type = "PIG";
                    }
                    Object ev = new SpawnerPlaceEvent(event.getPlayer(), type);
                    Bukkit.getPluginManager().callEvent((Event) ev);

                    if (((SpawnerPlaceEvent) ev).isCancelled()) {
                        return;
                    }

                    // spawner.setCreatureTypeByName(spawnerName);
//                    try {
                        spawner.setSpawnedType(EntityType.valueOf(spawnerName));
                        spawner.update(true, true);

                        event.getPlayer().sendMessage(Language.getReplacedLocale(event.getPlayer(), LocalePath.PLACE,
                                "%type%", spawnerName.toLowerCase()));
                        // event.getPlayer().sendMessage(lang.equals("hu")? "§7Letettél egy §e" +
                        // spawnerName.toLowerCase() + " §7spawnert!" : "§7You have placed one §e" +
                        // spawnerName.toLowerCase() + "§7 spawner.");
//                    } catch (IllegalArgumentException e) {
//                        spawner.setSpawnedType(EntityType.valueOf("PIG"));
//                        spawner.update(true, true);
//
//                        event.getPlayer().sendMessage(
//                                Language.getReplacedLocale(event.getPlayer(), LocalePath.PLACE, "%type%", "pig"));
//                        // event.getPlayer().sendMessage(lang.equals("hu") ? "§7Letettél egy §epig
//                        // §7spawnert!": "§7You have placed one §epig §7spawner.");
//                    }

                    return;
                }
            }
//            if (meta.getDisplayName().contains(
//                    ((CreatureSpawner) block.getState()).getSpawnedType().name().toLowerCase() + " Spawner")) {
//                CreatureSpawner spawner = (CreatureSpawner) block.getState();
//
//                String name = meta.getDisplayName();
//                if (!name.isEmpty()) {
//                    String spawnerName = ChatColor.stripColor(name.toUpperCase());
//                    spawnerName = spawnerName.replaceAll("SPAWNER", "");
//                    spawnerName = spawnerName.replaceAll(" ", "");
//                    try {
//                        EntityType.valueOf(spawnerName.toUpperCase());
//                    } catch (Exception e) {
//
//                        return;
//
//                    }
//                    spawner.setSpawnedType(EntityType.valueOf(spawnerName));
//                    spawner.update(true, true);
//                    event.getPlayer().sendMessage(
//                            lang.equals("hu") ? "§7Letettél egy §e" + spawnerName.toLowerCase() + " §7spawnert!"
//                                    : "§7You have placed one §e" + spawnerName.toLowerCase() + "§7 spawner.");
//                    return;
//                }
//            }

        }
    }

}
