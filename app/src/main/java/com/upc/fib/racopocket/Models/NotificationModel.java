package com.upc.fib.racopocket.Models;

import android.support.v4.util.Pair;

import java.util.List;

public class NotificationModel
{
    String idNotification;
    String title;
    String pubDate;
    List<Pair<String, String>> attachmentsList;
    String description;

    public NotificationModel(String idNotification, String title, String pubDate, List<Pair<String, String>> attachmentsList, String description)
    {
        this.idNotification = idNotification;
        this.title = title;
        this.pubDate = pubDate;
        this.attachmentsList = attachmentsList;
        this.description = description;
    }

    public String getIdNotification()
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

    public List<Pair<String, String>> getAttachmentsList()
    {
        return this.attachmentsList;
    }

    public String getDescription()
    {
        return this.description;
    }

}
