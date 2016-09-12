package de.domisum.compitumapi.navmesh.edit;

import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.navmesh.NavMesh;
import de.domisum.compitumapi.navmesh.NavMeshTriangle;
import de.domisum.lib.auxilium.util.DebugUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

class NavMeshEditListener implements Listener
{

	// -------
	// CONSTRUCTOR
	// -------
	NavMeshEditListener()
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

		NavMeshEditManager editManager = CompitumAPI.getNavMeshManager().getEditManager();
		if(!editManager.isActiveFor(player))
			return;

		event.setCancelled(true);

		if(itemStack.isSimilar(editManager.createPointItemStack))
			editManager.getEditor(player).createPoint();
		else if(itemStack.isSimilar(editManager.deletePointItemStack))
			editManager.getEditor(player).deletePoint();
		else if(itemStack.isSimilar(editManager.selectPointItemStack))
			editManager.getEditor(player).selectPoint();
		else if(itemStack.isSimilar(editManager.deselectPointItemStack))
			editManager.getEditor(player).deselectPoint();
		else if(itemStack.isSimilar(editManager.createTriangleItemStack))
			editManager.getEditor(player).createTriangle();
		else if(itemStack.isSimilar(editManager.deleteTriangleItemStack))
			editManager.getEditor(player).deleteTriangle();
		else if(itemStack.isSimilar(editManager.movePointItemStack))
			editManager.getEditor(player).movePoint();
		else if(itemStack.isSimilar(editManager.infoItemStack))
			editManager.getEditor(player).info();
	}

	@EventHandler
	public void playerDropEditItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();
		itemStack.setAmount(1); // set to 1 so equels works, in case player drops itemstack with amount bigger than 1

		NavMeshEditManager editManager = CompitumAPI.getNavMeshManager().getEditManager();
		if(!editManager.editItemStacks.contains(itemStack))
			return;

		editManager.endEditMode(player);
		event.getItemDrop().remove();
	}

	@EventHandler
	public void test(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String[] args = event.getMessage().split("\\s+");
		if(args.length != 2)
			return;

		NavMesh mesh = CompitumAPI.getNavMeshManager().getNavMeshAt(player.getLocation());
		NavMeshTriangle start = mesh.getTriangle(args[0]);
		NavMeshTriangle end = mesh.getTriangle(args[1]);

		List<NavMeshTriangle> path = mesh.findPath(start, end);

		for(NavMeshTriangle triangle : path)
			DebugUtil.say(triangle.id);
	}

}
