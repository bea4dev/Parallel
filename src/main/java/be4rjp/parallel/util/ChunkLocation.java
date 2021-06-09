package be4rjp.parallel.util;

import java.util.Objects;

public class ChunkLocation {

    public final int x;
    public final int z;

    public ChunkLocation(int blockX, int blockZ){
        this.x = blockX >> 4;
        this.z = blockZ >> 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
