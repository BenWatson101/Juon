package JUOM.WebServices;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CopyOnWriteArrayList;

public class MonitoredThread extends Thread {

    private static final CopyOnWriteArrayList<MonitoredThread> instances = new CopyOnWriteArrayList<>();

    public MonitoredThread(Runnable task) {
        super(task);
        instances.add(this);
        //System.out.println("Created new thread: " + this.getName());
    }



    private long getThreadAllocatedBytes() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (threadMXBean instanceof com.sun.management.ThreadMXBean) {
            return ((com.sun.management.ThreadMXBean) threadMXBean).getThreadAllocatedBytes(this.getId());
        } else {
            throw new UnsupportedOperationException("ThreadMXBean does not support getThreadAllocatedBytes");
        }
    }

    public static synchronized long getTotalAllocatedBytes() {
        long total = 0;
        synchronized (instances) {
            for (MonitoredThread thread : instances) {
                total += thread.getThreadAllocatedBytes();
            }
        }
        return total;
    }

    public static long getTotalInstances() {
        return instances.size();
    }

    public static void printInstancesAndMemory() {
        System.out.println("Total threads instances: " + getTotalInstances());
        System.out.println("Total allocated bytes: " + getTotalAllocatedBytes());
        System.out.println("Total memory bytes: " + Runtime.getRuntime().totalMemory());
        System.out.println("Free memory bytes: " + Runtime.getRuntime().freeMemory());
        System.out.print("\n");
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            instances.remove(this);
        }
    }
}