package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

import java.util.ArrayList;

public class MessageMapDetails extends Message {

    public static String type = "MAP_DETAILS";
    ArrayList<ReferencePoint> referencePointArrayList;

    public MessageMapDetails(ArrayList<ReferencePoint> referencePointArrayList){
        super(type);
        this.referencePointArrayList = referencePointArrayList;
    }

    public void setReferencePointArrayList(ArrayList<ReferencePoint> referencePointArrayList) { this.referencePointArrayList = referencePointArrayList; }

    public ArrayList<ReferencePoint> getReferencePointArrayList() { return referencePointArrayList; }

}
