package com.example.playertags.models;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tag with all its properties
 */
public class Tag {
    
    private final String id;
    private final String displayName;
    private final String prefix;
    private final String suffix;
    private final String permission;
    private final Material icon;
    private final int slot;
    private final List<String> description;
    private final boolean glowing;
    
    public Tag(String id, String displayName, String prefix, String suffix, 
               String permission, Material icon, int slot, List<String> description, boolean glowing) {
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.permission = permission;
        this.icon = icon;
        this.slot = slot;
        this.description = description != null ? description : new ArrayList<>();
        this.glowing = glowing;
    }
    
    /**
     * Creates an ItemStack representation of this tag for GUI
     */
    public ItemStack createItem(boolean hasPermission) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        String nameColor = hasPermission ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
        meta.setDisplayName(nameColor + ChatColor.translateAlternateColorCodes('&', displayName));
        
        // Build lore
        List<String> lore = new ArrayList<>();
        
        // Add description
        for (String line : description) {
            lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
        }
        
        lore.add("");
        
        // Show prefix/suffix preview
        lore.add(ChatColor.YELLOW + "Preview:");
        lore.add(ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', 
                prefix + "Player" + suffix));
        
        lore.add("");
        
        // Show permission status
        if (hasPermission) {
            lore.add(ChatColor.GREEN + "✓ You have permission");
            lore.add(ChatColor.GRAY + "Click to select this tag!");
        } else {
            lore.add(ChatColor.RED + "✗ No permission");
            lore.add(ChatColor.GRAY + "Required: " + permission);
        }
        
        meta.setLore(lore);
        
        // Add glow effect if enabled and player has permission
        if (glowing && hasPermission) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return ChatColor.translateAlternateColorCodes('&', displayName); }
    public String getPrefix() { return ChatColor.translateAlternateColorCodes('&', prefix); }
    public String getSuffix() { return ChatColor.translateAlternateColorCodes('&', suffix); }
    public String getPermission() { return permission; }
    public Material getIcon() { return icon; }
    public int getSlot() { return slot; }
    public List<String> getDescription() { return description; }
    public boolean isGlowing() { return glowing; }
}