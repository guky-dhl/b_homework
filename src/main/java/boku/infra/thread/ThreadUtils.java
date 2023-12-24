package boku.infra.thread;

import java.util.ArrayList;

public interface ThreadUtils {
    static void joinAll(ArrayList<Thread> threads) {
        threads.forEach((it) -> {
            try {
                it.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static ArrayList<Thread> spawn(int count, Runnable runnable) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var thread = new Thread(runnable);
            thread.start();
            threads.add(thread);
        }
        return threads;
    }
}
