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
        // Constants to define scoring rules
        final double MAX_SCORE = 10.0;
        final double MIN_ELBOW_HEIGHT_RATIO = 0.7; // Elbow should not go below 70% of the distance from shoulder to hip for a good form
        final double MAX_ELBOW_HEIGHT_RATIO = 1.1; // Elbow should not go above 110% for a good form

        // Get the relevant coordinates
        Coordinate rightElbow = image.getCoordinates().get(13);
        Coordinate rightShoulder = image.getCoordinates().get(12);
        Coordinate rightHip = image.getCoordinates().get(24);

        // Calculate the height ratio of the elbow
        double shoulderToHipDistance = Math.abs(rightShoulder.getY() - rightHip.getY());
        double elbowHeightRatio = (rightElbow.getY() - rightShoulder.getY()) / shoulderToHipDistance;

        // Check form
        double score = MAX_SCORE;
        if (elbowHeightRatio < MIN_ELBOW_HEIGHT_RATIO) {
            // Penalize for going too low
            score *= elbowHeightRatio / MIN_ELBOW_HEIGHT_RATIO;
        } else if (elbowHeightRatio > MAX_ELBOW_HEIGHT_RATIO) {
            // Penalize for not going low enough
            score *= MAX_ELBOW_HEIGHT_RATIO / elbowHeightRatio;
        }

        // You can also add more conditions to check for alignment, such as the elbow being in line with the shoulder.
        // Add other conditions for scoring as needed based on the form

        return score;
    }

    public PlankAnalysisResult analyzePlanks(Video video) {
        boolean isPlankPosition = false;
        double totalPlankScore = 0.0;
        int plankDurations = 0; // This could be the count of frames or time in plank position

        for (Image image : video.getImages()) {
            if (isPlankPosition(image)) {
                isPlankPosition = true;
                totalPlankScore += scorePlank(image);
                plankDurations++;
            } else {
                isPlankPosition = false;
            }
        }

        double averagePlankScore = plankDurations > 0 ? totalPlankScore / plankDurations : 0;
        return new PlankAnalysisResult(plankDurations, averagePlankScore);
    }

    private boolean isPlankPosition(Image image) {
        // Example condition: Check if hips are aligned with shoulders and ankles
        Coordinate shoulders = image.getCoordinates().get(12); // Assuming this is the midpoint of shoulders
        Coordinate hips = image.getCoordinates().get(24); // Assuming this is the midpoint of hips
        Coordinate ankles = image.getCoordinates().get(28); // Assuming this is the midpoint of ankles

        // Allow some margin of error in the alignment
        double alignmentErrorMargin = 0.1; // Example value, adjust based on your requirements

        return Math.abs(shoulders.getY() - hips.getY()) < alignmentErrorMargin &&
                Math.abs(hips.getY() - ankles.getY()) < alignmentErrorMargin;
    }

    private double scorePlank(Image image) {
        final double MAX_SCORE = 10.0;
        final double ALIGNMENT_THRESHOLD = 10.0; // Allowable deviation in degrees from perfect alignment

        // Get coordinates for shoulders, hips, and ankles
        Coordinate leftShoulder = image.getCoordinates().get(11); // Assuming index 11 for left shoulder
        Coordinate rightShoulder = image.getCoordinates().get(12); // Assuming index 12 for right shoulder
        Coordinate leftHip = image.getCoordinates().get(23); // Assuming index 23 for left hip
        Coordinate rightHip = image.getCoordinates().get(24); // Assuming index 24 for right hip
        Coordinate leftAnkle = image.getCoordinates().get(27); // Assuming index 27 for left ankle
        Coordinate rightAnkle = image.getCoordinates().get(28); // Assuming index 28 for right ankle

        // Calculate the midpoint for shoulders, hips, and ankles
        Coordinate shoulderMidpoint = new Coordinate(
                "Shoulder Midpoint",
                (leftShoulder.getX() + rightShoulder.getX()) / 2,
                (leftShoulder.getY() + rightShoulder.getY()) / 2,
                (leftShoulder.getZ() + rightShoulder.getZ()) / 2,
                (leftShoulder.getVisibility() + rightShoulder.getVisibility()) / 2
        );

        Coordinate hipMidpoint = new Coordinate(
                "Hip Midpoint",
                (leftHip.getX() + rightHip.getX()) / 2,
                (leftHip.getY() + rightHip.getY()) / 2,
                (leftHip.getZ() + rightHip.getZ()) / 2,
                (leftHip.getVisibility() + rightHip.getVisibility()) / 2
        );

        Coordinate ankleMidpoint = new Coordinate(
                "Ankle Midpoint",
                (leftAnkle.getX() + rightAnkle.getX()) / 2,
                (leftAnkle.getY() + rightAnkle.getY()) / 2,
                (leftAnkle.getZ() + rightAnkle.getZ()) / 2,
                (leftAnkle.getVisibility() + rightAnkle.getVisibility()) / 2
        );

        // Calculate the angle at the hip using the Law of Cosines
        double angleAtHip = calculateAngle(shoulderMidpoint, hipMidpoint, ankleMidpoint);

        // Scoring based on the angle, closer to 180 degrees (straight line) is better
        double deviationFromPerfect = Math.abs(180.0 - angleAtHip);
        double scoreReduction = (deviationFromPerfect > ALIGNMENT_THRESHOLD) ? (deviationFromPerfect / 180.0) * MAX_SCORE : 0;
        double score = MAX_SCORE - scoreReduction;

        return Math.max(0, score); // Ensure score is not negative
    }

    private double calculateAngle(Coordinate pointA, Coordinate pointB, Coordinate pointC) {
        // Calculate the distance between points
        double distanceAB = calculateDistance(pointA, pointB);
        double distanceBC = calculateDistance(pointB, pointC);
        double distanceAC = calculateDistance(pointA, pointC);

        // Use the Law of Cosines to calculate the angle at point B
        double angleB = Math.acos((Math.pow(distanceAB, 2) + Math.pow(distanceBC, 2) - Math.pow(distanceAC, 2)) / (2 * distanceAB * distanceBC));

        // Convert to degrees
        return Math.toDegrees(angleB);
    }

    private double calculateDistance(Coordinate point1, Coordinate point2) {
        return Math.sqrt(
                Math.pow(point2.getX() - point1.getX(), 2) +
                        Math.pow(point2.getY() - point1.getY(), 2) +
                        Math.pow(point2.getZ() - point1.getZ(), 2)
        );
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

    public static class PlankAnalysisResult {
        private final int duration;
        private final double averageScore;

        public PlankAnalysisResult(int duration, double averageScore) {
            this.duration = duration;
            this.averageScore = averageScore;
        }

        public int getDuration() {
            return duration;
        }

        public double getAverageScore() {
            return averageScore;
        }
    }
}

