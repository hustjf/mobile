package utility;

import android.util.Log;
import proxy.StateMachine;

/**
 * Created by Alex on 14/10/28.
 */
public class Counter {
    private int count;
    private int maxCount;
    private CounterTask task;
    private boolean stop;
    private Thread thread;
    private Object counterType;

    public Counter(StateMachine.AnchorPointCounters counter) {
        counterType = counter;
        switch (counter) {
            case DATA_TRANSFER_COUNTER:break;
            case CONNECT_COUNTER:
                maxCount = 5;
                task = new CounterTask() {
                    @Override
                    public void run() {
                        Log.e("service", "connect counter over count");

                    }
                };
        }
    }

    public Counter(StateMachine.PerceptionPointCounters counter) {
        counterType = counter;
        switch (counter) {
            case DATA_TRANSFER_COUNTER:break;
            case DATA_BROADCAST_COUNTER:
        }
    }

    public Counter(int maxCount, CounterTask task) {
        count = 0;
        this.maxCount = maxCount;
        this.task = task;
        stop = false;
    }

    public void start() {
        stop = false;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    try {
                        if (count > maxCount) {
                            task.run();
                            stop = true;
                            count = 0;
                            break;
                        }
                    } catch (Exception e) { //if thread statue is block
                        stop = true;
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void addCount() {
        ++count;
        Log.d("service", "count: " + count);
    }

    public void reset() {
        stop = false;
        thread.interrupt();
        count = 0;
    }

    public void close() {
        stop = true;
        thread.interrupt();
    }

    public Object getType() {
        return counterType;
    }
}
