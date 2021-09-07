package bct.inoapi;

// Executes an InoSketch - Arduino like code snippet written in java
public class InoCurator implements Runnable {

    private static InoCurator runner;
    private final InoSketch sketch;
    private boolean stopped;

    /**
     * Starts the {@link InoSketch}.
     * @param inoSketch the app to run
     */
    public static void runSketch(InoSketch inoSketch) {
        if (runner != null) {
            throw new RuntimeException("An InoApp is already running");
        }
        if (inoSketch == null) {
            throw new RuntimeException("Invalid inoApp");
        }
        // Start the InoApp in its own thread.
        runner = new InoCurator(inoSketch);
        Thread t = new Thread(runner);
        t.start();
    }

    /**
     * Stops the previously started {@link InoSketch}.
     */
    public static void stop() {
        if (runner != null) {
            runner.stopped = true;
            // wait a bit to ensure the thread has finished
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            runner = null;

        }
    }

    static void delay(long ms) {
        while (ms-- > 0) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
            if (runner != null && runner.stopped) {
                return;
            }
        }
    }

    private InoCurator(InoSketch sketch) {
        this.sketch = sketch;
    }

    public void run() {
        sketch.setup();
        while (!stopped) {
            Thread.yield();
            sketch.loop();
        }
        sketch.finish();
    }

}
