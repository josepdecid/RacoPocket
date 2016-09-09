package com.upc.fib.racopocket.Models;

public class TimetableSubjectModel {

    String name;
    String group;
    String startTime;
    String classroom;

    /**
     * TimetableSubjectModel constructor.
     * @param name Subject name.
     * @param group Enrolled group.
     * @param startTime When the subject starts.
     * @param classroom Where the classroom takes part.
     */
    public TimetableSubjectModel(String name, String group, String startTime, String classroom) {
        this.name = name;
        this.group = group;
        this.startTime = startTime;
        this.classroom = classroom;
    }

    /**
     * Gets the subject name.
     * @return String object that represents the subject name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the subject group.
     * @return String object that represents the subject group.
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Gets the subject start time.
     * @return String object that represents the start time.
     */
    public String getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the classroom where the subject takes part.
     * @return String object that represents the classroom.
     */
    public String getClassroom() {
        return this.classroom;
    }

}