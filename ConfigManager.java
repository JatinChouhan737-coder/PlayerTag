package com.example.playertags.managers;

import com.example.playertags.PlayerTags;
import com.example.playertags.models.Tag;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages plugin configuration and settings
 */
public class ConfigManager {
    
    private final PlayerTags plugin;
    private FileConfiguration config;
    
    // Settings
    private int cooldownSeconds;
    private String guiTitle;
    private int guiSize;
    private Sound selectSound;
    private Sound errorSound;
    private boolean usePlaceholderAPI;
    private String defaultTagId;
    
    // Navigation items
    private Material nextPageItem;
    private String nextPageName;
    private Material prevPageItem;
    private String prevPageName;
    private Material closeItem;
    private String closeName;
    private Material removeTagItem;
    private String removeTagName;
    
    public ConfigManager(PlayerTags plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadSettings();
    }
    
    /**
     * Load all configuration settings
     */
    private void loadSettings() {
        plugin.saveDefaultConfig();
        
        // Load cooldown settings
        cooldownSeconds = config.getInt("settings.cooldown-seconds", 60);
        
        // Load GUI settings
        guiTitle = config.getString("settings.gui-title", "&8Select Your Tag");
        guiSize = config.getInt("settings.gui-size", 54);
        
        // Load sound settings
        String selectSoundName = config.getString("settings.sounds.select", "ENTITY_PLAYER_LEVELUP");
        String errorSoundName = config.getString("settings.sounds.error", "ENTITY_VILLAGER_NO");
        
        try {
            selectSound = Sound.valueOf(selectSoundName);
            errorSound = Sound.valueOf(errorSoundName);
        } catch (IllegalArgumentException e) {
            selectSound = Sound.ENTITY_PLAYER_LEVELUP;
            errorSound = Sound.ENTITY_VILLAGER_NO;
        }
        
        // Load placeholder settings
        usePlaceholderAPI = config.getBoolean("settings.use-placeholderapi", true);
        defaultTagId = config.getString("settings.default-tag", "");
        
        // Load navigation items
        nextPageItem = Material.valueOf(config.getString("gui-items.next-page.material", "ARROW"));
        nextPageName = config.getString("gui-items.next-page.name", "&aNext Page →");
        prevPageItem = Material.valueOf(config.getString("gui-items.prev-page.material", "ARROW"));
        prevPageName = config.getString("gui-items.prev-page.name", "&a← Previous Page");
        closeItem = Material.valueOf(config.getString("gui-items.close.material", "BARRIER"));
        closeName = config.getString("gui-items.close.name", "&cClose");
        removeTagItem = Material.valueOf(config.getString("gui-items.remove-tag.material", "REDSTONE"));
        removeTagName = config.getString("gui-items.remove-tag.name", "&cRemove Tag");
    }
    
    /**
     * Load all tags from configuration
     */
    public Map<String, Tag> loadTags() {
        Map<String, Tag> tags = new LinkedHashMap<>();
        ConfigurationSection tagsSection = config.getConfigurationSection("tags");
        
        if (tagsSection == null) {
            plugin.getLogger().warning("No tags section found in config.yml!");
            return tags;
        }
        
        for (String key : tagsSection.getKeys(false)) {
            ConfigurationSection tagSection = tagsSection.getConfigurationSection(key);
            if (tagSection == null) continue;
            
            String displayName = tagSection.getString("display-name", key);
            String prefix = tagSection.getString("prefix", "");
            String suffix = tagSection.getString("suffix", "");
            String permission = tagSection.getString("permission", "playertags.tag." + key.toLowerCase());
            
            Material icon;
            try {
                icon = Material.valueOf(tagSection.getString("icon", "NAME_TAG"));
            } catch (IllegalArgumentException e) {
                icon = Material.NAME_TAG;
            }
            
            int slot = tagSection.getInt("slot", -1);
            List<String> description = tagSection.getStringList("description");
            boolean glowing = tagSection.getBoolean("glowing", false);
            
            Tag tag = new Tag(key, displayName, prefix, suffix, permission, icon, slot, description, glowing);
            tags.put(key, tag);
        }
        
        return tags;
    }
    
    /**
     * Get all tags sorted by slot position
     */
    public List<Tag> getSortedTags() {
        Map<String, Tag> tags = loadTags();
        List<Tag> sortedTags = new ArrayList<>(tags.values());
        
        sortedTags.sort((t1, t2) -> {
            if (t1.getSlot() == -1 && t2.getSlot() == -1) return 0;
            if (t1.getSlot() == -1) return 1;
            if (t2.getSlot() == -1) return -1;
            return Integer.compare(t1.getSlot(), t2.getSlot());
        });
        
        return sortedTags;
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadSettings();
    }
    
    // Getters
    public int getCooldownSeconds() { return cooldownSeconds; }
    public String getGuiTitle() { return guiTitle; }
    public int getGuiSize() { return guiSize; }
    public Sound getSelectSound() { return selectSound; }
    public Sound getErrorSound() { return errorSound; }
    public boolean usePlaceholderAPI() { return usePlaceholderAPI; }
    public String getDefaultTagId() { return defaultTagId; }
    public Material getNextPageItem() { return nextPageItem; }
    public String getNextPageName() { return nextPageName; }
    public Material getPrevPageItem() { return prevPageItem; }
    public String getPrevPageName() { return prevPageName; }
    public Material getCloseItem() { return closeItem; }
    public String getCloseName() { return closeName; }
    public Material getRemoveTagItem() { return removeTagItem; }
    public String getRemoveTagName() { return removeTagName; }
}