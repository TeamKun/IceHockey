package net.kunmc.lab.icehockey;

import dev.kotx.flylib.FlyLib;
import net.kunmc.lab.configlib.ConfigCommand;
import net.kunmc.lab.configlib.ConfigCommandBuilder;
import net.kunmc.lab.icehockey.command.MainCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class IceHockeyPlugin extends JavaPlugin {
    public final Config config;

    public IceHockeyPlugin() {
        config = new Config(this);
    }

    @Override
    public void onEnable() {
        ConfigCommand configCommand = new ConfigCommandBuilder(config).build();

        FlyLib.create(this, builder -> {
            builder.command(new MainCommand(configCommand));
        });
    }

    @Override
    public void onDisable() {
    }
}
