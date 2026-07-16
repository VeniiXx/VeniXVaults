package me.venixvaults.core.command;

import me.venixvaults.core.VeniXVaultsPlugin;
import me.venixvaults.core.gui.GuiManager;
import me.venixvaults.core.storage.VaultManager;
import me.venixvaults.core.util.Messages;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class VeniXVaultsCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("help", "reload", "save", "backup", "open", "info", "debug");

    private final VeniXVaultsPlugin plugin;
    private final VaultManager vaultManager;
    private final GuiManager guiManager;
    private final Messages messages;

    public VeniXVaultsCommand(VeniXVaultsPlugin plugin, VaultManager vaultManager, GuiManager guiManager, Messages messages) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.guiManager = guiManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (!sender.hasPermission("venixvaults.admin")) {
            messages.send(sender, "messages.no-permission");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadVeniXVaults();
                messages.send(sender, "messages.reloaded");
            }
            case "save" -> {
                vaultManager.saveAllCached();
                messages.send(sender, "messages.saved");
            }
            case "backup" -> createBackup(sender);
            case "open" -> openOther(sender, args);
            case "info" -> info(sender, args);
            case "debug" -> debug(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void createBackup(CommandSender sender) {
        try {
            File backup = vaultManager.createBackup();
            messages.send(sender, "messages.backup-created", Map.of("file", backup.getName()));
        } catch (IOException exception) {
            messages.send(sender, "messages.backup-failed");
            exception.printStackTrace();
        }
    }

    private void openOther(CommandSender sender, String[] args) {
        if (!(sender instanceof Player viewer)) {
            messages.send(sender, "messages.only-player");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.prefix() + "/venixvaults open <oyuncu> <depo>");
            return;
        }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            messages.send(sender, "messages.player-not-found");
            return;
        }

        int vaultNumber;
        try {
            vaultNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            messages.send(sender, "messages.invalid-number");
            return;
        }

        if (vaultNumber < 1 || vaultNumber > vaultManager.maxVaults()) {
            messages.send(sender, "messages.invalid-number");
            return;
        }

        guiManager.openVault(viewer, target, vaultNumber, true);
        messages.send(sender, "messages.admin-opened", Map.of(
            "player", target.getName() == null ? args[1] : target.getName(),
            "vault", String.valueOf(vaultNumber)
        ));
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.prefix() + "/venixvaults info <oyuncu>");
            return;
        }

        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            messages.send(sender, "messages.player-not-found");
            return;
        }

        sender.sendMessage(messages.prefix() + "Oyuncu: §f" + (target.getName() == null ? args[1] : target.getName()));
        sender.sendMessage(messages.prefix() + "UUID: §f" + target.getUniqueId());
        sender.sendMessage(messages.prefix() + "Online: §f" + (target.isOnline() ? "Evet" : "Hayir"));
        if (target.getPlayer() != null) {
            Player player = target.getPlayer();
            sender.sendMessage(messages.prefix() + "Aktif depo: §f" + vaultManager.unlockedVaults(player));
            sender.sendMessage(messages.prefix() + "Depo boyutu: §f" + vaultManager.rows(player) + " satir");
        }
    }

    private void debug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.prefix() + "/venixvaults debug <oyuncu>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "messages.player-not-found");
            return;
        }

        sender.sendMessage(messages.prefix() + "Debug oyuncu: §f" + target.getName());
        sender.sendMessage(messages.prefix() + "Config default-vaults: §f" + vaultManager.defaultVaults());
        sender.sendMessage(messages.prefix() + "Pluginin gordugu aktif depo: §f" + vaultManager.unlockedVaults(target));
        sender.sendMessage(messages.prefix() + "Algilanan depo permleri: §f" + vaultManager.detectedVaultPermissions(target));
        sender.sendMessage(messages.prefix() + "Direkt kontrol venixvaults.vaults.2-6: §f"
            + target.hasPermission("venixvaults.vaults.2") + "/"
            + target.hasPermission("venixvaults.vaults.3") + "/"
            + target.hasPermission("venixvaults.vaults.4") + "/"
            + target.hasPermission("venixvaults.vaults.5") + "/"
            + target.hasPermission("venixvaults.vaults.6"));
    }

    private OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (!offline.hasPlayedBefore()) {
            return null;
        }
        return offline;
    }

    private void sendHelp(CommandSender sender) {
        for (String line : messages.list("messages.help")) {
            sender.sendMessage(line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("debug"))) {
            List<String> players = Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(name -> name != null && name.toLowerCase().startsWith(args[1].toLowerCase()))
                .limit(30)
                .toList();
            return players;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= vaultManager.maxVaults(); i++) {
                String value = String.valueOf(i);
                if (value.startsWith(args[2])) {
                    numbers.add(value);
                }
            }
            return numbers;
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }
}
