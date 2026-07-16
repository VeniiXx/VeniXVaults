package me.venixvaults.core.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class VaultHolder implements InventoryHolder {
    private final UUID owner;
    private final String ownerName;
    private final int vaultNumber;

    public VaultHolder(UUID owner, String ownerName, int vaultNumber) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.vaultNumber = vaultNumber;
    }

    public UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    public int vaultNumber() {
        return vaultNumber;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
