package com.example.multiroomlocalization.messages.localization;

import com.example.multiroomlocalization.localization.ReferencePoint;
import com.example.multiroomlocalization.messages.Message;

public class MessageNewReferencePoint  extends Message {
    private ReferencePoint referencePoint;
    private int x;
    private int y;

    public MessageNewReferencePoint(int x,int y,ReferencePoint referencePoint) {
        super("NEW_REFERENCE_POINT");
        this.referencePoint = referencePoint;
        this.x = x;
        this.y = y;
    }

    public ReferencePoint getReferencePoint() {
        return this.referencePoint;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
