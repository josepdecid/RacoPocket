package com.upc.fib.racopocket.Models;

public class NotificationModel {

    private String title;
    private String description;
    private String pubDate;
    private String link;

    /**
     * NotificationModel constructor.
     * @param title Notification title.
     * @param description Notification description.
     * @param pubDate Notification publication date.
     * @param link Notification link.
     */
    public NotificationModel(String title, String description, String pubDate, String link) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.link = link;
    }

    /**
     * Gets the notification title.
     * @return String object representing the notification title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the notification description.
     * @return String object representing the notification description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the notification publication date.
     * @return String object representing the notification pubDate.
     */
    public String getPubDate() {
        return this.pubDate;
    }

    /**
     * Gets the notification link.
     * @return String object representing the notification link.
     */
    public String getLink() {
        return this.link;
    }

}
