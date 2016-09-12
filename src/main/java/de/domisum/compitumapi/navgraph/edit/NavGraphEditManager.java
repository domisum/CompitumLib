package de.domisum.compitumapi.navgraph.edit;

import de.domisum.compitumapi.CompitumAPI;
import de.domisum.lib.auxilium.util.bukkit.ItemStackBuilder;
import de.domisum.lib.auxilium.util.bukkit.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NavGraphEditManager
{

	// CONSTANTS
	private static final int TASK_INTERVAL_TICKS = 5;

	// REFERENCES
	private Set<NavGraphEditor> editors = new HashSet<>();
	private BukkitTask updateTask;

	// ITEMSTACKS
	List<ItemStack> editItemStacks = new ArrayList<>();
	ItemStack connectItemStack;
	ItemStack disconnectItemStack;
	ItemStack createNodeItemStack;
	ItemStack removeNodeItemStack;
	ItemStack infoItemStack;

	// STATUS
	private int updateCount = 0;


	// -------
	// CONSTRUCTOR
	// -------
	public NavGraphEditManager()
	{

	}

	public void initialize()
	{
		createEditItemStacks();
		registerCommand();
		new NavGraphEditListener();
	}

	public void terminate()
	{
		stopUpdateTask();

		// copying the set to avoid ConcurrentModificaiton
		for(NavGraphEditor editor : new HashSet<>(this.editors))
			endEditMode(editor.getPlayer());
	}


	private void createEditItemStacks()
	{
		this.connectItemStack = new ItemStackBuilder(Material.SLIME_BALL).displayName(ChatColor.YELLOW+"Connect to closest node")
				.build();
		this.disconnectItemStack = new ItemStackBuilder(Material.SHEARS).displayName(ChatColor.GOLD+"Disconnect from node")
				.build();
		this.createNodeItemStack = new ItemStackBuilder(Material.RABBIT_FOOT).displayName(ChatColor.GREEN+"Create node").build();
		this.removeNodeItemStack = new ItemStackBuilder(Material.BLAZE_POWDER).displayName(ChatColor.RED+"Remove node").build();
		this.infoItemStack = new ItemStackBuilder(Material.BOOK).displayName(ChatColor.AQUA+"Node information").build();


		// this determines the order of the itemstacks in the inventory
		this.editItemStacks.add(this.connectItemStack);
		this.editItemStacks.add(this.createNodeItemStack);
		this.editItemStacks.add(this.disconnectItemStack);
		this.editItemStacks.add(this.removeNodeItemStack);
		this.editItemStacks.add(this.infoItemStack);
	}

	private void registerCommand()
	{
		((CraftServer) CompitumAPI.getPlugin().getServer()).getCommandMap().register("editNavGraph", new EditNavGraphCommand());
	}


	// -------
	// GETTERS
	// -------
	boolean isActiveFor(Player player)
	{
		return getEditor(player) != null;
	}


	private NavGraphEditor getEditor(Player player)
	{
		for(NavGraphEditor editor : this.editors)
			if(editor.getPlayer() == player)
				return editor;

		return null;
	}


	// -------
	// UPDATE
	// -------
	private void startUpdateTask()
	{
		if(this.updateTask != null)
			return;

		this.updateTask = Bukkit.getScheduler().runTaskTimer(CompitumAPI.getPlugin(), this::update, 5, TASK_INTERVAL_TICKS);
	}

	private void stopUpdateTask()
	{
		if(this.updateTask == null)
			return;

		this.updateTask.cancel();
		this.updateTask = null;
	}

	private boolean isUpdateTaskRunning()
	{
		return this.updateTask != null;
	}


	private void update()
	{
		if(this.editors.size() == 0)
		{
			stopUpdateTask();
			return;
		}

		Iterator<NavGraphEditor> iterator = this.editors.iterator();
		while(iterator.hasNext())
		{
			NavGraphEditor editor = iterator.next();
			if(!editor.getPlayer().isOnline())
			{
				iterator.remove();
				PlayerUtil.removeItemStacksFromInventory(editor.getPlayer(), this.editItemStacks);
				continue;
			}

			editor.update(this.updateCount);
		}

		this.updateCount++;
	}


	// -------
	// EDITING MODE
	// -------
	void startEditMode(Player player)
	{
		if(isActiveFor(player))
			return;

		NavGraphEditor editor = new NavGraphEditor(player);
		this.editors.add(editor);
		if(!isUpdateTaskRunning())
			startUpdateTask();

		for(ItemStack is : this.editItemStacks)
			player.getInventory().addItem(is);

		player.sendMessage("NavGraph editing activated");
	}

	void endEditMode(Player player)
	{
		if(!isActiveFor(player))
			return;

		this.editors.remove(getEditor(player));
		PlayerUtil.removeItemStacksFromInventory(player, this.editItemStacks);

		player.sendMessage("NavGraph editing deactivated");
		CompitumAPI.getNavGraphManager().additionalSave();
	}


	// -------
	// EDITING ACTIONS
	// -------
	void connect(Player player)
	{
		// noinspection ConstantConditions
		getEditor(player).connect();
	}

	void disconnect(Player player)
	{
		// noinspection ConstantConditions
		getEditor(player).disconnect();
	}

	void createNode(Player player)
	{
		// noinspection ConstantConditions
		getEditor(player).createNode();
	}

	void removeNode(Player player)
	{
		// noinspection ConstantConditions
		getEditor(player).removeNode();
	}

	void info(Player player)
	{
		// noinspection ConstantConditions
		getEditor(player).info();
	}

}
