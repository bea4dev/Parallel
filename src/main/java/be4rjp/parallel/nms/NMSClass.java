package be4rjp.parallel.nms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum NMSClass {
    
    WORLD("World"),
    CHUNK("Chunk"),
    CHUNK_COORD_INT_PAIR("ChunkCoordIntPair"),
    BIOME_STORAGE("BiomeStorage"),
    CHUNK_SECTION("ChunkSection"),
    BLOCK("Block"),
    IBLOCK_DATA("IBlockData"),
    BASE_BLOCK_POSITION("BaseBlockPosition"),
    SECTION_POSITION("SectionPosition"),
    NIBBLE_ARRAY("NibbleArray"),
    ENTITY("Entity"),
    ENTITY_LIVING("EntityLiving"),
    PLAYER_CONNECTION("PlayerConnection"),
    PACKET("Packet"),
    PACKET_PLAY_IN_FLYING("PacketPlayInFlying"),
    PACKET_PLAY_OUT_ENTITY_TELEPORT("PacketPlayOutEntityTeleport"),
    PACKET_PLAY_OUT_BLOCK_CHANGE("PacketPlayOutBlockChange"),
    PACKET_PLAY_OUT_MULTI_BLOCK_CHANGE("PacketPlayOutMultiBlockChange"),
    MULTI_BLOCK_CHANGE_INFO(true, "PacketPlayOutMultiBlockChange$MultiBlockChangeInfo"),
    PACKET_PLAY_OUT_MAP_CHUNK("PacketPlayOutMapChunk"),
    PACKET_PLAY_OUT_LIGHT_UPDATE("PacketPlayOutLightUpdate");
    
    
    public final static boolean HIGHER_1_17_R1;
    
    static{
        Set<Package> nmsPackages = new HashSet<>();
        for(Package p : Package.getPackages()){
            if(p.getName().startsWith("net.minecraft")) nmsPackages.add(p);
        }
        
        loop : for(NMSClass classEnum : NMSClass.values()){
            for(Package nmsPackage : nmsPackages){
                String packageName = nmsPackage.getName();
                
                for(String className : classEnum.classNames){
                    try {
                        classEnum.nmsClass = Class.forName(packageName +  "." + className);
                        continue loop;
                    } catch (ClassNotFoundException e) {/*Ignore*/}
                }
            }
        }
    
        for(NMSClass classEnum : NMSClass.values()){
            if(classEnum.nmsClass == null && !classEnum.notFoundIgnore) {
                new ClassNotFoundException("Not found nms class -> " + Arrays.toString(classEnum.classNames)).printStackTrace();
            }
        }
        
        HIGHER_1_17_R1 = NMSClass.WORLD.getNMSClass().getPackage().getName().equals("net.minecraft.world.level");
        
    }
    
    public static void loadNMSClasses(){
        //static
    }
    
    
    private Class<?> nmsClass = null;
    
    private final String[] classNames;
    
    private final boolean notFoundIgnore;
    
    NMSClass(String... classNames){
        this.classNames = classNames;
        this.notFoundIgnore = false;
    }
    
    NMSClass(boolean notFoundIgnore, String... classNames){
        this.classNames = classNames;
        this.notFoundIgnore = notFoundIgnore;
    }
    
    public Class<?> getNMSClass() {return nmsClass;}
}
