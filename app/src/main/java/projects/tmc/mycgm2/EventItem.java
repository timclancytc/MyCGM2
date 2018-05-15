package projects.tmc.mycgm2;

import java.util.Date;

@SuppressWarnings("unused")
class EventItem {

    private Date mSystemTime;
    private Date mDisplayTime;
    private String mEventType;
    private String mEventSubType;
    private float mValue;
    private String mUnit;

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

    public String getEventType() {
        return mEventType;
    }

    public void setEventType(String eventType) {
        mEventType = eventType;
    }

    public String getEventSubType() {
        return mEventSubType;
    }

    public void setEventSubType(String eventSubType) {
        mEventSubType = eventSubType;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        mValue = value;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }
}
