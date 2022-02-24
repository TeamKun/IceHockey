package net.kunmc.lab.icehockey.command;

import dev.kotx.flylib.command.Command;
import net.kunmc.lab.icehockey.IceHockeyPlugin;
import org.bukkit.entity.Player;

import java.util.List;

public class StartCommand extends Command {
    public StartCommand() {
        super("start");

        usage(builder -> {
            builder.entityArgument("ballPlayer", true, false)
                    .executes(ctx -> {
                        IceHockeyPlugin.game.start(((List<Player>) ctx.getTypedArgs().get(0)).get(0), ctx);
                    });
        });
    }
}
