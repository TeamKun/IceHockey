package net.kunmc.lab.icehockey.command;

import dev.kotx.flylib.command.Command;
import net.kunmc.lab.configlib.ConfigCommand;

public class MainCommand extends Command {
    public MainCommand(ConfigCommand configCommand) {
        super("icehockey");

        children(new StartCommand(), new StopCommand(), configCommand);
    }
}
