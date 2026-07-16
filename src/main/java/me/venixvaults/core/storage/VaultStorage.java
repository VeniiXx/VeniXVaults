package me.venixvaults.core.storage;

import me.venixvaults.core.util.ItemSerializer;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultStorage {
    private final JavaPlugin plugin;
    private final File dataFolder;

    public VaultStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Data klasoru olusturulamadi: " + dataFolder.getAbsolutePath());
        }
    }

    public PlayerVaultData load(UUID uuid, String lastName) {
        File file = file(uuid);
        PlayerVaultData data = new PlayerVaultData(uuid, lastName);
        if (!file.exists()) {
            return data;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.setLastName(yaml.getString("last-name", lastName));
        ConfigurationSection section = yaml.getConfigurationSection("vaults");
        if (section == null) {
            return data;
        }

        for (String key : section.getKeys(false)) {
            try {
                int number = Integer.parseInt(key);
                String encoded = section.getString(key + ".contents", "");
                if (!encoded.isBlank()) {
                    data.setVault(number, ItemSerializer.fromBase64(encoded));
                }
            } catch (Exception exception) {
                plugin.getLogger().warning("Depo okunamadi: " + uuid + " #" + key + " (" + exception.getMessage() + ")");
            }
        }
        return data;
    }

    public void save(PlayerVaultData data) {
        File file = file(data.uuid());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("uuid", data.uuid().toString());
        yaml.set("last-name", data.lastName());

        for (var entry : data.vaults().entrySet()) {
            try {
                yaml.set("vaults." + entry.getKey() + ".contents", ItemSerializer.toBase64(entry.getValue()));
            } catch (IOException exception) {
                plugin.getLogger().warning("Depo kaydedilemedi: " + data.uuid() + " #" + entry.getKey());
            }
        }

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Oyuncu depo dosyasi kaydedilemedi: " + file.getAbsolutePath());
            exception.printStackTrace();
        }
    }

    public File dataFolder() {
        return dataFolder;
    }

    private File file(UUID uuid) {
        return new File(dataFolder, uuid + ".yml");
    }
}
