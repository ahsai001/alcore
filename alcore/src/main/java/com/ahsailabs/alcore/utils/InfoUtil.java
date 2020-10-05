package com.ahsailabs.alcore.utils;

import com.ahsailabs.alcore.events.InfoCounterEvent;
import com.ahsailabs.alcore.events.InfoPositionEvent;
import com.ahsailabs.alcore.events.UpdateInfoListEvent;
import com.ahsailabs.alcore.tables.InformationModel;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ahsai on 6/16/2017.
 */

public class InfoUtil {
    public static long insertNewInfo(String title, String body, String photoUrl, String infoUrl, int type){
        InformationModel newInfo = new InformationModel(title, body, photoUrl, infoUrl, type);

        //save to DB
        newInfo.save();

        //broadcast to any listener
        notifyAddingInfo(newInfo);

        notifyInfoCounter();
        scrollInfoList(newInfo._id);

        return newInfo._id;
    }

    public static void notifyAddingInfo(InformationModel newInfo){
        EventBus.getDefault().post(newInfo);
    }

    public static void notifyInfoCounter(){
        EventBus.getDefault().post(new InfoCounterEvent());
    }

    public static void notifyUpdateInfoList(int position, boolean read){
        EventBus.getDefault().post(new UpdateInfoListEvent(position, read));
    }

    public static void scrollInfoList(long infoId){
        EventBus.getDefault().post(new InfoPositionEvent(infoId));
    }
}
