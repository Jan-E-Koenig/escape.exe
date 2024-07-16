package at.ac.tuwien.mmue_sb10.util;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Concurrency {
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void executeAsync(Runnable task) {
        executor.execute(task);
    }
}
