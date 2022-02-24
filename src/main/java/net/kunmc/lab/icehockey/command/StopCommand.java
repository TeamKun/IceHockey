package net.kunmc.lab.icehockey.command;

import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;
import net.kunmc.lab.icehockey.IceHockeyPlugin;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
    }

    @Override
    public void execute(CommandContext ctx) {
        IceHockeyPlugin.game.stop(ctx);
    }
}
