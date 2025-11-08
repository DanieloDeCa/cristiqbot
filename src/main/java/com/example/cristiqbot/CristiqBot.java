package com.example.cristiqbot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import com.example.cristiqbot.commands.CristiqCommand;
import com.example.cristiqbot.managers.AiChatManager;
import com.example.cristiqbot.managers.BaseSearchManager;

public class CristiqBot extends JavaPlugin {
    private static CristiqBot instance;
    private AiChatManager chatManager;
    private BaseSearchManager searchManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();

        // Initialize managers
        chatManager = new AiChatManager(this);
        searchManager = new BaseSearchManager(this);

        // Register command
        getCommand("cristiqbot").setExecutor(new CristiqCommand(chatManager, searchManager));

        getLogger().info("CristiqBot enabled! Ready to chat and search.");
    }

    @Override
    public void onDisable() {
        if (chatManager != null) chatManager.shutdown();
        if (searchManager != null) searchManager.shutdown();
        getLogger().info("CristiqBot disabled.");
    }

    public static CristiqBot getInstance() {
        return instance;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }
}
