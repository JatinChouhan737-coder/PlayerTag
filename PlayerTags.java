package com.example.playertags;

import com.example.playertags.commands.TagsCommand;
import com.example.playertags.listeners.ChatListener;
import com.example.playertags.listeners.PlayerListener;
import com.example.playertags.managers.ConfigManager;
import com.example.playertags.managers.GUIManager;
import com.example.playertags.managers.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for PlayerTags
 * Handles plugin lifecycle and manager initialization
 */
public class PlayerTags extends JavaPlugin {
    
    private static PlayerTags instance;
    private ConfigManager configManager;
    private TagManager tagManager;
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers in correct order
        this.configManager = new ConfigManager(this);
        this.tagManager = new TagManager(this);
        this.guiManager = new GUIManager(this);
        
        // Register commands and listeners
        getCommand("tags").setExecutor(new TagsCommand(this));
        getCommand("tags").setTabCompleter(new TagsCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        
        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found! Hooking into it...");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
        
        // Load saved player tags
        tagManager.loadPlayerTags();
        
        getLogger().info("PlayerTags v" + getDescription().getVersion() + " has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all player data before shutdown
        if (tagManager != null) {
            tagManager.saveAllPlayerTags();
        }
        
        getLogger().info("PlayerTags has been disabled!");
    }
    
    public static PlayerTags getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public TagManager getTagManager() {
        return tagManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public void reload() {
        reloadConfig();
        configManager.reloadConfig();
        tagManager.loadPlayerTags();
        getLogger().info("PlayerTags configuration reloaded!");
    }
}