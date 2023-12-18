package com.SCU.pose.model;

import javax.persistence.*;

@Entity
@Table(name = "Coordinate")
public class Coordinate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String keyPointName; // To identify the key point (e.g., "Nose", "Left Eye", etc.)
    private double x;
    private double y;
    private double z;
    private double visibility;

    // Assuming there's a back-reference to the Image entity
    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    // Constructors
    public Coordinate() {

    }

    public Coordinate(String keyPointName, double x, double y, double z, double visibility) {
        this.keyPointName = keyPointName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.visibility = visibility;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyPointName() {
        return keyPointName;
    }

    public void setKeyPointName(String keyPointName) {
        this.keyPointName = keyPointName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}
