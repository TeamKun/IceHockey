package net.kunmc.lab.icehockey;

import net.kunmc.lab.configlib.BaseConfig;
import net.kunmc.lab.configlib.value.DoubleValue;
import net.kunmc.lab.configlib.value.LocationValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Config extends BaseConfig {
    public DoubleValue ballFriction = new DoubleValue(0.0, 0.0, 1.0);
    public LocationValue center = new LocationValue();

    public Config(@NotNull Plugin plugin) {
        super(plugin);
    }
}
