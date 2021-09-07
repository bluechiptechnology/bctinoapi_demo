package bct.grove.gas_monitor;

public interface GasListener {
    public void onGasSampled(float sensor1, float sensor2, float sensor3, float sensor4);
}
