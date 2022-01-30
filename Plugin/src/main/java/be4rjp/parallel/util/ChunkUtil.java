package be4rjp.parallel.util;

public class ChunkUtil {
    
    public static long getCoordinateKey(final int x, final int z) {return ((long)z << 32) | (x & 0xFFFFFFFFL);}
    
}
