package com.upc.fib.racopocket.Models;

public class ClassroomModel {
    String name;
    int availability;

    public ClassroomModel(String name, int availability) {
        this.name = name;
        this.availability = availability;
    }

    public String getName() {
        return this.name;
    }

    public int getAvailability() {
        return this.availability;
    }

}