package me.venixvaults.core.gui;

import me.venixvaults.core.storage.PlayerVaultData;
import me.venixvaults.core.storage.VaultManager;
import me.venixvaults.core.util.ItemFactory;
import me.venixvaults.core.util.Messages;
import me.venixvaults.core.util.Text;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuiManager {
    private final JavaPlugin plugin;
    private final VaultManager vaultManager;
    private final Messages messages;

    public GuiManager(JavaPlugin plugin, VaultManager vaultManager, Messages messages) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.messages = messages;
    }

    public void openSelector(Player player) {
        int size = normalizeSize(plugin.getConfig().getInt("gui.selector-size", 54));
        int unlocked = vaultManager.unlockedVaults(player);
        int rows = vaultManager.rows(player);
        int usableSlots = Math.max(0, size - 9);
        int configuredVisibleVaults = plugin.getConfig().getInt("gui.visible-vaults", unlocked + 1);
        int visibleVaults = Math.min(Math.min(vaultManager.maxVaults(), usableSlots), Math.max(unlocked + 1, configuredVisibleVaults));
        int infoSlot = Math.max(0, size - 6);
        int closeSlot = Math.max(0, size - 4);

        Inventory inventory = Bukkit.createInventory(
            new SelectorHolder(visibleVaults, infoSlot, closeSlot),
            size,
            Text.color(plugin.getConfig().getString("gui.selector-title", "&8VeniXVaults"))
        );

        if (plugin.getConfig().getBoolean("gui.show-filler", true)) {
            ItemStack filler = ItemFactory.item(
                ItemFactory.material(plugin.getConfig().getString("gui.filler-material"), Material.GRAY_STAINED_GLASS_PANE),
                " ",
                java.util.List.of()
            );
            for (int slot = 0; slot < size; slot++) {
                inventory.setItem(slot, filler);
            }
        }

        for (int vault = 1; vault <= visibleVaults; vault++) {
            Map<String, String> placeholders = placeholders(vault, rows, unlocked, vaultManager.maxVaults());
            boolean locked = vault > unlocked;
            Material material = ItemFactory.material(
                plugin.getConfig().getString(locked ? "gui.locked-material" : "gui.open-material"),
                locked ? Material.BARRIER : Material.ENDER_CHEST
            );
            String name = messages.get(locked ? "gui.locked-name" : "gui.vault-name", placeholders);
            java.util.List<String> lore = messages.list(locked ? "gui.locked-lore" : "gui.vault-lore", placeholders);
            inventory.setItem(vault - 1, ItemFactory.item(material, name, lore));
        }

        Map<String, String> placeholders = placeholders(0, rows, unlocked, vaultManager.maxVaults());
        inventory.setItem(infoSlot, ItemFactory.item(
            ItemFactory.material(plugin.getConfig().getString("gui.info-material"), Material.BOOK),
            messages.get("gui.info-name", placeholders),
            messages.list("gui.info-lore", placeholders)
        ));
        inventory.setItem(closeSlot, ItemFactory.item(
            ItemFactory.material(plugin.getConfig().getString("gui.close-material"), Material.BARRIER),
            messages.get("gui.close-name"),
            java.util.List.of()
        ));

        player.openInventory(inventory);
        play(player, "sounds.open");
    }

    public void openVault(Player viewer, OfflinePlayer owner, int vaultNumber, boolean adminView) {
        int rows = vaultManager.rows(viewer);
        int size = rows * 9;
        PlayerVaultData data = vaultManager.data(owner);
        String ownerName = owner.getName() == null ? "Unknown" : owner.getName();
        String title = Text.color(plugin.getConfig().getString("gui.vault-title", "&8VeniXVaults #{vault}")
            .replace("{vault}", String.valueOf(vaultNumber))
            .replace("{player}", ownerName));

        Inventory inventory = Bukkit.createInventory(new VaultHolder(owner.getUniqueId(), ownerName, vaultNumber), size, title);
        inventory.setContents(data.getVault(vaultNumber, size));
        viewer.openInventory(inventory);
        play(viewer, "sounds.open");

        if (adminView && plugin.getConfig().getBoolean("settings.audit-admin-open", true)) {
            plugin.getLogger().info(viewer.getName() + " admin olarak " + ownerName + " #" + vaultNumber + " deposunu acti.");
        }
    }

    public void play(Player player, String path) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }
        String soundName = plugin.getConfig().getString(path);
        if (soundName == null || soundName.isBlank()) {
            return;
        }
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.8f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private Map<String, String> placeholders(int vault, int rows, int unlocked, int max) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("vault", String.valueOf(vault));
        placeholders.put("rows", String.valueOf(rows));
        placeholders.put("unlocked", String.valueOf(unlocked));
        placeholders.put("max", String.valueOf(max));
        return placeholders;
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, Math.min(54, size));
        return (normalized / 9) * 9;
    }
}
