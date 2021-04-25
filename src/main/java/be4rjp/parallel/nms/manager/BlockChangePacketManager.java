package be4rjp.parallel.nms.manager;

import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.BlockPosition3i;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.nio.channels.ClosedChannelException;
import java.util.Map;

public class BlockChangePacketManager extends BukkitRunnable {
    
    private static Class<?> PacketPlayOutBlockChange;
    private static Field a;
    
    static {
        try {
            PacketPlayOutBlockChange = NMSUtil.getNMSClass("PacketPlayOutBlockChange");
            a = PacketPlayOutBlockChange.getDeclaredField("a");
            a.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    
    private final ChannelHandlerContext channelHandlerContext;
    private final Object packet;
    private final ChannelPromise channelPromise;
    private final PacketHandler packetHandler;
    private final Player player;
    
    public BlockChangePacketManager(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise, PacketHandler packetHandler, Player player){
        this.channelHandlerContext = channelHandlerContext;
        this.packet = packet;
        this.channelPromise = channelPromise;
        this.packetHandler = packetHandler;
        this.player = player;
    }
    
    
    @Override
    public void run() {
        try{
            Object blockPosition = a.get(packet);
            BlockPosition3i position3i = NMSUtil.getBlockPosition3i(blockPosition);
    
            ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(player);
            
            for (Map.Entry<Location, BlockData> entry : parallelWorld.getBlockMap()) {
                Location location = entry.getKey();
                BlockData blockData = entry.getValue();
                
                if(position3i.getX() == location.getBlockX() && position3i.getY() == location.getBlockY() && position3i.getZ() == location.getBlockZ()){
                    NMSUtil.setIBlockData(packet, NMSUtil.getIBlockData(blockData));
                    break;
                }
            }
            
            packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
        }catch (ClosedChannelException e){
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}