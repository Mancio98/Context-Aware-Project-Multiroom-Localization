package com.cas.multiroom.server.messages.connection;


import java.util.ArrayList;

import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

public class MessageSubscriptionSuccessful extends Message {
    public static String type = "SUBSCRIPTION_SUCCESSFULL";

    ArrayList<ReferencePoint> referencePointArrayList;


    public MessageSubscriptionSuccessful(ArrayList<ReferencePoint> referencePointsArrayList){
        super(type);
        this.referencePointArrayList = referencePointsArrayList;
    }

    public ArrayList<ReferencePoint> getReferencePointArrayList() { return referencePointArrayList; }

    public void setReferencePointArrayList(ArrayList<ReferencePoint> referencePointArrayList) { this.referencePointArrayList = referencePointArrayList; }
}
