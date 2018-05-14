package projects.tmc.mycgm2;

import java.util.Date;

class EGVSItem {

    private Date mSystemTime;
    private Date mDisplayTime;
    private float mValue;
    private String mStatus;
    private String mTrend;
    private float mTrendRate;

    public Date getSystemTime() {
        return mSystemTime;
    }

    public void setSystemTime(Date systemTime) {
        mSystemTime = systemTime;
    }

    public Date getDisplayTime() {
        return mDisplayTime;
    }

    public void setDisplayTime(Date displayTime) {
        mDisplayTime = displayTime;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        mValue = value;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public String getTrend() {
        return mTrend;
    }

    public void setTrend(String trend) {
        mTrend = trend;
    }

    public float getTrendRate() {
        return mTrendRate;
    }

    public void setTrendRate(float trendRate) {
        mTrendRate = trendRate;
    }
}
