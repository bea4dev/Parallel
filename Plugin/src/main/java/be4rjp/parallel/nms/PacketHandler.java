package be4rjp.parallel.nms;

import be4rjp.parallel.Config;
import be4rjp.parallel.player.ParallelPlayer;
import io.netty.channel.*;


public class PacketHandler extends ChannelDuplexHandler{
    
    private final ParallelPlayer parallelPlayer;
    
    public PacketHandler(ParallelPlayer parallelPlayer){this.parallelPlayer = parallelPlayer;}
    
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
    
        INMSHandler nmsHandler = NMSManager.getNmsHandler();
        
        if(nmsHandler.isFlyPacket(packet)){
            super.channelRead(channelHandlerContext, NMSManager.getFlyPacketHandler().rewrite(packet, parallelPlayer, Config.isPerformanceMode()));
            return;
        }
        
        super.channelRead(channelHandlerContext, packet);
    }
    
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {

        INMSHandler nmsHandler = NMSManager.getNmsHandler();

        if(nmsHandler.isMapChunkPacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMapChunkPacketHandler().rewrite(packet, parallelPlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
    
        if(nmsHandler.isLightUpdatePacket(packet) && Config.isRewriteLightPacket()){
            super.write(channelHandlerContext, NMSManager.getLightUpdatePacketHandler().rewrite(packet, parallelPlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        if(nmsHandler.isBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getBlockChangePacketHandler().rewrite(packet, parallelPlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        if(nmsHandler.isMultiBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMultiBlockChangePacketHandler().rewrite(packet, parallelPlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        super.write(channelHandlerContext, packet, channelPromise);
    }

}