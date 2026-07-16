package me.venixvaults.core.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class SelectorHolder implements InventoryHolder {
    private final int visibleVaults;
    private final int infoSlot;
    private final int closeSlot;

    public SelectorHolder(int visibleVaults, int infoSlot, int closeSlot) {
        this.visibleVaults = visibleVaults;
        this.infoSlot = infoSlot;
        this.closeSlot = closeSlot;
    }

    public int visibleVaults() {
        return visibleVaults;
    }

    public int infoSlot() {
        return infoSlot;
    }

    public int closeSlot() {
        return closeSlot;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
