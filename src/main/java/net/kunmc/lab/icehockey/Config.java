package net.kunmc.lab.icehockey;

import net.kunmc.lab.configlib.BaseConfig;
import net.kunmc.lab.configlib.value.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Config extends BaseConfig {
    public DoubleValue ballFriction = new DoubleValue(0.0, 0.0, 1.0);
    public LocationValue center = new LocationValue();
    public UUIDValue ballPlayer = new UUIDValue();
    public TeamValue blueTeam = new TeamValue();
    public TeamValue redTeam = new TeamValue();
    public IntegerValue matchSeconds = new IntegerValue(180, 1, 3600);

    public Config(@NotNull Plugin plugin) {
        super(plugin);
    }
}
