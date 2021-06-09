package be4rjp.parallel.nms.manager;

import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.nio.channels.ClosedChannelException;

public class FlyingPacketManager extends BukkitRunnable {
    
    private static Class<?> PacketPlayInFlying;
    private static Field f;
    
    static {
        try {
            PacketPlayInFlying = NMSUtil.getNMSClass("PacketPlayInFlying");
            f = PacketPlayInFlying.getDeclaredField("f");
            f.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    
    private final ChannelHandlerContext channelHandlerContext;
    private final Object packet;
    private final PacketHandler packetHandler;
    private final Player player;
    
    public FlyingPacketManager(ChannelHandlerContext channelHandlerContext, Object packet, PacketHandler packetHandler, Player player){
        this.channelHandlerContext = channelHandlerContext;
        this.packet = packet;
        this.packetHandler = packetHandler;
        this.player = player;
    }
    
    @Override
    public void run() {
        try{
            boolean onGround = (boolean)f.get(packet);
            
            if(onGround) NMSUtil.setCEtoZero(player);
            
            packetHandler.doRead(channelHandlerContext, packet);
        }catch (ClosedChannelException e){
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
