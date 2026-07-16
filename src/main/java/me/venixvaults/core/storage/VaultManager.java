package me.venixvaults.core.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultManager {
    private final JavaPlugin plugin;
    private final VaultStorage storage;
    private final Map<UUID, PlayerVaultData> cache = new HashMap<>();
    private final Set<String> blacklisted = new HashSet<>();
    private int defaultVaults;
    private int maxVaults;
    private int defaultRows;

    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storage = new VaultStorage(plugin);
        reloadSettings();
    }

    public void reloadSettings() {
        defaultVaults = clamp(plugin.getConfig().getInt("settings.default-vaults", 1), 1, 54);
        maxVaults = clamp(plugin.getConfig().getInt("settings.max-vaults", 54), 1, 54);
        defaultRows = clamp(plugin.getConfig().getInt("settings.default-rows", 6), 1, 6);

        blacklisted.clear();
        for (String material : plugin.getConfig().getStringList("items.blacklist")) {
            blacklisted.add(material.toUpperCase(Locale.ROOT));
        }
    }

    public PlayerVaultData data(Player player) {
        PlayerVaultData data = cache.computeIfAbsent(player.getUniqueId(), uuid -> storage.load(uuid, player.getName()));
        data.setLastName(player.getName());
        return data;
    }

    public PlayerVaultData data(OfflinePlayer player) {
        String name = player.getName() == null ? "Unknown" : player.getName();
        PlayerVaultData data = cache.computeIfAbsent(player.getUniqueId(), uuid -> storage.load(uuid, name));
        data.setLastName(name);
        return data;
    }

    public int unlockedVaults(Player player) {
        int amount = defaultVaults;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String permission = info.getPermission().toLowerCase(Locale.ROOT);
            if (!info.getValue()) {
                continue;
            }
            amount = Math.max(amount, parseTrailingNumber(permission, "venixvaults.vaults."));
            amount = Math.max(amount, parseTrailingNumber(permission, "venixvaults.depos."));
            amount = Math.max(amount, parseTrailingNumber(permission, "venixvaults.depo."));
        }
        return clamp(amount, 1, maxVaults);
    }

    public int rows(Player player) {
        int rows = defaultRows;
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String permission = info.getPermission().toLowerCase(Locale.ROOT);
            if (!info.getValue() || !permission.startsWith("venixvaults.size.")) {
                continue;
            }
            rows = Math.max(rows, parseTrailingNumber(permission, "venixvaults.size."));
        }
        return clamp(rows, 1, 6);
    }

    public boolean isLocked(Player player, int vault) {
        return vault < 1 || vault > unlockedVaults(player);
    }

    public boolean isDisabledWorld(Player player) {
        if (player.hasPermission("venixvaults.bypass.world")) {
            return false;
        }
        return plugin.getConfig().getStringList("settings.disabled-worlds").contains(player.getWorld().getName());
    }

    public boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        if (!plugin.getConfig().getBoolean("items.blacklist-enabled", true)) {
            return false;
        }
        Material type = item.getType();
        return blacklisted.contains(type.name());
    }

    public void saveInventory(UUID owner, String ownerName, int vaultNumber, Inventory inventory) {
        PlayerVaultData data = cache.computeIfAbsent(owner, uuid -> storage.load(uuid, ownerName));
        data.setLastName(ownerName);
        data.setVault(vaultNumber, inventory.getContents());
        storage.save(data);
    }

    public void saveAllCached() {
        for (PlayerVaultData data : cache.values()) {
            storage.save(data);
        }
    }

    public File createBackup() throws IOException {
        File source = storage.dataFolder();
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        Files.createDirectories(backupFolder.toPath());

        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
        File backup = new File(backupFolder, "VeniXVaults-backup-" + stamp + ".zip");

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(backup))) {
            if (source.exists()) {
                zipFolder(source, source, zip);
            }
        }
        return backup;
    }

    public int maxVaults() {
        return maxVaults;
    }

    public int defaultVaults() {
        return defaultVaults;
    }

    public Set<String> detectedVaultPermissions(Player player) {
        Set<String> permissions = new java.util.TreeSet<>();
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String permission = info.getPermission().toLowerCase(Locale.ROOT);
            if (info.getValue() && (permission.startsWith("venixvaults.vaults.")
                || permission.startsWith("venixvaults.depos.")
                || permission.startsWith("venixvaults.depo."))) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    private void zipFolder(File root, File current, ZipOutputStream zip) throws IOException {
        File[] files = current.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(root, file, zip);
                continue;
            }
            String relative = root.toPath().relativize(file.toPath()).toString().replace('\\', '/');
            zip.putNextEntry(new ZipEntry(relative));
            try (FileInputStream input = new FileInputStream(file)) {
                input.transferTo(zip);
            }
            zip.closeEntry();
        }
    }

    private int parseTrailingNumber(String permission, String prefix) {
        if (!permission.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(permission.substring(prefix.length()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
