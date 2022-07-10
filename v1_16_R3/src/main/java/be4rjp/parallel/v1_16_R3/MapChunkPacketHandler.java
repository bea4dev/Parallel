package be4rjp.parallel.v1_16_R3;

import be4rjp.asyncchunklib.impl.AsyncChunkCache;
import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.IPacketHandler;
import be4rjp.parallel.player.ParallelPlayer;
import be4rjp.parallel.util.SectionTypeArray;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunkSnapshot;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.List;

public class MapChunkPacketHandler implements IPacketHandler {

    private static Field a;
    private static Field b;
    private static Field c;
    private static Field d;
    private static Field e;
    private static Field g;
    private static Field h;

    private static Field blockids;

    private static Field blockIds;
    
    private static BiomeStorage EMPTY_BIOME_STORAGE = new BiomeStorage(null, null) {
        @Override
        public int[] a() {
            return new int[0];
        }
    };

    static {
        try {
            a = PacketPlayOutMapChunk.class.getDeclaredField("a");
            b = PacketPlayOutMapChunk.class.getDeclaredField("b");
            c = PacketPlayOutMapChunk.class.getDeclaredField("c");
            d = PacketPlayOutMapChunk.class.getDeclaredField("d");
            e = PacketPlayOutMapChunk.class.getDeclaredField("e");
            g = PacketPlayOutMapChunk.class.getDeclaredField("g");
            h = PacketPlayOutMapChunk.class.getDeclaredField("h");
            a.setAccessible(true);
            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            e.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);

            blockids = CraftChunkSnapshot.class.getDeclaredField("blockids");
            blockids.setAccessible(true);

            blockIds = ChunkSection.class.getDeclaredField("blockIds");
            blockIds.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, ParallelPlayer parallelPlayer, boolean cacheSetting) {

        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return packet;

        World world = parallelPlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {

            int chunkX = a.getInt(packet);
            int chunkZ = b.getInt(packet);

            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if(parallelChunk == null) return packet;
    
            Object cachedPacket = parallelChunk.getCachedMapChunkPacket();
            if(cachedPacket != null) return cachedPacket;

            ChunkSnapshot chunkSnapshot = AsyncChunkCache.getChunkCache(worldName, chunkX, chunkZ);
            if(chunkSnapshot == null) return packet;


            DataPaletteBlock<IBlockData>[] cachedDataBlocks = (DataPaletteBlock<IBlockData>[]) blockids.get(chunkSnapshot);

            ChunkSection[] chunkSections = new ChunkSection[16];
            int nonEmptyChunkSections = 0;
            boolean edited = false;

            for(int index = 0; index < 16; index++){

                ChunkSection chunkSection = Chunk.a;

                SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(index);
                if(sectionTypeArray != null) {
                    DataPaletteBlock<IBlockData> cachedBlockData = cachedDataBlocks[index];

                    if(cachedBlockData != null) {
                        final short[] nonEmptyTemp = {0};
                        cachedBlockData.a((iBlockData, i) -> {
                            Fluid fluid = iBlockData.getFluid();
                            if (!iBlockData.isAir()) {
                                nonEmptyTemp[0] = (short)(nonEmptyTemp[0] + i);
                            }

                            if (!fluid.isEmpty()) {
                                nonEmptyTemp[0] = (short)(nonEmptyTemp[0] + i);
                            }
                        });
                        short nonEmpty = nonEmptyTemp[0];

                        chunkSection = new ChunkSection(index << 4, nonEmpty, (short) 0, (short) 0);

                        NBTTagCompound data = new NBTTagCompound();
                        cachedBlockData.a(data, "Palette", "BlockStates");
                        DataPaletteBlock<IBlockData> blocks = chunkSection.getBlocks();
                        blocks.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                    }

                    if(chunkSection == Chunk.a) chunkSection = new ChunkSection(index << 4);
    
                    ChunkSection finalChunkSection = chunkSection;
                    boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                        finalChunkSection.setType(x, y, z, (IBlockData) iBlockData);
                    });
                    
                    if(notEmpty) edited = true;

                }else{
                    if(!chunkSnapshot.isSectionEmpty(index)){
                        DataPaletteBlock<IBlockData> dataPaletteBlock = cachedDataBlocks[index];

                        final short[] nonEmptyTemp = {0};

                        dataPaletteBlock.a((iBlockData, i) -> {
                            Fluid fluid = iBlockData.getFluid();
                            if (!iBlockData.isAir()) {
                                nonEmptyTemp[0] = (short)(nonEmptyTemp[0] + i);
                            }

                            if (!fluid.isEmpty()) {
                                nonEmptyTemp[0] = (short)(nonEmptyTemp[0] + i);
                            }
                        });

                        short nonEmpty = nonEmptyTemp[0];
                        chunkSection = new ChunkSection(index << 4, nonEmpty, (short) 0, (short) 0);
                        blockIds.set(chunkSection, dataPaletteBlock);
                    }
                }

                if(chunkSection != Chunk.a) nonEmptyChunkSections |= (1 << index);
                chunkSections[index] = chunkSection;
            }

            if(!edited) return packet;

            Chunk chunk = new Chunk(((CraftWorld) world).getHandle(), new ChunkCoordIntPair(chunkX, chunkZ),
                    EMPTY_BIOME_STORAGE, ChunkConverter.a, TickListEmpty.b(), TickListEmpty.b(), 0L, chunkSections, null);

            int cValue = c.getInt(packet);

            PacketPlayOutMapChunk newPacket = new PacketPlayOutMapChunk(chunk, nonEmptyChunkSections | cValue);

            NBTTagCompound dValue = (NBTTagCompound) d.get(packet);
            Object eValue = e.get(packet);
            List<NBTTagCompound> gValue = (List<NBTTagCompound>) g.get(packet);
            boolean hValue = h.getBoolean(packet);

            d.set(newPacket, dValue);
            e.set(newPacket, eValue);
            g.set(newPacket, gValue);
            h.set(newPacket, hValue || nonEmptyChunkSections == 65535);
    
            if(cacheSetting) parallelChunk.setMapChunkPacketCache(newPacket);

            return newPacket;

        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
