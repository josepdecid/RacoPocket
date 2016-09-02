package com.upc.fib.racopocket.Models;

public class TimetableModel
{
    String name;
    String group;
    String startTime;
    String classroom;

    public TimetableModel(String name, String group, String startTime, String classroom)
    {
        this.name = name;
        this.group = group;
        this.startTime = startTime;
        this.classroom = classroom;
    }

    public String getName()
    {
        return this.name;
    }

    public String getStartTime()
    {
        return this.startTime;
    }

    public String getClassroom()
    {
        return this.classroom;
    }

    public String getGroup()
    {
        return this.group;
    }
}