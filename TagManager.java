package com.example.playertags.managers;

import com.example.playertags.PlayerTags;
import com.example.playertags.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player tags, cooldowns, and data persistence
 */
public class TagManager {
    
    private final PlayerTags plugin;
    private final Map<UUID, String> playerTags; // Player UUID -> Tag ID
    private final Map<UUID, Long> cooldowns; // Player UUID -> Last change timestamp
    private File playerDataFile;
    private FileConfiguration playerData;
    
    public TagManager(PlayerTags plugin) {
        this.plugin = plugin;
        this.playerTags = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        setupPlayerData();
    }
    
    /**
     * Setup player data file
     */
    private void setupPlayerData() {
        playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.getParentFile().mkdirs();
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }
    
    /**
     * Load all player tags from file
     */
    public void loadPlayerTags() {
        playerTags.clear();
        ConfigurationSection section = playerData.getConfigurationSection("players");
        if (section != null) {
            for (String uuidString : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String tagId = section.getString(uuidString);
                    if (tagId != null) {
                        playerTags.put(uuid, tagId);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidString);
                }
            }
        }
        plugin.getLogger().info("Loaded " + playerTags.size() + " player tags.");
    }
    
    /**
     * Save a player's tag selection
     */
    public void savePlayerTag(UUID uuid, String tagId) {
        playerTags.put(uuid, tagId);
        playerData.set("players." + uuid.toString(), tagId);
        savePlayerData();
    }
    
    /**
     * Save all player tags (called on plugin disable)
     */
    public void saveAllPlayerTags() {
        for (Map.Entry<UUID, String> entry : playerTags.entrySet()) {
            playerData.set("players." + entry.getKey().toString(), entry.getValue());
        }
        savePlayerData();
    }
    
    /**
     * Save player data to file
     */
    private void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data!");
            e.printStackTrace();
        }
    }
    
    /**
     * Remove a player's tag
     */
    public void removePlayerTag(UUID uuid) {
        playerTags.remove(uuid);
        playerData.set("players." + uuid.toString(), null);
        savePlayerData();
    }
    
    /**
     * Get a player's current tag
     */
    public Tag getPlayerTag(Player player) {
        String tagId = playerTags.get(player.getUniqueId());
        if (tagId != null) {
            Map<String, Tag> tags = plugin.getConfigManager().loadTags();
            return tags.get(tagId);
        }
        
        // Return default tag if configured
        String defaultTagId = plugin.getConfigManager().getDefaultTagId();
        if (!defaultTagId.isEmpty()) {
            Map<String, Tag> tags = plugin.getConfigManager().loadTags();
            Tag defaultTag = tags.get(defaultTagId);
            if (defaultTag != null && player.hasPermission(defaultTag.getPermission())) {
                return defaultTag;
            }
        }
        
        return null;
    }
    
    /**
     * Check if player is on cooldown
     */
    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("playertags.bypass.cooldown")) {
            return false;
        }
        
        Long lastChange = cooldowns.get(player.getUniqueId());
        if (lastChange == null) {
            return false;
        }
        
        long cooldownSeconds = plugin.getConfigManager().getCooldownSeconds();
        long timeSince = (System.currentTimeMillis() - lastChange) / 1000;
        
        return timeSince < cooldownSeconds;
    }
    
    /**
     * Get remaining cooldown time in seconds
     */
    public long getCooldownRemaining(Player player) {
        Long lastChange = cooldowns.get(player.getUniqueId());
        if (lastChange == null) {
            return 0;
        }
        
        long cooldownSeconds = plugin.getConfigManager().getCooldownSeconds();
        long timeSince = (System.currentTimeMillis() - lastChange) / 1000;
        long remaining = cooldownSeconds - timeSince;
        
        return Math.max(0, remaining);
    }
    
    /**
     * Set player cooldown
     */
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Check if player has permission for a tag
     */
    public boolean hasPermissionForTag(Player player, Tag tag) {
        if (tag.getPermission() == null || tag.getPermission().isEmpty()) {
            return true;
        }
        
        return player.hasPermission(tag.getPermission()) || 
               player.hasPermission("playertags.admin") ||
               player.isOp();
    }
    
    /**
     * Get all tags a player has permission for
     */
    public List<Tag> getAvailableTags(Player player) {
        List<Tag> allTags = plugin.getConfigManager().getSortedTags();
        List<Tag> availableTags = new ArrayList<>();
        
        for (Tag tag : allTags) {
            if (hasPermissionForTag(player, tag)) {
                availableTags.add(tag);
            }
        }
        
        return availableTags;
    }
    
    /**
     * Apply tag to player (updates nametag)
     */
    public void applyTag(Player player, Tag tag) {
        // Update player's display name with prefix/suffix
        String displayName = tag.getPrefix() + player.getName() + tag.getSuffix();
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
        
        // For 1.16+ - set custom name visible
        player.setCustomName(displayName);
        player.setCustomNameVisible(true);
    }
    
    /**
     * Remove tag from player
     */
    public void removeTag(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        player.setCustomName(null);
        player.setCustomNameVisible(false);
    }
}