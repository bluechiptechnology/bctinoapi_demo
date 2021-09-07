/*
    Multichannel_gas_sensor_V2.0.ino
    Description: A terminal for Seeed Grove Multichannel gas sensor V2.0.
    2019 Copyright (c) Seeed Technology Inc.  All right reserved.
    Author: Hongtai Liu(lht856@foxmail.com)
    2019-9-29
    2021 - Blue Chip Technology

    The MIT License (MIT)
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.1  USA
*/
package bct.grove.gas_monitor;

import bct.inoapi.InoSketch;
import bct.inoapi.Serial;
import bct.inoapi.Wire;

public class GasInoSketch extends InoSketch
{
    private static final int DEVICE_ADDRESS = 8;

    GAS_GMXXX gas = new GAS_GMXXX();
    Wire wire = new Wire();

    GasListener listener;
    long sampleDelayMs;

    public GasInoSketch(GasListener listener, long sampleDelayMs) {
        if (sampleDelayMs < 100) {
            sampleDelayMs = 100;
        }
        this.listener = listener;
        this.sampleDelayMs = sampleDelayMs;
    }

    public void setup()
    {
        Serial.begin(0);
        try {
            gas.begin(wire, DEVICE_ADDRESS);
            delay(1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop()
    {
        int val1, val2, val3, val4;

        try {
            val1 = gas.getGM102B();
            val2 = gas.getGM302B();
            val3 = gas.getGM502B();
            val4 = gas.getGM702B();
            if (listener != null) {
                listener.onGasSampled(
                        gas.calcVol(val1),
                        gas.calcVol(val2),
                        gas.calcVol(val3),
                        gas.calcVol(val4)
                );
            } else {
                Serial.println("-----------------------------------------");
                printGasValue("Nitrogen Dioxide (NO2):  ", val1);
                printGasValue("Ethyl Alcohol (C2H5OH):  ", val2);
                printGasValue("Volatile compounds (VOC):", val3);
                printGasValue("Carbon Monoxide (CO):    ", val4);
            }
        } catch (Exception e) {
            System.out.println("Error reading gas sensor values");
            if (listener != null) {
                listener.onGasSampled(0f, 0f,0f,0f);
            }
        }
        delay(sampleDelayMs);
    }

    public void finish()
    {
        gas.coolDown();
        Serial.println("Gas sketch finished");
    }

    void printGasValue(String label, int val)
    {
        Serial.print(label);
        Serial.print(val);
        Serial.print("  eq  ");
        Serial.print(gas.calcVol(val));
        Serial.println("V");
    }

}
