package bct.grove.gas_monitor;

import java.util.Calendar;
import java.util.Date;

public class SampleHistory {
    private static final int MAX_HISTORY = 24 * 60;
    private static final int MINUTES_IN_HOUR = 60;

    private static final HistoryRecord NULL_RECORD = new HistoryRecord();
    private static final HistoryRecord[] TMP_RECORDS = new HistoryRecord[60];

    private HistoryRecord[] records;
    private int lastIndex = -1;
    private int hour;
    private int minute;
    boolean hourEvent;

    public SampleHistory() {
        records = new HistoryRecord[MAX_HISTORY];
        for (int i = 0; i < MAX_HISTORY; i++) {
            records[i] = new HistoryRecord();
        }
    }

    public boolean addSample(float s1, float s2, float s3, float s4) {
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        minute = Calendar.getInstance().get(Calendar.MINUTE);
        int index = hour * 60 + minute;
        boolean result = checkIndex(index);
        HistoryRecord r = records[index];
        r.addSample(s1, s2, s3, s4);
        return result;
    }

    public boolean isHourEvent() {
        if (minute == 0 && hourEvent == false) {
            hourEvent = true;
            return true;
        }
        if (minute == 1) {
            hourEvent = false;
        }
        return false;
    }

    // retrieve records of the last hour (60 minutes)
    private void getHourRecords(HistoryRecord[] history) {
        int index = hour * 60 + minute;

        //get data from the previous day if required
        if (index < MINUTES_IN_HOUR) {
            int cnt = MINUTES_IN_HOUR - index;
            System.arraycopy(records, MAX_HISTORY - cnt , history, 0, cnt);
            System.arraycopy(records, 0, history, cnt, index);
        } else {
            System.arraycopy(records, index - MINUTES_IN_HOUR, history, 0, MINUTES_IN_HOUR);
        }
    }

    public HistoryRecord getLastMinuteRecord() {
        if (lastIndex < 0) {
            return NULL_RECORD;
        }
        int index = lastIndex - 1;
        if (index < 0) {
            index += MAX_HISTORY;
        }
        //System.out.println("lastIndex=" + lastIndex + " index=" + index);
        return records[index];
    }

    public HistoryRecord getLastHourRecord() {
        HistoryRecord result = new HistoryRecord();

        getHourRecords(TMP_RECORDS);
        for (int i = 0; i < 60; i++) {
            HistoryRecord hr = TMP_RECORDS[i];
            if (hr.samples == 1) {
                result.addSample((float)hr.s1, (float)hr.s2, (float)hr.s3, (float)hr.s4);
            } else if (hr.samples > 1) {
                result.addSample(
                        (float)(hr.s1 / (double) hr.samples),
                        (float)(hr.s2 / (double) hr.samples),
                        (float)(hr.s3 / (double) hr.samples),
                        (float)(hr.s4 / (double) hr.samples)
                );
            }
        }
        result.closeRecord();
        return result;
    }

    private boolean checkIndex(int index) {
        if (index == lastIndex) {
            return false;
        }
        if (lastIndex >= 0) {
            //System.out.println("Closing record index: " + lastIndex);
            records[lastIndex].closeRecord();
        }
        //System.out.println("opening record index: " + index);
        records[index].openRecord();
        lastIndex = index;
        return true;
    }
}
