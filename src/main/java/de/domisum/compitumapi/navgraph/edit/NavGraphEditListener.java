package de.domisum.compitumapi.navgraph.edit;

import de.domisum.compitumapi.CompitumAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

class NavGraphEditListener implements Listener
{

	// -------
	// CONSTRUCTOR
	// -------
	NavGraphEditListener()
	{
		registerListener();
	}

	private void registerListener()
	{
		Plugin plugin = CompitumAPI.getPlugin();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	// -------
	// EVENTS
	// -------
	@EventHandler
	public void playerUseEditItem(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if(event.getAction() == Action.PHYSICAL)
			return;

		ItemStack itemStack = event.getItem();
		if(itemStack == null)
			return;

		NavGraphEditManager editManager = CompitumAPI.getNavGraphManager().getEditManager();
		if(!editManager.isActiveFor(player))
			return;

		if(itemStack.isSimilar(editManager.connectItemStack))
			editManager.connect(player);
		else if(itemStack.isSimilar(editManager.disconnectItemStack))
			editManager.disconnect(player);
		else if(itemStack.isSimilar(editManager.createNodeItemStack))
			editManager.createNode(player);
		else if(itemStack.isSimilar(editManager.removeNodeItemStack))
			editManager.removeNode(player);
		else if(itemStack.isSimilar(editManager.infoItemStack))
			editManager.info(player);
	}

	@EventHandler
	public void playerDropEditItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();
		itemStack.setAmount(1); // set to 1 so equels works, in case player drops itemstack with amount bigger than 1

		NavGraphEditManager editManager = CompitumAPI.getNavGraphManager().getEditManager();
		if(!editManager.editItemStacks.contains(itemStack))
			return;

		editManager.endEditMode(player);
		event.getItemDrop().remove();
	}

}
