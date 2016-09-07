package com.upc.fib.racopocket.Models;

import android.support.v4.util.Pair;

import java.util.List;

public class NotificationModel
{
    int idNotification;
    String title;
    String pubDate;
    List<Pair<Integer, String>> attachmentsList;
    String description;

    public NotificationModel(int idNotification, String title, String pubDate, List<Pair<Integer, String>> attachmentsList, String description)
    {
        this.idNotification = idNotification;
        this.title = title;
        this.pubDate = pubDate;
        this.attachmentsList = attachmentsList;
        this.description = description;
    }

    public int getIdNotification()
    {
        return this.idNotification;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getPubDate()
    {
        return this.pubDate;
    }

    public List<Pair<Integer, String>> getAttachmentsList()
    {
        return this.attachmentsList;
    }

    public String getDescription()
    {
        return this.description;
    }

}
