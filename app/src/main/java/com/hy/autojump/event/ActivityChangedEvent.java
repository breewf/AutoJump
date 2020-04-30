package com.hy.autojump.event;

/**
 * @author HY
 * @date 2020/4/29
 * Desc:ActivityChangedEvent
 */
public class ActivityChangedEvent {

    private final String mPackageName;
    private final String mClassName;

    public ActivityChangedEvent(String packageName, String className) {
        mPackageName = packageName;
        mClassName = className;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getClassName() {
        return mClassName;
    }
}
