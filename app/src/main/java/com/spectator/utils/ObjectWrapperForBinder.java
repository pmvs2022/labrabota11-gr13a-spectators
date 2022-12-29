package com.spectator.utils;

import android.os.Binder;

//Class for sending objects between activities
public class ObjectWrapperForBinder extends Binder {

    private final Object mData;

    public ObjectWrapperForBinder(Object data) {
        mData = data;
    }

    public Object getData() {
        return mData;
    }
}