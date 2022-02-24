package net.kunmc.lab.icehockey;

import dev.kotx.flylib.command.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Game implements Listener {
    private final Config config;
    private final Plugin plugin;
    private final List<BukkitTask> taskList = new ArrayList<>();
    private final BossBar matchTimeBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
    private Ball ball = null;
    private boolean isRunning = false;
    private int blueScore = 0;
    private int redScore = 0;
    private int remainingSecs = 0;

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

    private boolean checkStartPreConditions(CommandContext ctx) {
        if (config.center.value() == null) {
            ctx.fail("centerの座標が設定されていません.");
            return false;
        }

        if (config.blueTeam.value() == null) {
            ctx.fail("blueTeamが設定されていません.");
            return false;
        }

        if (config.redTeam.value() == null) {
            ctx.fail("redTeamが設定されていません.");
        }

        if (isRunning) {
            ctx.fail("すでに実行中です.");
            return false;
        }

        return true;
    }

    public void start(Player rider, CommandContext ctx) {
        if (!checkStartPreConditions(ctx)) {
            return;
        }
        isRunning = true;

        ball = new Ball(rider, config.center.value(), plugin);
        ball.friction(config.ballFriction.value());

        remainingSecs = config.matchSeconds.value();
        matchTimeBar.setProgress(1.0);
        matchTimeBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(matchTimeBar::addPlayer);

        blueScore = 0;
        redScore = 0;

        taskList.add(new BukkitRunnable() {
            private Integer countDownSecs = 3;

            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (countDownSecs == 0) {
                        p.sendTitle(ChatColor.GREEN + "スタート", "", 10, 40, 0);
                    } else {
                        p.sendTitle(ChatColor.GREEN + countDownSecs.toString(), "", 10, 40, 0);
                    }
                }

                if (countDownSecs == 0) {
                    taskList.add(new MainTask().runTaskTimerAsynchronously(plugin, 0, 0));

                    cancel();
                    return;
                }

                countDownSecs--;
            }
        }.runTaskTimer(plugin, 0, 20));
    }

    public void stop(CommandContext ctx) {
        if (!isRunning) {
            ctx.fail("開始されていません.");
            return;
        }
        stop();
    }

    private void stop() {
        isRunning = false;

        String resultMsg;
        if (blueScore > redScore) {
            resultMsg = ChatColor.BLUE + "青チームの勝利";
        } else if (blueScore < redScore) {
            resultMsg = ChatColor.RED + "赤チームの勝利";
        } else {
            resultMsg = ChatColor.GREEN + "引き分け";
        }
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(resultMsg, "", 10, 100, 10));

        ball.remove();

        matchTimeBar.setVisible(false);

        taskList.forEach(BukkitTask::cancel);
        taskList.clear();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        matchTimeBar.addPlayer(e.getPlayer());
    }

    private class MainTask extends BukkitRunnable {
        private int count = 1;

        public MainTask() {
            matchTimeBar.setTitle("残り時間: " + remainingSecs);
        }

        @Override
        public void run() {
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendActionBar(String.format("青チーム:%d 赤チーム:%d", blueScore, redScore));
            });

            if (count % 20 == 0) {
                remainingSecs--;
                matchTimeBar.setTitle("残り時間: " + remainingSecs);

                if (remainingSecs <= 0) {
                    stop();
                }
            }

            count++;
        }
    }

    @EventHandler
    private void playerCollision(PlayerMoveEvent e) {
        Location ballLocation = ball.getLocation();

        if (e.getPlayer().getLocation().distance(ballLocation) > 1.0) {
            return;
        }

        Vector velocity = e.getTo().toVector().subtract(e.getFrom().toVector()).multiply(20).setY(0);
        ball.setVelocity(ball.getVelocity().add(velocity));
    }
}