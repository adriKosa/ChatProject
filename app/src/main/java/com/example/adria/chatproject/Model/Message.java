package com.example.adria.chatproject.Model;

import java.util.Date;

public class Message {
    private String messageText;
    private String recieverId;
    private String senderId;
    private Date timeStamp;


    public Message(String messageText, String recieverId, String senderId, Date timeStamp) {
        this.messageText = messageText;
        this.recieverId = recieverId;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getRecieverId() {
        return recieverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
}
