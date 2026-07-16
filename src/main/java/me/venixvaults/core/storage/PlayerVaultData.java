package me.venixvaults.core.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public final class PlayerVaultData {
    private final UUID uuid;
    private String lastName;
    private final Map<Integer, ItemStack[]> vaults = new HashMap<>();

    public PlayerVaultData(UUID uuid, String lastName) {
        this.uuid = uuid;
        this.lastName = lastName;
    }

    public UUID uuid() {
        return uuid;
    }

    public String lastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
    }

    public ItemStack[] getVault(int number, int size) {
        ItemStack[] stored = vaults.get(number);
        if (stored == null) {
            return new ItemStack[size];
        }

        ItemStack[] resized = new ItemStack[size];
        System.arraycopy(stored, 0, resized, 0, Math.min(stored.length, resized.length));
        return resized;
    }

    public void setVault(int number, ItemStack[] contents) {
        vaults.put(number, contents);
    }

    public Map<Integer, ItemStack[]> vaults() {
        return vaults;
    }
}
