package bct.grove.gas_monitor;


// Experimental PPM calculator for Grove Gas demo board.
// !!!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !! The calculation results were not verified by         !!
// !! a lab equipment of any sort, therefore are probably  !!
// !! incorrect.                                           !!
// !!             USE AT YOUR OWN RISK                     !!
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
public class XPpmCalc {

    // #### [302B sensor ] ###########################################################
    // XPPM ranges for 302B sensor
    private static int[] B3_XPPM = {
            0, 5, 10, 20 ,30, 40, 60, 80, 100, 500
    };

    // Voltage curve corresponding to the XPPM values expressed as a percentage between
    // the min and max voltage. Taken from the datasheet for the Acetone voltage curve (Red).
    private static float[] B3_RANGE = {
            0, 29.63f, 51.85f, 62.96f, 72.22f, 77.78f, 85.185f, 92.59f, 96.3f, 100f
    };

    // Min And Max voltages @ 3.3V, RL = 20k, RS0 = 180k RS50 = 30k, RS500 = 12k
    private static float[] B3_VOLTAGE = {0.33f , 2.06f};

    // #### [502B sensor ] ###########################################################
    // XPPM ranges for 502B sensor
    private static int[] B5_XPPM = {
            0, 5, 10, 20 ,30, 40, 60, 80, 100, 500
    };

    // Voltage curve corresponding to the XPPM values expressed as a percentage between
    // the min and max voltage. Taken from the datasheet for the Ethanol voltage curve (Blue).
    private static float[] B5_RANGE = {
            0, 50, 66.67f, 72.22f, 77.78f, 83.33f, 87.78f, 93.33f, 95.55f, 100f
    };

    // Min And Max voltages @ 3.3V, RL = 20k, RS0 = 180k RS50 = 30k, RS500 = 19k
    private static float[] B5_VOLTAGE = {0.33f , 1.69f};

    // #### [702B sensor ] ###########################################################
    // XPPM ranges for 702B sensor
    private static int[] B7_XPPM = {
            0, 5, 10, 20 ,30, 40, 60, 80, 100, 500, 1000, 5000
    };

    // Voltage curve corresponding to the XPPM values expressed as a percentage between
    // the min and max voltage. Taken from the datasheet for the CO voltage curve (Black).
    private static float[] B7_RANGE = {
            0, 12.09f, 28.84f, 51.16f, 59.53f, 62.79f, 69.76f, 74.42f, 79.07f, 94.42f, 97.67f, 100f
    };

    // Min And Max voltages @ 3.3V, RL = 100k, RS0 = 240k RS150 = 30k, RS5000 = 26k
    private static float[] B7_VOLTAGE = {0.97f , 2.62f};

    // #### [102B sensor ] ###########################################################
    // XPPM ranges for 102B sensor
    private static int[] B1_XPPM = {
            0, 1, 2, 3 ,4, 5, 6, 7, 8, 9, 10, 20, 30
    };

    // Voltage curve corresponding to the XPPM values expressed as a percentage between
    // the min and max voltage. Taken from the image provided by the sensor manufacturer and 'vcoder' user.
    private static float[] B1_RANGE = {
            0, 22.6f, 39.13f, 52.17f, 60f, 69.56f, 73.91f, 78.26f, 81.74f, 85.22f, 96.09f, 96.52f, 100f
    };

    // Min And Max voltages @ 3.3V, RL = 47k, RS0 = 56k (1.5V measured). Note: inverse logic
    private static float[] B1_VOLTAGE = {1.5f , 0.35f};

    private static XPpmCalc instance;


    // The vcc parameter should be the value of VCC powering the Grove Gas Multisensor module.
    public static final XPpmCalc getInstance(float vcc) {
        if (instance == null) {
            instance = new XPpmCalc(vcc);
        }
        return instance;
    }

    private XPpmCalc(float vcc) {
        float ratio = vcc / 3.3f; // the default values are calculated at 3.3v
        B1_VOLTAGE[0] *= ratio;
        B1_VOLTAGE[1] *= ratio;
        adjustVoltages(B1_RANGE, B1_VOLTAGE[0], B1_VOLTAGE[1]);

        B3_VOLTAGE[0] *= ratio;
        B3_VOLTAGE[1] *= ratio;
        adjustVoltages(B3_RANGE, B3_VOLTAGE[0], B3_VOLTAGE[1]);

        B5_VOLTAGE[0] *= ratio;
        B5_VOLTAGE[1] *= ratio;
        adjustVoltages(B5_RANGE, B5_VOLTAGE[0], B5_VOLTAGE[1]);

        B7_VOLTAGE[0] *= ratio;
        B7_VOLTAGE[1] *= ratio;
        adjustVoltages(B7_RANGE, B7_VOLTAGE[0], B7_VOLTAGE[1]);
    }

    private void adjustVoltages(float[] ranges, float v1, float v2) {
        float d = v2 - v1;
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = v1 + ((d * ranges[i]) / 100f);
        }
    }

    private int getExperimentalPPM(int[] xppms, float[] voltages, float[] vrange, float voltage, boolean inverseLogic) {
        float min = vrange[inverseLogic ? 1 : 0];
        float max = vrange[inverseLogic ? 0 : 1];
        int len = xppms.length;

        //handle the outer margins of the voltage ramges
        if (voltage <= min) {
            return inverseLogic ? xppms[len - 1] : xppms[0];
        }
        if (voltage >= max) {
            return inverseLogic ? xppms[0] : xppms[len - 1];
        }

        float v1 = voltages[0];
        for (int i = 1; i < len; i++) {
            float v2 = voltages[i];
            if (inverseLogic) {
                if (voltage <= v1 && voltage > v2) {
                    return interpolateXppm(v1, v2, voltage, xppms[i - 1], xppms[i]);
                }
            } else {
                if (voltage >= v1 && voltage < v2) {
                    return interpolateXppm(v1, v2, voltage, xppms[i - 1], xppms[i]);
                }
            }
            v1 = v2;
        }
        return xppms[len - 1];
    };

    private int interpolateXppm(float v1, float v2, float v, int x1, int x2) {
        float ratio = (v - v1) / (v2 - v1);
        float result = ((float)(x2 - x1) * ratio) + 0.5f;
        return x1 + (int) result;
    }

    public int getXppm102B(float voltage) {
        return getExperimentalPPM(
                B1_XPPM, B1_RANGE, B1_VOLTAGE, voltage, true
        );
    }

    public int getXppm302B(float voltage) {
        return getExperimentalPPM(
                B3_XPPM, B3_RANGE, B3_VOLTAGE, voltage, false
        );
    }

    public int getXppm502B(float voltage) {
        return getExperimentalPPM(
                B5_XPPM, B5_RANGE, B5_VOLTAGE, voltage, false
        );
    }
    public int getXppm702B(float voltage) {
        return getExperimentalPPM(
                B7_XPPM, B7_RANGE, B7_VOLTAGE, voltage, false
        );
    }
}
