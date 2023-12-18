package com.SCU.pose.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Foreign key in Video table
    private User user; // Reference back to the User entity

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id") // Foreign key in Image table
    private List<Image> images;

    @Column(length = 5000) // Assuming analysis can be a long text
    private String analysis;

    // Constructors
    public Video() {
    }

    public Video(List<Image> images, String analysis) {
        this.images = images;
        this.analysis = analysis;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
