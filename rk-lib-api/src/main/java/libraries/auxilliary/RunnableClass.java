package libraries.auxilliary;

/**
 * Created by osipenko on 25.03.15.
 */
public class RunnableClass implements Runnable {

    private boolean stopped;
    private long timeout;

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void stop() {
        stopped = true;
        synchronized (this) {
            notify();
        }
    }

    private synchronized void waitForNotify() {
        try {
            if (timeout <= 0) wait();
            else wait(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void body() {
    }

    protected void destroy() {
    }

    @Override
    public void run() {

        while (!stopped) {

            waitForNotify();
            if (stopped) continue;

            body();

        }
        destroy();

    }


}
