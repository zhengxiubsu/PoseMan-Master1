package com.SCU.pose.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.SCU.pose.service.VideoService;
import java.io.IOException;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/process")
    public String processVideo(@RequestParam("file") MultipartFile file,
                               @RequestParam("userId") int userId) {
        try {
            byte[] videoBytes = file.getBytes();
            return videoService.processVideo(videoBytes, userId);
        } catch (IOException e) {
            // Handle the exception
            return "Error processing video: " + e.getMessage();
        }
    }
}

