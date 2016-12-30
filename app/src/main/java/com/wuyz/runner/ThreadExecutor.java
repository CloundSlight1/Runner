package com.wuyz.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadExecutor {

    private final static String TAG = "ThreadExecutor";

    private static final Object lock = new Object();
    private static ExecutorService executorService;

    public static void initExecutorService() {
        if (executorService == null) {
            synchronized (lock) {
                if (executorService == null) {
                    Log2.i(TAG, "create thread pool");
                    executorService = new ThreadPoolExecutor(2, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>());
                }
            }
        }
    }

    public static void destroyExecutorService() {
        if (executorService != null) {
            synchronized (lock) {
                if (executorService != null) {
                    Log2.i(TAG, "shutdown thread pool");
                    executorService.shutdown();
                    executorService = null;
                }
            }
        }
    }

    public static void execute(Runnable r) {
        if (executorService != null) {
            executorService.execute(r);
        }
    }
}
