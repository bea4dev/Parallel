package be4rjp.parallel.util;

import it.unimi.dsi.fastutil.shorts.Short2ByteArrayMap;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SectionLevelArray {
    
    private final Short2ByteArrayMap arrayMap = new Short2ByteArrayMap();
    
    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    
    public int getSize(){
        try{
            LOCK.lock();
            return arrayMap.size();
        }finally {
            LOCK.unlock();
        }
    }
    
    public void setLevel(int sectionX, int sectionY, int sectionZ, byte level){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            arrayMap.put(serialIndex, level);
        }finally {
            LOCK.unlock();
        }
    }
    
    public byte getLevel(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            return arrayMap.get(serialIndex);
        }finally {
            LOCK.unlock();
        }
    }
    
    public void removeLevel(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            arrayMap.remove(serialIndex);
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
    
    public boolean threadsafeIteration(ThreadsafeIteration<Byte> iteration) {
        
        boolean notEmpty;
        
        try {
            LOCK.lock();
            for (Map.Entry<Short, Byte> entry : arrayMap.entrySet()) {
                short serialIndex = entry.getKey();
                byte level = entry.getValue();
        
                int x = serialIndex & 0xF;
                int y = (serialIndex >> 8) & 0xF;
                int z = (serialIndex >> 4) & 0xF;
        
                iteration.accept(x, y, z, level);
            }
            notEmpty = arrayMap.size() != 0;
        }finally {
            LOCK.unlock();
        }
        
        return notEmpty;
    }
    
    private short getSerialIndex(int x, int y, int z){return (short) (y << 8 | z << 4 | x);}
    
}

