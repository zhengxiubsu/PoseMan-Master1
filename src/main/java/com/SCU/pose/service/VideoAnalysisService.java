package com.SCU.pose.service;

import com.SCU.pose.model.Video;
import com.SCU.pose.model.Image;
import com.SCU.pose.model.Coordinate;
import org.springframework.stereotype.Service;

@Service
public class VideoAnalysisService {

    public PushupAnalysisResult analyzePushups(Video video) {
        int pushupCount = 0;
        boolean isDown = false;
        double totalScore = 0.0;

        for (Image image : video.getImages()) {
            if (isPushupDown(image)) {
                if (!isDown) {
                    isDown = true;
                    totalScore += scorePushup(image);
                }
            } else if (isDown) {
                pushupCount++;
                isDown = false;
            }
        }

        double averageScore = pushupCount > 0 ? totalScore / pushupCount : 0;
        return new PushupAnalysisResult(pushupCount, averageScore);
    }

    private boolean isPushupDown(Image image) {
        Coordinate rightElbow = image.getCoordinates().get(13);
        Coordinate rightShoulder = image.getCoordinates().get(12);
        return rightElbow.getY() > rightShoulder.getY();
    }

    public int countPushups(Video video) {
        int pushupCount = 0;
        boolean isDown = false;

        for (Image image : video.getImages()) {
            if (isPushupDown(image)) {
                if (!isDown) {
                    isDown = true;
                }
            } else if (isDown) {
                pushupCount++;
                isDown = false;
            }
        }

        return pushupCount;
    }
    private double scorePushup(Image image) {
        // Implement scoring logic here
        // For example, you could score based on the depth of the pushup, alignment, etc.
        // This is a placeholder implementation
        return 10.0; // Temporary score for each pushup
    }

    public static class PushupAnalysisResult {
        private final int count;
        private final double averageScore;

        public PushupAnalysisResult(int count, double averageScore) {
            this.count = count;
            this.averageScore = averageScore;
        }

        public int getCount() {
            return count;
        }

        public double getAverageScore() {
            return averageScore;
        }
    }
}
