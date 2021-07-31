package be4rjp.parallel.util;

import org.bukkit.World;

import java.util.Objects;

public class ChunkLocation extends ChunkPosition{
    
    public final World world;
    
    public ChunkLocation(World world, int blockX, int blockZ) {
        super(blockX, blockZ);
        this.world = world;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChunkLocation that = (ChunkLocation) o;
        return Objects.equals(world, that.world);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), world);
    }
}
