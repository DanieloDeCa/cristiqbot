package com.example.cristiqbot.managers;

import com.example.cristiqbot.AiClient;
import com.example.cristiqbot.CristiqBot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.chunk.Chunk;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BaseSearchManager {
    private final CristiqBot plugin;
    private final AiClient aiClient;
    private final int scanRadius;
    private final int maxResults;
    private final Set<Material> highlightBlocks;
    private final int cooldownSeconds;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public BaseSearchManager(CristiqBot plugin) {
        this.plugin = plugin;
        this.aiClient = new AiClient(plugin);
        var config = plugin.getPluginConfig();
        this.scanRadius = config.getInt("search.scan_radius", 500);
        this.maxResults = config.getInt("search.max_results", 3);
        this.cooldownSeconds = config.getInt("search.cooldown_seconds", 30);
        this.highlightBlocks = config.getStringList("search.highlight_blocks").stream()
                .map(Material::valueOf).collect(Collectors.toSet());
    }

    public void searchBase(Player player, String description) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(uuid, 0L) > now - (cooldownSeconds * 1000L)) {
            player.sendMessage("CristiqBot: On cooldown. Try again in " + cooldownSeconds + "s.");
            return;
        }
        cooldowns.put(uuid, now);

        player.sendMessage("CristiqBot: Interpreting your description...");

        // Step 1: AI interprets description for hints (e.g., "suggest coords/biome")
        String aiPrompt = "Analyze this Minecraft base description and suggest search hints like biome, materials, or coord offsets: " + description;
        aiClient.generateResponse(aiPrompt, "analytical", player.getWorld().getKey().getKey())
                .thenAccept(hints -> {
                    player.sendMessage("CristiqBot: " + hints + " Scanning nearby...");
                    // Step 2: Async scan
                    performScan(player, description, hints);
                });
    }

    private void performScan(Player player, String description, String hints) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        CompletableFuture<List<Location>> scanFuture = CompletableFuture.supplyAsync(() -> {
            List<Location> matches = new ArrayList<>();
            int centerX = loc.getBlockX();
            int centerZ = loc.getBlockZ();
            int radiusSq = scanRadius * scanRadius;

            // Spiral or grid scan chunks
            for (int dx = -scanRadius / 16; dx <= scanRadius / 16; dx++) {
                for (int dz = -scanRadius / 16; dz <= scanRadius / 16; dz++) {
                    int chunkX = (centerX >> 4) + dx;
                    int chunkZ = (centerZ >> 4) + dz;
                    Chunk chunk = world.getChunkAtAsync(chunkX, chunkZ).join(); // Async load
                    if (chunk == null) continue;

                    // Count highlight blocks in chunk
                    int matchCount = 0;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (highlightBlocks.contains(block.getType())) {
                                    matchCount++;
                                    if (matchCount > 5) break; // Threshold for "base"
                                }
                            }
                        }
                    }
                    if (matchCount > 5 && distanceSq(centerX, centerZ, chunkX * 16, chunkZ * 16) <= radiusSq) {
                        matches.add(new Location(world, chunkX * 16 + 8, loc.getY(), chunkZ * 16 + 8));
                    }
                }
            }
            return matches.stream()
                    .sorted(Comparator.comparingDouble(m -> m.distanceSquared(loc)))
                    .limit(maxResults)
                    .collect(Collectors.toList());
        });

        scanFuture.thenAccept(matches -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (matches.isEmpty()) {
                    player.sendMessage("CristiqBot: No bases found matching '" + description + "'. Try a wider radius!");
                } else {
                    for (int i = 0; i < matches.size(); i++) {
                        Location match = matches.get(i);
                        String msg = "CristiqBot: I found something matching your description near X: " +
                                match.getBlockX() + " Y: " + match.getBlockY() + " Z: " + match.getBlockZ() +
                                " [Click to Track]";
                        // For clickable: Use /tp or adventure mode, but here just send coords
                        player.sendMessage(msg);
                    }
                }
            });
        });
    }

    private double distanceSq(int x1, int z1, int x2, int z2) {
        int dx = x1 - x2, dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    public void shutdown() {
        cooldowns.clear();
    }
}
