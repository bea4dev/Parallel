package be4rjp.parallel.util;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureSet extends HashSet<CompletableFuture<?>> {
    
    private final int allTaskCount;
    
    private final Runnable allFinishRunnable;
    
    public CompletableFutureSet(int allTaskCount, Runnable allFinishRunnable){
        this.allTaskCount = allTaskCount;
        this.allFinishRunnable = allFinishRunnable;
    }
    
    
    private int task = 0;
    
    private synchronized void addTask(){
        task++;
        if(allTaskCount == task){
            TaskHandler.runAsync(allFinishRunnable);
        }
    }
    
    @Override
    public boolean add(CompletableFuture<?> completableFuture) {
        completableFuture.thenAccept(v -> {
            addTask();
        });
        return super.add(completableFuture);
    }
}
