package com.socraticphoenix.mc.bukkitbuzz.listener;

import com.socraticphoenix.mc.bukkitbuzz.AbstractPluginService;
import com.socraticphoenix.mc.bukkitbuzz.BukkitBuzzPlugin;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuzzBlockListener extends AbstractPluginService implements Listener {

    public BuzzBlockListener(BukkitBuzzPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent ev) {
        Block clicked = ev.getClickedBlock();
        if (clicked != null && this.plugin.buzzBlockManager().contains(clicked)) {
            ev.setCancelled(true);
            this.plugin.registerBuzz(ev.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent ev) {
        if (this.plugin.buzzBlockManager().contains(ev.getBlock())) {
            ev.setCancelled(true);
        }
    }

}
