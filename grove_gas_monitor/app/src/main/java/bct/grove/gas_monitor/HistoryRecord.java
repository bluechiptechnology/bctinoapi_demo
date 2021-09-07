package bct.grove.gas_monitor;

public class HistoryRecord {
    double s1;
    double s2;
    double s3;
    double s4;
    short samples;
    boolean recordClosed = true;

    public void addSample(float s1, float s2, float s3, float s4) {
        this.s1 += s1;
        this.s2 += s2;
        this.s3 += s3;
        this.s4 += s4;
        samples++;
    }
    public void closeRecord() {
        double d = (double) samples;
        if (samples != 0) {
            s1 /= d;
            s2 /= d;
            s3 /= d;
            s4 /= d;
            samples = 1;
        }
        recordClosed = true;
    }
    public void openRecord() {
        s1 = 0l;
        s2 = 0l;
        s3 = 0l;
        s4 = 0l;
        samples = 0;
        recordClosed = false;
    }
}

