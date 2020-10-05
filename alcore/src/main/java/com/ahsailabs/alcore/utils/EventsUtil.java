package com.ahsailabs.alcore.utils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ahsai on 6/15/2017.
 */

public class EventsUtil {
    public static void register(Object subscriber){
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber){
        EventBus.getDefault().unregister(subscriber);
    }
}
