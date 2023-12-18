package com.SCU.pose.model;


import javax.persistence.*;
import java.util.*;
@Entity
@Table(name = "Image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id") // Foreign key in Coordinate table
    private List<Coordinate> coordinates;

    // Constructors
    public Image() {
    }

    public Image(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

}
