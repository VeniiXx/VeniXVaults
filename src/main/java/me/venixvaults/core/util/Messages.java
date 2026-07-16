package me.venixvaults.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String language = plugin.getConfig().getString("settings.language", "tr").toLowerCase(Locale.ROOT);
        if (!language.equals("tr") && !language.equals("en")) {
            language = "tr";
        }

        String resourceName = "lang/" + language + ".yml";
        file = new File(plugin.getDataFolder(), resourceName);
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String prefix() {
        return Text.color(config.getString("prefix", "&aVeniXVaults &8» &7"));
    }

    public String get(String path) {
        String value = config.getString(path, "&cEksik mesaj: " + path);
        return Text.color(value.replace("{prefix}", prefix()));
    }

    public String get(String path, Map<String, String> placeholders) {
        String value = get(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return value;
    }

    public List<String> list(String path) {
        List<String> source = config.getStringList(path);
        if (source.isEmpty()) {
            return Collections.singletonList(Text.color("&cEksik mesaj listesi: " + path));
        }
        List<String> colored = new ArrayList<>();
        for (String line : source) {
            colored.add(Text.color(line.replace("{prefix}", prefix())));
        }
        return colored;
    }

    public List<String> list(String path, Map<String, String> placeholders) {
        List<String> lines = list(path);
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            String replaced = line;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            result.add(replaced);
        }
        return result;
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(get(path, placeholders));
    }
}
