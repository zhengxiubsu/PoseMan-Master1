package com.SCU.pose.repository;

import com.SCU.pose.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    // Define additional custom methods here if needed
}
