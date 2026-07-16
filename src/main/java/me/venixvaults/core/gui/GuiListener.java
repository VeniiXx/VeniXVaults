package me.venixvaults.core.gui;

import me.venixvaults.core.storage.VaultManager;
import me.venixvaults.core.util.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuiListener implements Listener {
    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final VaultManager vaultManager;
    private final Messages messages;

    public GuiListener(JavaPlugin plugin, GuiManager guiManager, VaultManager vaultManager, Messages messages) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.vaultManager = vaultManager;
        this.messages = messages;
    }

    @EventHandler
    public void onSelectorClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof SelectorHolder holder)) {
            return;
        }

        event.setCancelled(true);
        int rawSlot = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();
        if (rawSlot < 0 || rawSlot >= top.getSize()) {
            return;
        }
        if (rawSlot == holder.closeSlot()) {
            player.closeInventory();
            return;
        }
        if (rawSlot == holder.infoSlot() || rawSlot >= holder.visibleVaults()) {
            return;
        }

        int vaultNumber = rawSlot + 1;
        if (vaultNumber > vaultManager.maxVaults()) {
            return;
        }
        if (vaultManager.isLocked(player, vaultNumber)) {
            messages.send(player, "messages.vault-locked");
            guiManager.play(player, "sounds.deny");
            return;
        }
        guiManager.openVault(player, player, vaultNumber, false);
        messages.send(player, "messages.vault-opened", java.util.Map.of("vault", String.valueOf(vaultNumber)));
    }

    @EventHandler
    public void onVaultClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof VaultHolder)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        boolean movingIntoVault = event.getRawSlot() < top.getSize()
            || event.isShiftClick()
            || event.getClick().isKeyboardClick();

        if (!movingIntoVault) {
            return;
        }

        ItemStack incoming = incomingItem(event, top);
        if (!player.hasPermission("venixvaults.bypass.blacklist") && vaultManager.isBlacklisted(incoming)) {
            event.setCancelled(true);
            messages.send(player, "messages.item-blacklisted");
            guiManager.play(player, "sounds.deny");
            return;
        }
        scheduleInstantSave(top);
    }

    @EventHandler
    public void onVaultDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof VaultHolder)) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        boolean targetsVault = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
        if (targetsVault && !player.hasPermission("venixvaults.bypass.blacklist") && vaultManager.isBlacklisted(event.getOldCursor())) {
            event.setCancelled(true);
            messages.send(player, "messages.item-blacklisted");
            guiManager.play(player, "sounds.deny");
            return;
        }
        if (targetsVault) {
            scheduleInstantSave(event.getView().getTopInventory());
        }
    }

    @EventHandler
    public void onVaultClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof VaultHolder holder)) {
            return;
        }
        vaultManager.saveInventory(holder.owner(), holder.ownerName(), holder.vaultNumber(), event.getInventory());
        if (event.getPlayer() instanceof Player player) {
            guiManager.play(player, "sounds.close");
        }
    }

    private ItemStack incomingItem(InventoryClickEvent event, Inventory top) {
        if (event.isShiftClick()) {
            return event.getCurrentItem();
        }
        if (event.getClick().isKeyboardClick()) {
            int button = event.getHotbarButton();
            if (button >= 0 && button < 9 && event.getWhoClicked().getInventory().getType() == InventoryType.PLAYER) {
                return event.getWhoClicked().getInventory().getItem(button);
            }
        }
        if (event.getRawSlot() < top.getSize()) {
            return event.getCursor();
        }
        return null;
    }

    private void scheduleInstantSave(Inventory inventory) {
        if (!(inventory.getHolder() instanceof VaultHolder holder)) {
            return;
        }
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
            vaultManager.saveInventory(holder.owner(), holder.ownerName(), holder.vaultNumber(), inventory)
        );
    }
}
