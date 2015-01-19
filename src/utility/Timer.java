package utility;

import proxy.StateMachine;

/**
 * Created by Alex on 14/11/27.
 */
public class Timer {
    private TimerTask task;
    private java.util.TimerTask timerTask;
    private Object timerType;
    private java.util.Timer timer;
    private long delay;

    public Timer(StateMachine.AnchorPointTimers timer) {
        timerType = timer;
        switch (timer) {
            case DATA_TRANSFER_TIMER:
        }
    }

    public Timer(StateMachine.PerceptionPointTimers timer) {
        timerType = timer;
        switch (timer) {
            case DATA_TRANSFER_TIMER:break;
            case CONNECTING_TIMER:break;
            case LOCATION_BROADCAST_TIMER:break;
            case SLEEP_TIMER:
        }
    }

    public Timer(long delay, TimerTask task) {
        this.task = task;
        this.delay = delay;
        timer = new java.util.Timer();
        timerTask = new java.util.TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
    }

    public void start() {
        timer.schedule(timerTask, delay);
    }

    public void reset() {
        timer.cancel();
        timer.schedule(timerTask, delay);
    }

    public void close() {
        timer.cancel();
    }

    public Object getType() {
        return timerType;
    }

    /**
     * set delay time
     * @param delay amount of time in milliseconds before first execution
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

}
