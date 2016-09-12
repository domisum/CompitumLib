package de.domisum.lib.compitum.navgraph.edit;

import de.domisum.lib.compitum.CompitumLib;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;

class EditNavGraphCommand extends BukkitCommand
{

	// -------
	// CONSTRUCTOR
	// -------
	EditNavGraphCommand()
	{
		super("editNavGraph");

		this.description = "Used to edit the NavGraphs";
		this.usageMessage = "/editNavGraph";
		// this.setPermission("");

		this.setAliases(new ArrayList<>());
	}


	// -------
	// EXECUTION
	// -------
	@Override
	public boolean execute(CommandSender sender, String alias, String[] args)
	{
		if(!(sender instanceof Player))
		{
			CompitumLib.getLogger().severe("This command can only be used by players!");
			return true;
		}

		Player player = (Player) sender;

		NavGraphEditManager editManager = CompitumLib.getNavGraphManager().getEditManager();
		if(editManager.isActiveFor(player))
			editManager.endEditMode(player);
		else
			editManager.startEditMode(player);

		return true;
	}

}