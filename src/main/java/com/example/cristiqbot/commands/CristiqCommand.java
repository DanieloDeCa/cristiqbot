package com.example.cristiqbot.commands;

import com.example.cristiqbot.managers.AiChatManager;
import com.example.cristiqbot.managers.BaseSearchManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CristiqCommand implements CommandExecutor {
    private final AiChatManager chatManager;
    private final BaseSearchManager searchManager;

    public CristiqCommand(AiChatManager chatManager, BaseSearchManager searchManager) {
        this.chatManager = chatManager;
        this.searchManager = searchManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Usage: /cristiqbot <talkai|search> <args>");
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "talkai" -> handleTalkAi(player, args);
            case "search" -> handleSearch(player, args);
            default -> {
                player.sendMessage("Unknown subcommand. Use 'talkai' or 'search'.");
                yield false;
            }
        };
    }

    private boolean handleTalkAi(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /cristiqbot talkai <on|off>");
            return true;
        }
        boolean enable = args[1].equalsIgnoreCase("on");
        chatManager.toggleChat(player, enable);
        player.sendMessage(enable ? "CristiqBot chat enabled!" : "CristiqBot chat disabled.");
        return true;
    }

    private boolean handleSearch(Player player, String[] args) {
        if (args.length < 3 || !args[1].equals("base")) {
            player.sendMessage("Usage: /cristiqbot search base \"<description>\"");
            return true;
        }
        String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        searchManager.searchBase(player, description);
        return true;
    }
}
