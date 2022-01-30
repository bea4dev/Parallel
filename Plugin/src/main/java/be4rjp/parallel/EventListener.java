package be4rjp.parallel;

import be4rjp.parallel.impl.ImplParallelPlayer;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.player.ParallelPlayer;
import be4rjp.parallel.util.RegionBlocks;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class EventListener implements Listener {
    
    @EventHandler
    public void onjoin(PlayerJoinEvent event){
        //Inject packet handler
        Player player = event.getPlayer();
        ParallelPlayer parallelPlayer = ImplParallelPlayer.onPlayerJoin(player);

        ParallelUniverse universe = ParallelAPI.getInstance().createUniverse(player.getName());
        universe.addPlayer(parallelPlayer);
        
        ParallelWorld parallelWorld = universe.getWorld(player.getWorld().getName());
        Location loc = player.getLocation();
        RegionBlocks blocks = new RegionBlocks(loc.clone().add(20, 20, 20), loc.clone().add(-20, -20, -20));
        for(Block block : blocks.getBlocks()){
            parallelWorld.setType(block.getX(), block.getY(), block.getZ(), Material.AIR);
        }
        
        PacketHandler packetHandler = new PacketHandler(parallelPlayer);
        
        try {
            ChannelPipeline pipeline = NMSManager.getNmsHandler().getChannel(player).pipeline();
            pipeline.addBefore("packet_handler", Parallel.getPlugin().getName() + "PacketInjector:" + player.getName(), packetHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer(player);
        if(parallelPlayer == null) return;

        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null){
            player.sendMessage("NULL!");
            return;
        }

        ParallelWorld parallelWorld = universe.getWorld(player.getWorld().getName());

        Block block = event.getBlock();
        parallelWorld.setType(block.getX(), block.getY(), block.getZ(), Material.AIR);
        parallelWorld.sendBlockUpdate(block.getX(), block.getY(), block.getZ());

        if(parallelWorld.getType(block.getX(), block.getY(), block.getZ()) != Material.AIR){
            player.sendMessage("NOT EQUAL!");
        }

        event.setCancelled(true);
    }
    
    
    @EventHandler
    public void onleave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        ImplParallelPlayer.onPlayerQuit(player);

        try {
            Channel channel = NMSManager.getNmsHandler().getChannel(player);
            
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(Parallel.getPlugin().getName() + "PacketInjector:" + player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
