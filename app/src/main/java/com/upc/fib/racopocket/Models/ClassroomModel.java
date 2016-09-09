package com.upc.fib.racopocket.Models;

public class ClassroomModel {

    String name;
    int availability;

    /**
     * ClassroomModel constructor.
     * @param name Classroom name.
     * @param availability Classroom availability.
     */
    public ClassroomModel(String name, int availability) {
        this.name = name;
        this.availability = availability;
    }

    /**
     * Gets the classroom name.
     * @return String object with the classroom name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the free places in a classroom.
     * @return Integer value representing the classroom availability.
     */
    public int getAvailability() {
        return this.availability;
    }

}