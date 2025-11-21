package com.example.treasurehunt;

public class Checkpoint {
    private String name;
    private String clue;
    private double latitude;
    private double longitude;
    private boolean completed;

    // Constructor
    public Checkpoint(String name, String clue, double latitude, double longitude) {
        this.name = name;
        this.clue = clue;
        this.latitude = latitude;
        this.longitude = longitude;
        this.completed = false;
    }

    // Getter dan Setter untuk name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter dan Setter untuk clue
    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    // Getter dan Setter untuk latitude
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Getter dan Setter untuk longitude
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Getter dan Setter untuk completed status
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Method untuk mendapatkan koordinat sebagai string
    public String getCoordinatesString() {
        return latitude + ", " + longitude;
    }

    // Override toString untuk debugging
    @Override
    public String toString() {
        return "Checkpoint{" +
                "name='" + name + '\'' +
                ", clue='" + clue + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", completed=" + completed +
                '}';
    }
}