package be4rjp.parallel.util;

import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SectionTypeArray {

    private final Short2ObjectArrayMap<Object> arrayMap = new Short2ObjectArrayMap<>();

    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    public ReentrantLock getLOCK() {return LOCK;}
    
    public int getSize(){
        try{
            LOCK.lock();
            return arrayMap.size();
        }finally {
            LOCK.unlock();
        }
    }

    public void setType(int sectionX, int sectionY, int sectionZ, @Nullable Object iBlockData){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            if(iBlockData != null){
                arrayMap.put(serialIndex, iBlockData);
            }else{
                arrayMap.remove(serialIndex);
            }
        }finally {
            LOCK.unlock();
        }
    }

    public @Nullable Object getType(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            return arrayMap.get(serialIndex);
        }finally {
            LOCK.unlock();
        }
    }
    
    public boolean contains(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            return arrayMap.containsKey(serialIndex);
        }finally {
            LOCK.unlock();
        }
    }
    
    public void remove(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            arrayMap.remove(serialIndex);
        }finally {
            LOCK.unlock();
        }
    }
    
    public boolean threadsafeIteration(ThreadsafeIteration<Object> iteration) {
        
        boolean notEmpty;
        
        try {
            LOCK.lock();
            for (Map.Entry<Short, Object> entry : arrayMap.entrySet()) {
                short serialIndex = entry.getKey();
                Object iBlockData = entry.getValue();
                
                int x = serialIndex & 0xF;
                int y = (serialIndex >> 8) & 0xF;
                int z = (serialIndex >> 4) & 0xF;
                
                iteration.accept(x, y, z, iBlockData);
            }
            notEmpty = arrayMap.size() != 0;
        }finally {
            LOCK.unlock();
        }
        
        return notEmpty;
    }

    private short getSerialIndex(int x, int y, int z){return (short) (y << 8 | z << 4 | x);}

}
