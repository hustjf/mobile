package proxy;

import android.util.Log;
import utility.Counter;
import utility.Timer;

/**
 * Created by Alex on 14/11/18.
 */
public class StateMachine {
    private static Counter currentCounter;
    private static Timer currentTimer;

    public static Counter getCurrentCounter() {
        return currentCounter;
    }

    public static void setCurrentCounter(Counter currentCounter) {
        StateMachine.currentCounter = currentCounter;
    }

    public static Timer getCurrentTimer() {
        return currentTimer;
    }

    public static void setCurrentTimer(Timer currentTimer) {
        StateMachine.currentTimer = currentTimer;
    }

    public static enum AnchorPointTimers {
        DATA_TRANSFER_TIMER
    }

    public static enum AnchorPointCounters {
        CONNECT_COUNTER, DATA_TRANSFER_COUNTER
    }

    public static enum PerceptionPointTimers {
        LOCATION_BROADCAST_TIMER, SLEEP_TIMER, CONNECTING_TIMER, DATA_TRANSFER_TIMER
    }

    public static enum PerceptionPointCounters {
        DATA_BROADCAST_COUNTER, DATA_TRANSFER_COUNTER
    }

    public static enum AnchorPointStates {
        WAITING_FOR_CONNECTION, CONNECTING_RESPONSE, CONNECTED, TRANSFER_RESPONSE, TRANSFER_COMPLETED
    }

    public static enum PerceptionPointStates {
        SLEEP, SENDING_LOCATION_BROADCAST, SENDING_DATA_BROADCAST, WAITING_FOR_CONNECTION, CONNECTED, TRANSFER_RESPONSE, TRANSFER_COMPLETED
    }

    public static enum AnchorPointEvents {
        OPEN_CONNECT_COUNTER, CONNECT_COUNTER_OVER_COUNT, CLOSE_CONNECT_COUNTER, CONNECT, DISCONNECT, SEND_ACK, SEND_NAK, OPEN_DATA_TRANSFER_TIMER,
        DATA_TRANSFER_TIMER_OVERTIME, CLOSE_DATA_TRANSFER_TIMER, OPEN_DATA_TRANSFER_COUNTER, DATA_TRANSFER_COUNTER_OVER_COUNT, CLOSE_DATA_TRANSFER_COUNTER, DATA_TRANSFER
    }

    public static enum PerceptionPointEvents {
        SEND_LOCATION_BROADCAST, OPEN_LOCATION_BROADCAST_TIMER, LOCATION_BROADCAST_TIMER_OVERTIME, CLOSE_LOCATION_BROADCAST_TIMER, OPEN_SLEEP_TIMER,
        SLEEP_TIMER_OVERTIME, CLOSE_SLEEP_TIMER, OPEN_CONNECTING_TIMER, CONNECTING_TIMER_OVERTIME, CLOSE_CONNECTING_TIMER, SEND_DATA_BROADCAST,
        OPEN_DATA_BROADCAST_COUNTER, DATA_BROADCAST_OVER_COUNT, CLOSE_DATA_BROADCAST_COUNTER, OPEN_DATA_TRANSFER_TIMER, DATA_TRANSFER_TIMER_OVERTIME,
        CLOSE_DATA_TRANSFER_TIMER, OPEN_DATA_TRANSFER_COUNTER, DATA_TRANSFER_COUNTER_OVER_COUNT, CLOSE_DATA_TRANSFER_COUNTER, DATA_TRANSFER, SEND_ACK, SEND_NAK
    }

    public static void process(AnchorPointStates state, AnchorPointEvents event) {
        switch (state) {
            case WAITING_FOR_CONNECTION:
                switch (event) {
                    case CLOSE_CONNECT_COUNTER:
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != AnchorPointCounters.CONNECT_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    case CONNECT:
                        //if (platform == android)

                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case CONNECTING_RESPONSE:
                switch (event) {
                    case OPEN_CONNECT_COUNTER:
                        Counter counter = new Counter(AnchorPointCounters.CONNECT_COUNTER);
                        setCurrentCounter(counter);
                        counter.start();
                        break;
                    case CONNECT:
                        //do same as above
                        break;
                    case CONNECT_COUNTER_OVER_COUNT:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case CONNECTED:
                switch (event) {
                    case CLOSE_CONNECT_COUNTER: {
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != AnchorPointCounters.CONNECT_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case DISCONNECT:
                        //if (platform == android)
                        break;
                    case SEND_ACK:
                        //here need to define data structure of data
                        break;
                    case SEND_NAK:
                        //same as ack
                        break;
                    case CLOSE_DATA_TRANSFER_TIMER:
                        Timer timer = getCurrentTimer();
                        timer.close();
                        break;
                    case OPEN_DATA_TRANSFER_COUNTER: {
                        Counter counter = new Counter(AnchorPointCounters.DATA_TRANSFER_COUNTER);
                        setCurrentCounter(counter);
                        counter.start();
                        break;
                    }
                    case DATA_TRANSFER:
                        //if (platform == android)
                        break;
                    case DATA_TRANSFER_COUNTER_OVER_COUNT:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case TRANSFER_RESPONSE:
                switch (event) {
                    case OPEN_DATA_TRANSFER_TIMER:
                        Timer timer = new Timer(AnchorPointTimers.DATA_TRANSFER_TIMER);
                        setCurrentTimer(timer);
                        timer.start();
                        break;
                    case DATA_TRANSFER_TIMER_OVERTIME:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case TRANSFER_COMPLETED:
                switch (event) {
                    case DISCONNECT:
                        //if (platform == android)
                        break;
                    case CLOSE_DATA_TRANSFER_TIMER:
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != AnchorPointTimers.DATA_TRANSFER_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    case CLOSE_DATA_TRANSFER_COUNTER:
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != AnchorPointCounters.DATA_TRANSFER_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    default:
                        processImpossible(state, event);
                }
        }
    }

    public static void process(PerceptionPointStates state, PerceptionPointEvents event) {
        switch (state) {
            case SLEEP:
                switch (event) {
                    case CLOSE_LOCATION_BROADCAST_TIMER: {
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != AnchorPointTimers.DATA_TRANSFER_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case OPEN_SLEEP_TIMER: {
                        Timer timer = new Timer(PerceptionPointTimers.SLEEP_TIMER);
                        setCurrentTimer(timer);
                        timer.start();
                        break;
                    }
                    case SLEEP_TIMER_OVERTIME:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case SENDING_LOCATION_BROADCAST:
                switch (event) {
                    case SEND_LOCATION_BROADCAST:
                        break;
                    case OPEN_LOCATION_BROADCAST_TIMER: {
                        Timer timer = new Timer(PerceptionPointTimers.LOCATION_BROADCAST_TIMER);
                        setCurrentTimer(timer);
                        timer.start();
                        break;
                    }
                    case LOCATION_BROADCAST_TIMER_OVERTIME:
                        //start method has process this situation
                        break;
                    case CLOSE_SLEEP_TIMER:
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.SLEEP_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    case CLOSE_DATA_BROADCAST_COUNTER:
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != PerceptionPointCounters.DATA_BROADCAST_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case SENDING_DATA_BROADCAST:
                switch (event) {
                    case CLOSE_LOCATION_BROADCAST_TIMER: {
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.LOCATION_BROADCAST_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case CLOSE_CONNECTING_TIMER: {
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.CONNECTING_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case SEND_DATA_BROADCAST:
                        //if(platform == android)
                        break;
                    case OPEN_DATA_BROADCAST_COUNTER:
                        Counter counter = new Counter(PerceptionPointCounters.DATA_BROADCAST_COUNTER);
                        setCurrentCounter(counter);
                        counter.start();
                        break;
                    case DATA_BROADCAST_OVER_COUNT:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case WAITING_FOR_CONNECTION:
                switch (event) {
                    case OPEN_CONNECTING_TIMER:
                        Timer timer = new Timer(PerceptionPointTimers.CONNECTING_TIMER);
                        setCurrentTimer(timer);
                        timer.start();
                        break;
                    case CONNECTING_TIMER_OVERTIME:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case CONNECTED:
                switch (event) {
                    case CLOSE_CONNECTING_TIMER: {
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.CONNECTING_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case CLOSE_DATA_BROADCAST_COUNTER: {
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != PerceptionPointCounters.DATA_BROADCAST_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case CLOSE_DATA_TRANSFER_TIMER: {
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.DATA_TRANSFER_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    }
                    case OPEN_DATA_TRANSFER_COUNTER: {
                        Counter counter = new Counter(PerceptionPointCounters.DATA_TRANSFER_COUNTER);
                        setCurrentCounter(counter);
                        counter.start();
                        break;
                    }
                    case DATA_TRANSFER_COUNTER_OVER_COUNT:
                        //start method has process this situation
                        break;
                    case DATA_TRANSFER:
                        //if(platform == android)
                        break;
                    case SEND_ACK:
                        //here need to define data structure of data
                        break;
                    case SEND_NAK:
                        //same as ack
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case TRANSFER_RESPONSE:
                switch (event) {
                    case OPEN_DATA_TRANSFER_TIMER:
                        Timer timer = new Timer(PerceptionPointTimers.DATA_TRANSFER_TIMER);
                        setCurrentTimer(timer);
                        timer.start();
                        break;
                    case DATA_TRANSFER_TIMER_OVERTIME:
                        //start method has process this situation
                        break;
                    default:
                        processImpossible(state, event);
                }
                break;
            case TRANSFER_COMPLETED:
                switch (event) {
                    case CLOSE_DATA_TRANSFER_TIMER:
                        Timer timer = getCurrentTimer();
                        if (timer != null && timer.getType() != PerceptionPointTimers.DATA_TRANSFER_TIMER)
                            timer.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    case CLOSE_DATA_TRANSFER_COUNTER:
                        Counter counter = StateMachine.getCurrentCounter();
                        if (counter != null && counter.getType() != PerceptionPointCounters.DATA_TRANSFER_COUNTER)
                            counter.close();
                        else
                            Log.e("state machine", "state: " + state + "\nevent: " + event);
                        break;
                    default:
                        processImpossible(state, event);
                }
        }
    }

    private static void processImpossible(AnchorPointStates state, AnchorPointEvents event) {
        Log.e("state machine", "State: " + state + "\nEvent: " + event);
    }

    private static void processImpossible(PerceptionPointStates state, PerceptionPointEvents event) {
        Log.e("state machine", "State: " + state + "\nEvent: " + event);
    }
}
