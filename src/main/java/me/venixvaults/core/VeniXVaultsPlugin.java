package me.venixvaults.core;

import me.venixvaults.core.command.DepoCommand;
import me.venixvaults.core.command.VeniXVaultsCommand;
import me.venixvaults.core.gui.GuiListener;
import me.venixvaults.core.gui.GuiManager;
import me.venixvaults.core.storage.VaultManager;
import me.venixvaults.core.util.Messages;
import me.venixvaults.core.util.StartupBanner;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VeniXVaultsPlugin extends JavaPlugin {
    private Messages messages;
    private VaultManager vaultManager;
    private GuiManager guiManager;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("lang/tr.yml");
        saveResourceIfMissing("lang/en.yml");

        messages = new Messages(this);
        vaultManager = new VaultManager(this);
        guiManager = new GuiManager(this, vaultManager, messages);

        registerCommands();
        Bukkit.getPluginManager().registerEvents(new GuiListener(this, guiManager, vaultManager, messages), this);
        startAutoSaveTask();

        StartupBanner.print(this);
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (vaultManager != null) {
            vaultManager.saveAllCached();
        }
    }

    public void reloadVeniXVaults() {
        reloadConfig();
        messages.reload();
        vaultManager.reloadSettings();
        startAutoSaveTask();
    }

    private void registerCommands() {
        VeniXVaultsCommand adminCommand = new VeniXVaultsCommand(this, vaultManager, guiManager, messages);
        DepoCommand depoCommand = new DepoCommand(this, vaultManager, guiManager, messages);

        PluginCommand venixvaults = getCommand("venixvaults");
        if (venixvaults != null) {
            venixvaults.setExecutor(adminCommand);
            venixvaults.setTabCompleter(adminCommand);
        }

        PluginCommand depo = getCommand("depo");
        if (depo != null) {
            depo.setExecutor(depoCommand);
            depo.setTabCompleter(depoCommand);
        }
    }

    private void saveResourceIfMissing(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    private void startAutoSaveTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        int minutes = getConfig().getInt("settings.auto-save-minutes", 1);
        if (minutes <= 0) {
            return;
        }

        long ticks = minutes * 60L * 20L;
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(this, () -> vaultManager.saveAllCached(), ticks, ticks);
    }

    public Messages messages() {
        return messages;
    }
}
