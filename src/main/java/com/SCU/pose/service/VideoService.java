package com.SCU.pose.service;

import com.SCU.pose.model.User;
import com.SCU.pose.model.Video;
import com.SCU.pose.model.Image;
import com.SCU.pose.model.Coordinate;
import com.SCU.pose.repository.UserRepository;
import com.SCU.pose.repository.VideoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VideoService {

    @Autowired
    private VideoAnalysisService videoAnalysisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;



    // Method to process video
    public String processVideo(byte[] videoBytes, int userId) {
        // Find user by userId
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            // Handle user not found
            return "unvalid user";
        }

        // Create and save the video object
        Video video = new Video();
        video.setUser(user);
        videoRepository.save(video);

        // Split the video into key frames (every 3 frames)
        List<byte[]> keyFrames = extractKeyFrames(videoBytes);

        // Process each frame
        for (byte[] frame : keyFrames) {
            Image image = new Image();
            List<Coordinate> coordinates = getCoordinatesFromFrame(frame);
            image.setCoordinates(coordinates);
            // Save or process the image object as needed
        }

        int pushupCount = videoAnalysisService.countPushups(video);

        // Add the count to the analysis attribute of the video
        String analysis = "Pushup count: " + pushupCount;
        video.setAnalysis(analysis);

        // Save the updated video object to the database
        videoRepository.save(video);

        return video.getAnalysis();

    }
    static {
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public List<byte[]> extractKeyFrames(byte[] videoBytes) {
        List<byte[]> keyFrames = new ArrayList<>();

        // Assume videoBytes is the byte array of the video file
        // Need to write this byte array to a temporary file, as OpenCV VideoCapture requires a file path
        String tempVideoFilePath = writeToTempFile(videoBytes);

        // Open the video file
        VideoCapture videoCapture = new VideoCapture(tempVideoFilePath);

        if (!videoCapture.isOpened()) {
            // Handle error when opening the video file
            throw new RuntimeException("Failed to open video file: " + tempVideoFilePath);
        }

        Mat frame = new Mat();
        int frameCount = 0;

        // Read video frames
        while (videoCapture.read(frame)) {
            // Extract a key frame every 3 frames
            if (frameCount % 3 == 0) {
                MatOfByte buffer = new MatOfByte();
                // Encode the frame to a byte array
                Imgcodecs.imencode(".jpg", frame, buffer);
                // Add the byte array to the keyFrames list
                keyFrames.add(buffer.toArray());
            }
            frameCount++;
        }

        // Release resources
        videoCapture.release();

        // Delete the temporary file
        deleteTempFile(tempVideoFilePath);

        return keyFrames;
    }

    private String writeToTempFile(byte[] videoBytes) {
        FileOutputStream fos = null;
        try {
            // Create a temporary file
            File tempFile = File.createTempFile("temp_video", ".tmp");

            // Write bytes to the temporary file
            fos = new FileOutputStream(tempFile);
            fos.write(videoBytes);

            // Return the path of the temporary file
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private List<byte[]> extractKeyFrames(byte[] videoBytes) {
//        // Implement the logic to extract every 3rd frame from the video
//        // This is a placeholder implementation
//        return new ArrayList<>();
//    }

    private List<Coordinate> getCoordinatesFromFrame(byte[] frame) {
        RestTemplate restTemplate = new RestTemplate();
        String endpoint = "http://127.0.0.1:5000/upload";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(frame, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);

        // Check if response is OK
        if (response.getStatusCode() != HttpStatus.OK) {
            // Handle error response
            return new ArrayList<>();
        }

        // Parse the response to create Coordinate objects
        return parseCoordinates(response.getBody());
    }

    private List<Coordinate> parseCoordinates(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Double>> coordinatesList = mapper.readValue(json, new TypeReference<List<Map<String, Double>>>() {});

            List<Coordinate> coordinates = new ArrayList<>();
            String[] labels = {
                    "Nose",
                    "Left Eye Inner",
                    "Left Eye",
                    "Left Eye Outer",
                    "Right Eye Inner",
                    "Right Eye",
                    "Right Eye Outer",
                    "Left Ear",
                    "Right Ear",
                    "Mouth Left",
                    "Mouth Right",
                    "Left Shoulder",
                    "Right Shoulder",
                    "Left Elbow",
                    "Right Elbow",
                    "Left Wrist",
                    "Right Wrist",
                    "Left Pinky",
                    "Right Pinky",
                    "Left Index",
                    "Right Index",
                    "Left Thumb",
                    "Right Thumb",
                    "Left Hip",
                    "Right Hip",
                    "Left Knee",
                    "Right Knee",
                    "Left Ankle",
                    "Right Ankle",
                    "Left Heel",
                    "Right Heel",
                    "Left Foot Index",
                    "Right Foot Index"
            };
            for (int i = 0; i < coordinatesList.size(); i++) {
                Map<String, Double> coordinateMap = coordinatesList.get(i);
                Coordinate coordinate = new Coordinate(
                        labels[i],
                        coordinateMap.get("x"),
                        coordinateMap.get("y"),
                        coordinateMap.get("z"),
                        coordinateMap.get("visibility")
                );
                coordinates.add(coordinate);
            }
            return coordinates;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

