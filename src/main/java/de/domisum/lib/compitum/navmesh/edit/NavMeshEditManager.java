package de.domisum.lib.compitum.navmesh.edit;

import de.domisum.lib.compitum.CompitumLib;
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

public class NavMeshEditManager
{

	// CONSTANTS
	private static final int TASK_INTERVAL_TICKS = 3;

	// REFERENCES
	private Set<NavMeshEditor> editors = new HashSet<>();
	private BukkitTask updateTask;

	// ITEMSTACKS
	ItemStack createPointItemStack;
	ItemStack deletePointItemStack;
	ItemStack selectPointItemStack;
	ItemStack deselectPointItemStack;
	ItemStack createTriangleItemStack;
	ItemStack deleteTriangleItemStack;
	ItemStack movePointItemStack;
	ItemStack infoItemStack;
	List<ItemStack> editItemStacks = new ArrayList<>();

	// STATUS
	private int updateCount;


	// -------
	// CONSTRUCTOR
	// -------
	public NavMeshEditManager()
	{

	}

	public void initialize()
	{
		createEditItemStacks();
		registerCommand();
		new NavMeshEditListener();
	}

	public void terminate()
	{
		stopUpdateTask();

		// copying the set to avoid ConcurrentModificaiton
		for(NavMeshEditor editor : new HashSet<>(this.editors))
			endEditMode(editor.getPlayer());
	}


	private void createEditItemStacks()
	{
		this.createPointItemStack = new ItemStackBuilder(Material.SEEDS).displayName(ChatColor.GREEN+"Create new point").build();
		this.deletePointItemStack = new ItemStackBuilder(Material.NETHER_STALK).displayName(ChatColor.RED+"Delete closest point")
				.build();
		this.selectPointItemStack = new ItemStackBuilder(Material.GOLD_INGOT).displayName(ChatColor.YELLOW+"Select closest point")
				.build();
		this.deselectPointItemStack = new ItemStackBuilder(Material.IRON_INGOT)
				.displayName(ChatColor.GOLD+"Deselect closest point").lore("Sneak while using to deselect all").build();

		this.createTriangleItemStack = new ItemStackBuilder(Material.EMERALD).displayName(ChatColor.GREEN+"Create triangle")
				.build();
		this.deleteTriangleItemStack = new ItemStackBuilder(Material.BLAZE_POWDER).displayName(ChatColor.RED+"Delete triangle")
				.build();
		this.movePointItemStack = new ItemStackBuilder(Material.SADDLE).displayName(ChatColor.DARK_AQUA+"Move point").build();
		this.infoItemStack = new ItemStackBuilder(Material.BOOK).displayName(ChatColor.AQUA+"Point/triangle info").build();

		this.editItemStacks.add(this.createPointItemStack);
		this.editItemStacks.add(this.deletePointItemStack);
		this.editItemStacks.add(this.selectPointItemStack);
		this.editItemStacks.add(this.deselectPointItemStack);
		this.editItemStacks.add(this.createTriangleItemStack);
		this.editItemStacks.add(this.deleteTriangleItemStack);
		this.editItemStacks.add(this.movePointItemStack);
		this.editItemStacks.add(this.infoItemStack);
	}

	private void registerCommand()
	{
		((CraftServer) CompitumLib.getPlugin().getServer()).getCommandMap().register("editNavMesh", new EditNavMeshCommand());
	}


	// -------
	// GETTERS
	// -------
	private boolean isUpdateTaskRunning()
	{
		return this.updateTask != null;
	}

	int getUpdateCount()
	{
		return this.updateCount;
	}

	boolean isActiveFor(Player player)
	{
		return getEditor(player) != null;
	}


	NavMeshEditor getEditor(Player player)
	{
		for(NavMeshEditor editor : this.editors)
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

		this.updateTask = Bukkit.getScheduler().runTaskTimer(CompitumLib.getPlugin(), this::update, 5, TASK_INTERVAL_TICKS);
	}

	private void stopUpdateTask()
	{
		if(this.updateTask == null)
			return;

		this.updateTask.cancel();
		this.updateTask = null;
	}

	private void update()
	{
		if(this.editors.size() == 0)
		{
			stopUpdateTask();
			return;
		}

		Iterator<NavMeshEditor> iterator = this.editors.iterator();
		while(iterator.hasNext())
		{
			NavMeshEditor editor = iterator.next();
			if(!editor.getPlayer().isOnline())
			{
				iterator.remove();
				PlayerUtil.removeItemStacksFromInventory(editor.getPlayer(), this.editItemStacks);
				continue;
			}

			editor.update();
		}

		this.updateCount++;
	}


	// -------
	// EDITING MODE
	// -------
	void executeCommand(Player player, String[] args)
	{
		if(args.length == 0)
		{
			if(isActiveFor(player))
				endEditMode(player);
			else
				startEditMode(player);

			return;
		}

		if(!isActiveFor(player))
			startEditMode(player);

		getEditor(player).executeCommand(args);
	}

	private void startEditMode(Player player)
	{
		if(isActiveFor(player))
			return;

		NavMeshEditor editor = new NavMeshEditor(player);
		this.editors.add(editor);
		if(!isUpdateTaskRunning())
			startUpdateTask();

		for(ItemStack is : this.editItemStacks)
			player.getInventory().addItem(is);

		player.sendMessage("NavMesh editing activated");
	}

	void endEditMode(Player player)
	{
		if(!isActiveFor(player))
			return;

		this.editors.remove(getEditor(player));
		PlayerUtil.removeItemStacksFromInventory(player, this.editItemStacks);

		player.sendMessage("NavMesh editing deactivated");
		CompitumLib.getNavMeshManager().additionalSave();
	}

}
