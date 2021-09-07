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
package bct.grove.gas_test;

import bct.inoapi.InoSketch;
import bct.inoapi.Serial;
import bct.inoapi.Wire;

public class GasInoSketch extends InoSketch
{
    private static final int DEVICE_ADDRESS = 8;

    GAS_GMXXX gas = new GAS_GMXXX();
    Wire wire = new Wire();

    public void setup()
    {
        Serial.begin(0);
        gas.begin(wire, DEVICE_ADDRESS);
    }

    public void loop()
    {
        Serial.println("-----------------------------------------");
        printGasValue("Nitrogen Dioxide (NO2):  ", gas.getGM102B());
        printGasValue("Ethyl Alcohol (C2H5OH):  ", gas.getGM302B());
        printGasValue("Volatile compounds (VOC):", gas.getGM502B());
        printGasValue("Carbon Monoxide (CO):    ", gas.getGM702B());
        delay(2000L);
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
