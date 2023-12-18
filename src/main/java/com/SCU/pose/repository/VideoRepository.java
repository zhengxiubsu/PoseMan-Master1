package com.SCU.pose.repository;

import com.SCU.pose.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
    // Define additional custom methods here if needed
}
