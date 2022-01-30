package be4rjp.parallel.nms;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.util.BlockPosition3i;
import io.netty.channel.Channel;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface INMSHandler {

    Channel getChannel(Player player);
    
    void sendPacket(Player player, Object packet);

    Object getIBlockDataByCombinedId(int id);

    int getCombinedIdByIBlockData(Object iBlockData);

    Object getIBlockData(BlockData blockData);

    BlockData getBukkitBlockData(Object iBlockData);

    Object[] createIBlockDataArray(int length);

    boolean isMapChunkPacket(Object packet);

    boolean isMultiBlockChangePacket(Object packet);

    boolean isBlockChangePacket(Object packet);

    boolean isLightUpdatePacket(Object packet);
    
    boolean isFlyPacket(Object packet);
    
    @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ);
    
    Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks);
    
    void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk);
    
    @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk);

    void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk);
    
}
