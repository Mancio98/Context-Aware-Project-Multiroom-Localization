package com.cas.multiroom.server.messages.connection;

import java.util.ArrayList;
import com.cas.multiroom.server.localization.ReferencePoint;
import com.cas.multiroom.server.messages.Message;

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
