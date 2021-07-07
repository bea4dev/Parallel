package be4rjp.parallel.util;

import java.util.Objects;

public class BlockPosition3i {
    
    protected final int x;
    protected final int y;
    protected final int z;
    
    public BlockPosition3i(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX() {return x;}
    
    public int getY() {return y;}
    
    public int getZ() {return z;}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPosition3i that = (BlockPosition3i) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
