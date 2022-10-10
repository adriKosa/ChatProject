package com.example.adria.chatproject.Model;


import com.google.firebase.firestore.FieldValue;

import java.util.Date;


public class Friend {
    private String friendsName;
    private String friendsImage;
    private String friendsId;
    private String lastMessage;
    private Date timeStamp;
    private String docId;


    public Friend(String friendsName, String friendsImage, String friendsId, String lastMessage, Date timeStamp, String docId) {
        this.friendsName = friendsName;
        this.friendsImage = friendsImage;
        this.friendsId = friendsId;
        this.lastMessage = lastMessage;
        this.timeStamp = timeStamp;
        this.docId = docId;
    }

    public String getFriendsName() {
        return friendsName;
    }

    public String getFriendsImage() {
        return friendsImage;
    }

    public String getFriendsId() {
        return friendsId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Date
    getTimeStamp() {
        return timeStamp;
    }

    public String getDocId() {
        return docId;
    }

    public void setFriendsImage(String friendsImg) {
        this.friendsImage = friendsImg;
    }
}
