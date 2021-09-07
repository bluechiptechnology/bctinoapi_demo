package bct.inoapi;

public abstract class InoSketch {
    public abstract void setup();
    public abstract void loop();

    // This method is not in Arduino API, but
    // may be handy in some cases.
    public void finish() {};

    public static void delay(long ms) {
        InoCurator.delay(ms);
    }
}
