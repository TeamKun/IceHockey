package net.kunmc.lab.icehockey;

import dev.kotx.flylib.command.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Game implements Listener {
    private final Config config;
    private final Plugin plugin;
    private Ball ball = null;
    private boolean isRunning = false;

    public Game(Config config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;

        config.ballFriction.onModify(x -> {
            if (ball != null) {
                ball.friction(x);
            }
        });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start(Player rider, CommandContext ctx) {
        if (config.center.value() == null) {
            ctx.fail("centerの座標が設定されていません.");
            return;
        }

        if (isRunning) {
            ctx.fail("すでに実行中です.");
            return;
        }
        isRunning = true;

        ball = new Ball(rider, config.center.value(), plugin);
        ball.friction(config.ballFriction.value());
    }

    public void stop(CommandContext ctx) {
        if (!isRunning) {
            ctx.fail("開始されていません.");
            return;
        }
        isRunning = false;

        ball.remove();
    }
}