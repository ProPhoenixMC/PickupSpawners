package me.poma123.spawners;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SettingsManager {	
	FileConfiguration config;
	File cfile;
	
	public SettingsManager(PickupSpawners p) {
		p.saveDefaultConfig();
		
		config = p.getConfig();
		config.options().copyDefaults(true);
	
		cfile = new File(p.getDataFolder(), "config.yml");
		saveConfig();
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public void saveConfig() {
		try {
			config.save(cfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save config.yml!");
		}
	}
	
	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(cfile);
	}
	
	public String getStringValue(String path) {
		return config.getString(path);
	}
	
	public int getIntValue(String path) {
		return config.getInt(path);
	}
	
	public Set<String> getKeys(String path) {
		return config.getConfigurationSection(path).getKeys(false);
	}
	
	public String getStringValue(SettingKey key) {
		return config.getString(key.getValue());
	}
	
	public int getIntValue(SettingKey key) {
		return config.getInt(key.getValue());
	}
	
	public Set<String> getKeys(SettingKey key) {
		return config.getConfigurationSection(key.getValue()).getKeys(false);
	}
	
	public String getStringValue(SettingKey path, String key) {
		return config.getString(path.getValueAndSeparator() + key);
	}
	
	public int getIntValue(SettingKey path, String key) {
		return config.getInt(path.getValueAndSeparator() + key);
	}
	
	public enum SettingKey {
		BREAK_LIMITS("break-limits"),
		ITEM("item");
		
		private SettingKey(String value) {
			this.value = value;
		}
		
		private final String value;
		
		public String getValue() {
			return value;
		}
		
		public String getValueAndSeparator() {
			return value + ".";
		}
	}
}
