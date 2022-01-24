package be4rjp.parallel.world;

public class ImplParallelChunk {
    
    private final int chunkX;
    
    private final int chunkZ;
    
    private final NibbleArray[] nibbleArrays;
    
    public ImplParallelChunk(int chunkX, int chunkZ){
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        
        this.nibbleArrays = new NibbleArray[16];
        for(int index = 0; index < 16; index++){
            nibbleArrays[index] = new NibbleArray();
        }
        
        
    }
    
    public int getChunkX() {return chunkX;}
    
    public int getChunkZ() {return chunkZ;}
    
    
}
