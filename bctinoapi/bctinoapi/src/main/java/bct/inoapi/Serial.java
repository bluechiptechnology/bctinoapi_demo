package bct.inoapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import bct.hwapi.SerialPort;

import static bct.hwapi.SerialPortDefinitions.COM2;

public class Serial {

    private static SerialPort serialPort;
    private static PrintStream out;
    private static InputStream in;
    private static int timeout = 1000;
    private static boolean terminatorReached;

    public static void begin(int baudRate) {
        if (baudRate <= 0) {
           out = System.out;
           in = System.in;
        } else {
            try {
                serialPort = new SerialPort(COM2, baudRate, 8, 1, 0, false, false);
                out = new PrintStream(serialPort.getOutputStream());
                in = serialPort.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public static void end() {
        if (serialPort != null) {
            serialPort.Close();
            serialPort = null;
        }
        in = null;
        out = null;
    }

    public static void print(String string) {
        out.print(string);
    }

    public static void println(String string) {
        out.println(string);
    }

    public static void print(int value) {
        out.print(value);
    }

    public static void println(int value) {
        out.println(value);
    }

    public static void print(float value) {
        out.print(value);
    }

    public static void println(float value) {
        out.println(value);
    }

    public static int available() {
        try {
            return in.available();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setTimeout(int timeMs) {
        timeout = timeMs;
    }

    public static int read() {
        try {
            return in.read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int readBytesUntil(byte terminator, byte[] buffer, int length) {
        int t = 0;
        int index = 0;
        terminatorReached = false;
        while (t < timeout) {
            try {
                if (in.available() < 1) {
                    t++;
                    InoCurator.delay(1);
                } else {
                    while (in.available() > 0 && index < length) {
                        byte b = (byte)(in.read() & 0xFF);
                        if (b == terminator) {
                            terminatorReached = true;
                            return index;
                        }
                        buffer[index++] = b;
                    }
                }
                if (index >= length) {
                    return length;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    public static int readBytes(byte[] buffer, int length) {
        int t = 0;
        int index = 0;
        while (t < timeout) {
            t++;
            try {
                int size = in.read(buffer, index, length - index);
                index += size;
                if (index >= length) {
                    return length;
                }
                if (t < timeout) {
                    InoCurator.delay(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    private static String readStringInternal(boolean useTerminator, byte terminator) {
        byte[] buffer = new byte[256];
        long start = System.currentTimeMillis();
        long time = 0;
        String result = new String("");
        while (time < timeout) {
            int size;
            // readBytes blocks until either timeout expires or the buffer is full or terminator is reached
            if (useTerminator) {
                size = readBytesUntil(terminator, buffer, 256);
            } else {
                size = readBytes(buffer, 256);
            }
            if (size > 0) {
                result = result.concat(new String(buffer, 0, size));
            }
            if (useTerminator && terminatorReached) {
                break;
            }
            time = System.currentTimeMillis() - start;
        }
        return result;

    }

    public static String readString() {
        return readStringInternal(false, (byte) 0);
    }
    public static String readStringUntil(char terminator) {
        return readStringInternal(true, (byte) terminator);
    }

    public static void flush() {
        out.flush();
    }
}
