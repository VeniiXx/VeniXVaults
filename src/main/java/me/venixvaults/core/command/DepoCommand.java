package me.venixvaults.core.command;

import me.venixvaults.core.gui.GuiManager;
import me.venixvaults.core.storage.VaultManager;
import me.venixvaults.core.util.Messages;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class DepoCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final VaultManager vaultManager;
    private final GuiManager guiManager;
    private final Messages messages;

    public DepoCommand(JavaPlugin plugin, VaultManager vaultManager, GuiManager guiManager, Messages messages) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.guiManager = guiManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "messages.only-player");
            return true;
        }
        if (!player.hasPermission("venixvaults.use")) {
            messages.send(player, "messages.no-permission");
            return true;
        }
        if (vaultManager.isDisabledWorld(player)) {
            messages.send(player, "messages.world-disabled");
            guiManager.play(player, "sounds.deny");
            return true;
        }

        if (args.length == 0) {
            guiManager.openSelector(player);
            return true;
        }

        int vaultNumber;
        try {
            vaultNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            messages.send(player, "messages.invalid-number");
            return true;
        }

        if (vaultManager.isLocked(player, vaultNumber)) {
            messages.send(player, "messages.vault-locked");
            guiManager.play(player, "sounds.deny");
            return true;
        }

        guiManager.openVault(player, player, vaultNumber, false);
        messages.send(player, "messages.vault-opened", Map.of("vault", String.valueOf(vaultNumber)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return List.of();
        }

        int unlocked = vaultManager.unlockedVaults(player);
        List<String> numbers = new ArrayList<>();
        for (int i = 1; i <= unlocked; i++) {
            String value = String.valueOf(i);
            if (value.startsWith(args[0])) {
                numbers.add(value);
            }
        }
        return numbers;
    }
}
