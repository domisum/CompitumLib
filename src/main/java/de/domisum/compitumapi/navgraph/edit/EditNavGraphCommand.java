package de.domisum.compitumapi.navgraph.edit;

import de.domisum.compitumapi.CompitumAPI;
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
			CompitumAPI.getLogger().severe("This command can only be used by players!");
			return true;
		}

		Player player = (Player) sender;

		NavGraphEditManager editManager = CompitumAPI.getNavGraphManager().getEditManager();
		if(editManager.isActiveFor(player))
			editManager.endEditMode(player);
		else
			editManager.startEditMode(player);

		return true;
	}

}