package be4rjp.parallel.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class NMSManager {

    private static String version;

    private static boolean isHigher_v1_18_R1;

    private static Class<?> getImplClass(String className)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
        return Class.forName("be4rjp.parallel." + version + "." + className);
    }

    public static String getVersion() {return version;}

    public static boolean isHigher_v1_18_R1() {return isHigher_v1_18_R1;}


    private static INMSHandler nmsHandler;

    public static INMSHandler getNmsHandler() {return nmsHandler;}
    

    private static IPacketHandler mapChunkPacketHandler;
    private static IPacketHandler blockChangePacketHandler;
    private static IPacketHandler multiBlockChangePacketHandler;
    private static IPacketHandler lightUpdatePacketHandler;
    private static IPacketHandler flyPacketHandler;


    public static IPacketHandler getBlockChangePacketHandler() {return blockChangePacketHandler;}

    public static IPacketHandler getMapChunkPacketHandler() {return mapChunkPacketHandler;}

    public static IPacketHandler getFlyPacketHandler() {return flyPacketHandler;}

    public static IPacketHandler getMultiBlockChangePacketHandler() {return multiBlockChangePacketHandler;}

    public static IPacketHandler getLightUpdatePacketHandler() {return lightUpdatePacketHandler;}


    public static void setup(){
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf('.') + 1);

        isHigher_v1_18_R1 = Integer.parseInt(version.split("_")[1]) >= 18;

        try{
            Class<?> nmsHandlerClass = getImplClass("NMSHandler");
            nmsHandler = (INMSHandler) nmsHandlerClass.getConstructor().newInstance();

            Class<?> MapChunkPacketHandler = getImplClass("MapChunkPacketHandler");
            Class<?> BlockChangePacketHandler = getImplClass("BlockChangePacketHandler");
            Class<?> MultiBlockChangePacketHandler = getImplClass("MultiBlockChangePacketHandler");
            Class<?> LightUpdatePacketHandler = getImplClass("LightUpdatePacketHandler");
            Class<?> FlyPacketHandler = getImplClass("FlyPacketHandler");

            mapChunkPacketHandler = (IPacketHandler) MapChunkPacketHandler.getConstructor().newInstance();
            blockChangePacketHandler = (IPacketHandler) BlockChangePacketHandler.getConstructor().newInstance();
            multiBlockChangePacketHandler = (IPacketHandler) MultiBlockChangePacketHandler.getConstructor().newInstance();
            lightUpdatePacketHandler = (IPacketHandler) LightUpdatePacketHandler.getConstructor().newInstance();
            flyPacketHandler = (IPacketHandler) FlyPacketHandler.getConstructor().newInstance();

        }catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();

            throw new IllegalStateException("This version is not supported!"
                    + System.lineSeparator() + "Server version : " + version);
        }
    }

}
