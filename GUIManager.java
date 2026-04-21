package com.example.playertags.managers;

import com.example.playertags.PlayerTags;
import com.example.playertags.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the tag selection GUI with pagination
 */
public class GUIManager implements Listener {
    
    private final PlayerTags plugin;
    private final Map<UUID, Integer> playerPages; // Player UUID -> Current page
    
    public GUIManager(PlayerTags plugin) {
        this.plugin = plugin;
        this.playerPages = new ConcurrentHashMap<>();
    }
    
    /**
     * Open the tag selection GUI for a player
     */
    public void openTagMenu(Player player) {
        openTagMenu(player, 0);
    }
    
    /**
     * Open the tag selection GUI at a specific page
     */
    public void openTagMenu(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        
        String title = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getGuiTitle() + " &7(Page " + (page + 1) + ")");
        int size = plugin.getConfigManager().getGuiSize();
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Get all tags
        List<Tag> allTags = plugin.getConfigManager().getSortedTags();
        
        // Calculate pagination
        int tagsPerPage = size - 9; // Reserve bottom row for navigation
        int startIndex = page * tagsPerPage;
        int endIndex = Math.min(startIndex + tagsPerPage, allTags.size());
        
        // Add tags to GUI
        for (int i = startIndex; i < endIndex; i++) {
            Tag tag = allTags.get(i);
            boolean hasPermission = plugin.getTagManager().hasPermissionForTag(player, tag);
            ItemStack item = tag.createItem(hasPermission);
            
            // If tag is currently selected, add enchantment effect
            Tag currentTag = plugin.getTagManager().getPlayerTag(player);
            if (currentTag != null && currentTag.getId().equals(tag.getId())) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.add("");
                    lore.add(ChatColor.GREEN + "✓ Currently Selected");
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }
            
            gui.setItem(i - startIndex, item);
        }
        
        // Add navigation items
        addNavigationItems(gui, page, allTags.size(), tagsPerPage);
        
        player.openInventory(gui);
    }
    
    /**
     * Add navigation buttons to the GUI
     */
    private void addNavigationItems(Inventory gui, int page, int totalTags, int tagsPerPage) {
        int size = gui.getSize();
        int maxPages = (int) Math.ceil((double) totalTags / tagsPerPage);
        
        // Previous page button
        if (page > 0) {
            ItemStack prevItem = new ItemStack(plugin.getConfigManager().getPrevPageItem());
            ItemMeta meta = prevItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getPrevPageName()));
            prevItem.setItemMeta(meta);
            gui.setItem(size - 9, prevItem);
        }
        
        // Next page button
        if (page < maxPages - 1) {
            ItemStack nextItem = new ItemStack(plugin.getConfigManager().getNextPageItem());
            ItemMeta meta = nextItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getNextPageName()));
            nextItem.setItemMeta(meta);
            gui.setItem(size - 7, nextItem);
        }
        
        // Remove tag button
        Tag currentTag = plugin.getTagManager().getPlayerTag(gui.getViewers().stream()
                .findFirst().map(h -> (Player) h).orElse(null));
        if (currentTag != null) {
            ItemStack removeItem = new ItemStack(plugin.getConfigManager().getRemoveTagItem());
            ItemMeta meta = removeItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getRemoveTagName()));
            removeItem.setItemMeta(meta);
            gui.setItem(size - 5, removeItem);
        }
        
        // Close button
        ItemStack closeItem = new ItemStack(plugin.getConfigManager().getCloseItem());
        ItemMeta meta = closeItem.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getCloseName()));
        closeItem.setItemMeta(meta);
        gui.setItem(size - 1, closeItem);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getGuiTitle());
        
        if (!event.getView().getTitle().startsWith(title)) return;
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        int size = event.getInventory().getSize();
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        // Handle navigation
        if (slot == size - 9 && event.getCurrentItem().getType() == plugin.getConfigManager().getPrevPageItem()) {
            // Previous page
            openTagMenu(player, currentPage - 1);
            return;
        }
        
        if (slot == size - 7 && event.getCurrentItem().getType() == plugin.getConfigManager().getNextPageItem()) {
            // Next page
            openTagMenu(player, currentPage + 1);
            return;
        }
        
        if (slot == size - 5 && event.getCurrentItem().getType() == plugin.getConfigManager().getRemoveTagItem()) {
            // Remove tag
            handleTagRemoval(player);
            return;
        }
        
        if (slot == size - 1 && event.getCurrentItem().getType() == plugin.getConfigManager().getCloseItem()) {
            // Close inventory
            player.closeInventory();
            return;
        }
        
        // Handle tag selection
        handleTagSelection(player, slot, currentPage);
    }
    
    /**
     * Handle tag selection logic
     */
    private void handleTagSelection(Player player, int slot, int page) {
        List<Tag> allTags = plugin.getConfigManager().getSortedTags();
        int tagsPerPage = plugin.getConfigManager().getGuiSize() - 9;
        int tagIndex = page * tagsPerPage + slot;
        
        if (tagIndex >= allTags.size()) return;
        
        Tag selectedTag = allTags.get(tagIndex);
        
        // Check permission
        if (!plugin.getTagManager().hasPermissionForTag(player, selectedTag)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this tag!");
            player.playSound(player.getLocation(), plugin.getConfigManager().getErrorSound(), 1.0f, 1.0f);
            return;
        }
        
        // Check cooldown
        if (plugin.getTagManager().isOnCooldown(player)) {
            long remaining = plugin.getTagManager().getCooldownRemaining(player);
            player.sendMessage(ChatColor.RED + "Please wait " + remaining + " seconds before changing tags again!");
            player.playSound(player.getLocation(), plugin.getConfigManager().getErrorSound(), 1.0f, 1.0f);
            return;
        }
        
        // Apply the tag
        plugin.getTagManager().savePlayerTag(player.getUniqueId(), selectedTag.getId());
        plugin.getTagManager().applyTag(player, selectedTag);
        plugin.getTagManager().setCooldown(player);
        
        // Send success message and sound
        player.sendMessage(ChatColor.GREEN + "You have selected the " + 
                selectedTag.getDisplayName() + ChatColor.GREEN + " tag!");
        player.playSound(player.getLocation(), plugin.getConfigManager().getSelectSound(), 1.0f, 1.0f);
        
        // Refresh GUI
        player.closeInventory();
        openTagMenu(player, page);
    }
    
    /**
     * Handle tag removal
     */
    private void handleTagRemoval(Player player) {
        Tag currentTag = plugin.getTagManager().getPlayerTag(player);
        
        if (currentTag == null) {
            player.sendMessage(ChatColor.RED + "You don't have any tag selected!");
            player.playSound(player.getLocation(), plugin.getConfigManager().getErrorSound(), 1.0f, 1.0f);
            return;
        }
        
        plugin.getTagManager().removePlayerTag(player.getUniqueId());
        plugin.getTagManager().removeTag(player);
        
        player.sendMessage(ChatColor.GREEN + "Your tag has been removed!");
        player.playSound(player.getLocation(), plugin.getConfigManager().getSelectSound(), 1.0f, 1.0f);
        
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getGuiTitle());
        
        if (event.getView().getTitle().startsWith(title)) {
            event.setCancelled(true);
        }
    }
}