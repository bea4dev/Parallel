package be4rjp.parallel.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class BlockLocation extends BlockPosition3i{

    public static BlockLocation createBlockLocation(Location location){
        return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockLocation createBlockLocation(Block block){
        return new BlockLocation(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }



    private final World world;

    public BlockLocation(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = world;
    }

    public World getWorld() {return world;}

    public Chunk getChunk(){
        return world.getBlockAt(x, y, z).getChunk();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BlockLocation that = (BlockLocation) o;
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), world);
    }
}
