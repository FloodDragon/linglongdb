package com.linglong.protocol.message;

/**
 * @author Stereo on 2021/3/8.
 */
public class IndexStatsResponse extends IndexRequest {
    private double entryCount;
    private double keyBytes;
    private double valueBytes;
    private double freeBytes;
    private double totalBytes;

    public double getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(double entryCount) {
        this.entryCount = entryCount;
    }

    public double getKeyBytes() {
        return keyBytes;
    }

    public void setKeyBytes(double keyBytes) {
        this.keyBytes = keyBytes;
    }

    public double getValueBytes() {
        return valueBytes;
    }

    public void setValueBytes(double valueBytes) {
        this.valueBytes = valueBytes;
    }

    public double getFreeBytes() {
        return freeBytes;
    }

    public void setFreeBytes(double freeBytes) {
        this.freeBytes = freeBytes;
    }

    public double getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(double totalBytes) {
        this.totalBytes = totalBytes;
    }
}
