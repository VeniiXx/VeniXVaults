package me.venixvaults.core.util;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class StartupBanner {
    private StartupBanner() {
    }

    public static void print(JavaPlugin plugin) {
        Logger logger = plugin.getLogger();
        String version = plugin.getDescription().getVersion();

        String[] lines = {
            "",
            "&#18F29A __      __ _____                    __   __",
            "&#22EFA0 \\ \\    / /|  __ \\                   \\ \\ / /",
            "&#2CECA6  \\ \\  / / | |  | | ___ _ __   ___   \\ V / ",
            "&#36E9AC   \\ \\/ /  | |  | |/ _ \\ '_ \\ / _ \\   > <  ",
            "&#40E6B2    \\  /   | |__| |  __/ |_) | (_) | / . \\ ",
            "&#4AE3B8     \\/    |_____/ \\___| .__/ \\___/ /_/ \\_\\",
            "&#7CFFCB                        | |                ",
            "&#7CFFCB                        |_|                ",
            "&#18F29A--------------------------------------------------",
            "&#7CFFCBVeniXVaults &8| &#B8FFD9Version: &f" + version,
            "&#7CFFCBVeniXVaults &8| &#B8FFD9Proje sahibi: &fVeniX",
            "&#7CFFCBVeniXVaults &8| &#B8FFD9Paper 1.21.x - 1.26.x depo sistemi",
            "&#18F29A--------------------------------------------------",
            ""
        };

        try {
            for (String line : lines) {
                Bukkit.getConsoleSender().sendMessage(Text.color(line));
            }
        } catch (Throwable ignored) {
            logger.info("VeniXVaults | Version: " + version + " | Proje sahibi: VeniX");
        }
    }
}
