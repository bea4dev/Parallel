package be4rjp.parallel.world;

public class NibbleArray {
    
    private byte[] bytes = null;
    
    private void createArray(){
        bytes = new byte[2048];
    }
    
    public int getLevel(int x, int y, int z){
        return getLevel(getSerialIndex(x, y, z));
    }
    
    public void setLevel(int x, int y, int z, int level){
        setLevel(getSerialIndex(x, y, z), level);
    }
    
    public int getLevel(int serialIndex) {
        if(this.bytes == null) return 0;
        return this.bytes[serialIndex >> 1] >> (serialIndex & 0x1) << 2 & 0xF;
    }
    
    public void setLevel(int serialIndex, int level) {
        if (this.bytes == null) createArray();
        
        int index = serialIndex >> 1;
        int shift = (serialIndex & 0x1) << 2;
        this.bytes[index] = (byte)(this.bytes[index] & (~(0xF << shift)) | (level & 0xF) << shift);
    }
    
    private int getSerialIndex(int x, int y, int z){
        return y << 8 | z << 4 | x;
    }
}
