package me.poma123.spawners;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.poma123.spawners.SettingsManager.SettingKey;

public class LimitManager {
	private static final String limitPermissionPrefix = "pickupspawners.breaklimit.";
	
	private final PickupSpawners pickupSpawners;
	private final File dailyLimitsFile;
	
	private int breakedSpawners = 0;
	private int dayOfYear = dayOfYear();
	private final Map<String, Integer> playerBreaks = new ConcurrentHashMap<>();
	
	public LimitManager(PickupSpawners pickupSpawners) {
		this.pickupSpawners = pickupSpawners;
		
		dailyLimitsFile = new File(pickupSpawners.getDataFolder() + File.separator + "daily_limits.yml");
		
		loadDailyLimitsFile();
	}
	
	public int getLimit(Player player) {
		SettingsManager sm = PickupSpawners.getInstance().getSettingsManager();

        return sm.getKeys(SettingKey.BREAK_LIMITS).stream()
        	.filter(limitKey -> player.hasPermission(limitPermissionPrefix + limitKey))
        	.mapToInt(limitKey -> sm.getIntValue(SettingKey.BREAK_LIMITS, limitKey)).max().getAsInt();
	}
	
	public int getCurrentBreaks(String playerName) {
		int currentDayOfYear = dayOfYear();
        if (dayOfYear != currentDayOfYear) {
        	dayOfYear = currentDayOfYear;
        	playerBreaks.clear();
        	resetDailyLimitsFile();
        	breakedSpawners = 0;
        }
        
        return playerBreaks.getOrDefault(playerName, 0);
	}
	
	public void incrementBreaks(String playerName) {
		int value = getCurrentBreaks(playerName) + 1;
		playerBreaks.put(playerName, value);
		writeToDailyLimitsFile(playerName, value);
		breakedSpawners++;
	}
	
	private void writeToDailyLimitsFile(String playerName, int value) {
		pickupSpawners.getServer().getScheduler().runTaskAsynchronously(pickupSpawners, () -> {
			FileConfiguration conf = YamlConfiguration.loadConfiguration(dailyLimitsFile);
			conf.set("players." + playerName, value);
			
			try {
                conf.save(dailyLimitsFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
		});
	}
    
    private void resetDailyLimitsFile() {
    	pickupSpawners.getServer().getScheduler().runTaskAsynchronously(pickupSpawners, () -> {
    		FileConfiguration conf = YamlConfiguration.loadConfiguration(dailyLimitsFile);
            conf.set("date", getTimestamp());
            conf.set("players", null);
            
            try {
                conf.save(dailyLimitsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
    	});
    }
    
    private void loadDailyLimitsFile() {
    	pickupSpawners.getServer().getScheduler().runTaskAsynchronously(pickupSpawners, () -> {
	    	prepareDailyLimitsFile();
	    	
	    	FileConfiguration conf = YamlConfiguration.loadConfiguration(dailyLimitsFile);
	    	if (!conf.getString("date", "default").equals(getTimestamp())) {
	    		resetDailyLimitsFile();
	    		return;
	    	}
	    	
	    	ConfigurationSection playersSection = conf.getConfigurationSection("players");
	    	if (playersSection != null) {
		    	for (String playerName : playersSection.getKeys(false)) {
					playerBreaks.put(playerName, conf.getInt("players." + playerName));
				}
	    	}
    	});
    }
    
    private void prepareDailyLimitsFile() {
    	if (!dailyLimitsFile.exists()) {
            try {
            	dailyLimitsFile.createNewFile();
                FileConfiguration conf = YamlConfiguration.loadConfiguration(dailyLimitsFile);
                conf.set("date", getTimestamp());
                conf.save(dailyLimitsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String getTimestamp() {
    	LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        return year + "_" + month + "_" + day;
    }
    
    private int dayOfYear() {
    	Calendar calendar = Calendar.getInstance();
    	return calendar.get(Calendar.DAY_OF_YEAR); 
    }
    
    public int getBreakedSpawners() {
    	return breakedSpawners;
    }
}
