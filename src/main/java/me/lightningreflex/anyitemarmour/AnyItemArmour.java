package me.lightningreflex.anyitemarmour;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnyItemArmour extends JavaPlugin implements Listener {

	private AnyItemArmour instance;

	@Override
	public void onEnable() {
		// Plugin startup logic
		instance = this;
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
//		instance.getLogger().info("Slot Clicked!");
//		instance.getLogger().info("Slot: " + event.getSlot());
//		instance.getLogger().info("Slot Type: " + event.getSlotType());
//		instance.getLogger().info("Held Item: " + event.getCursor().getType().name());
//		instance.getLogger().info("Clicked Item: " + event.getCurrentItem().getType().name());
//		instance.getLogger().info("Click Type: " + event.getClick().name());

		if (
			event.getSlotType().equals(InventoryType.SlotType.ARMOR) &&
				!player.hasMetadata("isArmorSwapping")
		) {
			// Set metadata to limit the event to one click
			player.setMetadata("isArmorSwapping", new FixedMetadataValue(instance, true));
			// Code to execute the next tick
			Runnable runAtEnd = () -> {};

			// Cancel the event
			event.setCancelled(true);

			// Check click type and run code accordingly
			if (event.getClick().equals(ClickType.LEFT)) { // Left click
				if (!event.getCursor().getType().equals(event.getCurrentItem().getType())) {
					// Get the item in the cursor and clone it, so we don't modify the original
					ItemStack cursorItem = event.getCursor().clone();

					event.setCursor(event.getCurrentItem());

					// Wait a tick to prevent the client from picking up the item again
					runAtEnd = () -> {
						event.setCurrentItem(cursorItem); // Set the armor slot to the cursor item
					};

				} else { // Add the amount of items in the cursor to the armor slot
//					event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() + event.getCursor().getAmount());
//					event.setCursor(new ItemStack(Material.AIR));
					while (event.getCursor().getAmount() > 0 && event.getCurrentItem().getAmount() < event.getCurrentItem().getMaxStackSize()) {
						event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() + 1);
						event.getCursor().setAmount(event.getCursor().getAmount() - 1);
					}
				}


			} else if (event.getClick().equals(ClickType.RIGHT)) { // Right click
				if (event.getCurrentItem().getType().equals(Material.AIR)) { // If the armor slot is empty
					// Add 1 of the item in the cursor to the armor slot
					event.setCurrentItem(new ItemStack(event.getCursor().getType(), 1));
					// Remove 1 of the item in the cursor
					event.getCursor().setAmount(event.getCursor().getAmount() - 1);

				} else { // If the armor slot is not empty
					// Check if the item in the cursor is the same as the item in the armor slot
					if (event.getCursor().getType().equals(event.getCurrentItem().getType())) {
						if (event.getCursor().getAmount() < event.getCursor().getMaxStackSize()) { // If the cursor item is not full
							// Add 1 of the item in the cursor to the armor slot
							event.setCurrentItem(new ItemStack(event.getCursor().getType(), event.getCurrentItem().getAmount() + 1));
							// Remove 1 of the item in the cursor
							event.getCursor().setAmount(event.getCursor().getAmount() - 1);
						}

					} else if (event.getCursor().getType().equals(Material.AIR)) {
						// Take out half of the items in the armor slot
						event.setCursor(new ItemStack(event.getCurrentItem().getType(), (int) Math.round(event.getCurrentItem().getAmount() / 2.0)));
						// Remove half of the items in the armor slot
						event.getCurrentItem().setAmount((int) Math.floor(event.getCurrentItem().getAmount() / 2.0));

					} else if (!event.getCursor().getType().equals(event.getCurrentItem().getType())) { // If the item in the cursor is not the same as the item in the armor slot
						// Get the item in the cursor and clone it, so we don't modify the original
						ItemStack cursorItem = event.getCursor().clone();

						event.setCursor(event.getCurrentItem());

						// Wait a tick to prevent the client from picking up the item again
						runAtEnd = () -> {
							event.setCurrentItem(cursorItem); // Set the armor slot to the cursor item
						};
					}
				}


			} else if (event.getClick().equals(ClickType.NUMBER_KEY)) { // Number key
				// Get the item in the cursor and clone it, so we don't modify the original
				ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
				if (hotbarItem != null) hotbarItem = hotbarItem.clone();

				player.getInventory().setItem(event.getHotbarButton(), event.getCurrentItem());

				// Wait a tick to prevent the client from picking up the item again
				ItemStack finalHotbarItem = hotbarItem;
				runAtEnd = () -> {
					event.setCurrentItem(finalHotbarItem); // Set the armor slot to the cursor item
				};


			} else if (event.getClick().equals(ClickType.SWAP_OFFHAND)) { // Shift left click
				// Get the item in the cursor and clone it, so we don't modify the original
				ItemStack offhandItem = player.getInventory().getItemInOffHand();

				player.getInventory().setItemInOffHand(event.getCurrentItem());

				// Wait a tick to prevent the client from picking up the item again
				runAtEnd = () -> {
					event.setCurrentItem(offhandItem); // Set the armor slot to the cursor item
				};


			} else if (event.getClick().equals(ClickType.DROP)) { // Drop
				event.setCancelled(false);


			} else if (event.getClick().equals(ClickType.CONTROL_DROP)) { // Control drop
				event.setCancelled(false);


			} else if (event.getClick().equals(ClickType.SHIFT_LEFT)) { // Shift left click
				event.setCancelled(false);


			} else if (event.getClick().equals(ClickType.SHIFT_RIGHT)) { // Shift right click
				event.setCancelled(false);


			} else if (event.getClick().equals(ClickType.CREATIVE)) { // Creative
				event.setCancelled(false);
			}

			// Run the code on the next tick
			Runnable finalRunAtEnd = runAtEnd;
			Bukkit.getScheduler().runTaskLater(instance, () -> {
				finalRunAtEnd.run();
				player.removeMetadata("isArmorSwapping", instance);
			}, 1);

		} else if (player.hasMetadata("isArmorSwapping")) {
			// Prevent the player from messing with the inventory while the event is running
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.removeMetadata("isArmorSwapping", instance);
	}
}
