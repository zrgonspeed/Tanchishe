package top.cnzrg.tanchishe.util;

import java.util.ArrayList;
import java.util.List;

public class ThreadManager {
    private List<Thread> threads = new ArrayList<>();
    private static ThreadManager instance;

    public ThreadManager() {
    }

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }

        return instance;
    }

    public void addThread(Thread thread) {
        threads.add(thread);
    }

    public void destory() {
        for (Thread thread : threads) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }

        threads = null;
        instance = null;
    }
}
