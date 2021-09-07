package bct.inoapi;

import java.io.File;
import java.io.IOException;

import bct.hwapi.HardwareConfiguration;
import bct.hwapi.I2C;
import bct.hwapi.SysInfo;

public class Wire {
    I2C i2c;
    byte[] writeBuffer = new byte[16];
    int writeBufferPos;
    byte[] readBuffer;
    int readBufferPos;
    byte slaveAddress;

    public void begin() {
        String deviceName;
        // DB1
        if (SysInfo.getHardwareConfiguration() == HardwareConfiguration.BCT_DB1) {
            deviceName = "/dev/i2c-6";
        } else
        // TM1
        {
            deviceName = "/dev/i2c-1";
        }
        try {
            i2c = new I2C(new File(deviceName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void begin(int address) {
        slaveAddress = (byte)(address & 0xFF);
    }

    public void beginTransmission(int address) {
        slaveAddress = (byte)(address & 0xFF);
        writeBufferPos = 0;
    }

    public void endTransmission() {
        byte[] data = new byte[writeBufferPos];
        System.arraycopy(writeBuffer, 0, data, 0, writeBufferPos);
        try {
            i2c.BufferedWrite(slaveAddress, data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void write(int value8bit) {
        ensureSize(1);
        writeBuffer[writeBufferPos] = (byte)(value8bit & 0xFF);
        writeBufferPos++;
    }

    public void write(String string) {
        byte[] data = string.getBytes();
        write(data, data.length);
    }

    public void write(byte[] data, int length) {
        ensureSize(length);
        System.arraycopy(data, 0, writeBuffer, writeBufferPos, length);
        writeBufferPos += length;
    }

    public void requestFrom(int address, int quantity) {
        if (quantity < 1 || quantity >127) {
            throw new RuntimeException("invalid quantity");
        }
        readBuffer = null;
        readBufferPos = 0;
        try {
            readBuffer = i2c.BufferedRead(slaveAddress, (byte)(quantity));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public int available() {
        if (readBuffer == null) {
            return 0;
        }
        return readBuffer.length - readBufferPos;
    }

    public int read() {
        int val = readBuffer[readBufferPos] & 0xFF;
        readBufferPos++;
        return val;
    }

    // ensure the new contents fits into the buffer
    private void ensureSize(int addSize) {
        if (writeBufferPos + addSize > writeBuffer.length) {
            int newSize = (writeBuffer.length + addSize) + (writeBuffer.length / 4);
            byte[] tmp = new byte[newSize];
            System.arraycopy(writeBuffer, 0, tmp, 0, writeBufferPos);
            writeBuffer = tmp;
        }
    }
}
