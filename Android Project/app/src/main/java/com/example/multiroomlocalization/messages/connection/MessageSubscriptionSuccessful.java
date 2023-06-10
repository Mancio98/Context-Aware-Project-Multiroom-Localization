package com.example.multiroomlocalization.messages.connection;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

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
