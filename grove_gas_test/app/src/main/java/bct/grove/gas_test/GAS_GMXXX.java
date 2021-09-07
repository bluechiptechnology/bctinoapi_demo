/*
    Multichannel_Gas_GMXXX.cpp
    Description: A drive for Seeed Grove Multichannel gas sensor V2.0.
    2019 Copyright (c) Seeed Technology Inc.  All right reserved.
    Author: Hongtai Liu(lht856@foxmail.com)
    2019-6-18
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
import bct.inoapi.Wire;

public class GAS_GMXXX
{
    private static final float GM_RESOLUTION = 1023F;
    private static final int GM_102B = 1;
    private static final int GM_302B = 3;
    private static final int GM_502B = 5;
    private static final int GM_702B = 7;
    private static final int CHANGE_I2C_ADDR = 85;
    private static final int WARM_UP = 254;
    private static final int COOL_DOWN = 255;

    private Wire wire;
    private boolean isPreheated;
    private int GMXXX_ADDRESS;

    public void begin(Wire wire, int address)
    {
        this.wire = wire;
        wire.begin();
        GMXXX_ADDRESS = address;
        preheat();
    }

    public void preheat()
    {
        if (isPreheated) {
            return;
        }
        GMXXXWriteByte(WARM_UP);
        isPreheated = true;
    }

    public void coolDown()
    {
        if (!isPreheated) {
            return;
        }
        GMXXXWriteByte(COOL_DOWN);
        isPreheated = false;
    }

    public int getGM102B()
    {
        preheat();
        GMXXXWriteByte(GM_102B);
        return GMXXXRead32();
    }

    public int getGM302B()
    {
        preheat();
        GMXXXWriteByte(GM_302B);
        return GMXXXRead32();
    }

    public int getGM502B()
    {
        preheat();
        GMXXXWriteByte(GM_502B);
        return GMXXXRead32();
    }

    public int getGM702B()
    {
        preheat();
        GMXXXWriteByte(GM_702B);
        return GMXXXRead32();
    }

    public void changeGMXXXAddr(int address)
    {
        if(address == 0 || address > 127) {
            address = 8;
        }
        wire.beginTransmission(GMXXX_ADDRESS);
        wire.write(CHANGE_I2C_ADDR);
        wire.write(address);
        wire.endTransmission();
        GMXXX_ADDRESS = address;
    }

    public float calcVol(int adc)
    {
        return ((float)adc * 3.3F) / GM_RESOLUTION;
    }

    private void GMXXXWriteByte(int cmd)
    {
        wire.beginTransmission(GMXXX_ADDRESS);
        wire.write(cmd);
        wire.endTransmission();
        InoSketch.delay(1L);
    }

    private int GMXXXRead32()
    {
        int index = 0;
        int value = 0;
        wire.requestFrom(GMXXX_ADDRESS, 4);
        while(wire.available() > 0) 
        {
            int b = wire.read();
            value += b << 8 * index;
            index++;
        }
        InoSketch.delay(1);
        return value;
    }


}
