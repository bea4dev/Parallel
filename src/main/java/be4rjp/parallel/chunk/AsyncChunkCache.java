package be4rjp.parallel.chunk;

import be4rjp.parallel.nms.NMSUtil;
import org.bukkit.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncChunkCache {

    private static final Map<String, AsyncChunkCache> worldChunkCashMap = new ConcurrentHashMap<>();

    public static AsyncChunkCache computeIfAbsentWorldAsyncChunkCash(String worldName){
        return worldChunkCashMap.computeIfAbsent(worldName, k -> new AsyncChunkCache());
    }

    public static AsyncChunkCache getWorldAsyncChunkCash(String worldName){
        return worldChunkCashMap.get(worldName);
    }

    private final Map<Long, Object> chunkCashMap = new ConcurrentHashMap<>();

    public Object addLoadedChunk(Chunk chunk){
        try {
            Object nmsChunk = NMSUtil.getNMSChunk(chunk);
            long coordinate = ((long)chunk.getX() << 32) | (chunk.getZ() & 0xFFFFFFFFL);
            chunkCashMap.put(coordinate, nmsChunk);
            return nmsChunk;
        } catch (Exception e) {e.printStackTrace();}
        
        return null;
    }

    public Object getCashedChunk(int chunkX, int chunkZ){
        return chunkCashMap.get(((long)chunkX << 32) | (chunkZ & 0xFFFFFFFFL));
    }

}
