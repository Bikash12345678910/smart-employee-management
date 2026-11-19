package com.company.smartbiometric.biometric;

import android.graphics.PointF;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

/**
 * Extracts a 28-dimensional scale- and translation-invariant geometric face vector
 * from ML Kit Face landmarks, and performs real active liveness verification
 * (natural eye blink OR slight head-turn yaw motion) to defeat photo/screen spoofing.
 */
public class FaceFeatureExtractor {

    public static class LivenessState {
        private boolean sawEyesOpen = false;
        private boolean sawEyesClosed = false;
        private boolean blinkCompleted = false;
        private float minYaw = Float.MAX_VALUE;
        private float maxYaw = -Float.MAX_VALUE;
        private boolean headTurnCompleted = false;
        private int validFrames = 0;

        public void update(Face face) {
            validFrames++;
            Float leftEye = face.getLeftEyeOpenProbability();
            Float rightEye = face.getRightEyeOpenProbability();

            if (leftEye != null && rightEye != null) {
                float avgOpen = (leftEye + rightEye) / 2.0f;
                if (avgOpen > 0.70f) {
                    if (sawEyesClosed) {
                        blinkCompleted = true;
                    }
                    sawEyesOpen = true;
                } else if (avgOpen < 0.30f && sawEyesOpen) {
                    sawEyesClosed = true;
                }
            }

            float yaw = face.getHeadEulerAngleY();
            if (yaw < minYaw) minYaw = yaw;
            if (yaw > maxYaw) maxYaw = yaw;
            if ((maxYaw - minYaw) >= 12.0f && validFrames >= 5) {
                headTurnCompleted = true;
            }
        }

        public boolean isLivenessPassed() {
            return blinkCompleted || headTurnCompleted;
        }

        public String getChallengePassedName() {
            if (blinkCompleted) return "BLINK_DETECTED";
            if (headTurnCompleted) return "HEAD_TURN_DETECTED";
            return "PENDING";
        }

        public int getProgressPercent() {
            if (isLivenessPassed()) return 100;
            if (sawEyesClosed) return 75;
            if (sawEyesOpen) return 45;
            return Math.min(35, validFrames * 5);
        }

        public String getGuidanceText() {
            if (isLivenessPassed()) {
                return "Liveness Verified (" + getChallengePassedName() + ")! Capturing template...";
            }
            if (sawEyesClosed) {
                return "Great! Now open your eyes and look straight at the camera.";
            }
            if (sawEyesOpen) {
                return "Face detected! Please BLINK naturally (or turn head slightly) to verify liveness.";
            }
            return "Center your face in the frame with good lighting.";
        }
    }

    /**
     * Extracts 28 normalized geometric features from key facial landmarks.
     * Returns null if required landmarks are missing (e.g., face occluded or turned too far).
     */
    public static double[] extractFeatureVector(Face face) {
        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
        FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
        FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
        FaceLandmark mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
        FaceLandmark leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK);
        FaceLandmark rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK);

        if (leftEye == null || rightEye == null || noseBase == null
                || mouthLeft == null || mouthRight == null || mouthBottom == null) {
            return null;
        }

        PointF pLE = leftEye.getPosition();
        PointF pRE = rightEye.getPosition();
        PointF pN = noseBase.getPosition();
        PointF pML = mouthLeft.getPosition();
        PointF pMR = mouthRight.getPosition();
        PointF pMB = mouthBottom.getPosition();
        PointF pLC = leftCheek != null ? leftCheek.getPosition() : new PointF(pLE.x, pN.y);
        PointF pRC = rightCheek != null ? rightCheek.getPosition() : new PointF(pRE.x, pN.y);

        // Inter-ocular distance as scale normalization anchor
        double iod = dist(pLE, pRE);
        if (iod < 20.0) {
            return null;
        }

        PointF eyeMid = new PointF((pLE.x + pRE.x) / 2.0f, (pLE.y + pRE.y) / 2.0f);
        PointF mouthMid = new PointF((pML.x + pMR.x) / 2.0f, (pML.y + pMR.y) / 2.0f);
        Rect box = face.getBoundingBox();
        double boxW = Math.max(1.0, box.width());
        double boxH = Math.max(1.0, box.height());

        double[] raw = new double[]{
                // Pairwise distance ratios normalized by inter-ocular distance
                dist(pLE, pN) / iod,
                dist(pRE, pN) / iod,
                dist(eyeMid, pN) / iod,
                dist(eyeMid, mouthMid) / iod,
                dist(eyeMid, pMB) / iod,
                dist(pN, mouthMid) / iod,
                dist(pN, pMB) / iod,
                dist(pML, pMR) / iod,
                dist(pML, pMB) / iod,
                dist(pMR, pMB) / iod,
                dist(pLE, pML) / iod,
                dist(pRE, pMR) / iod,
                dist(pLE, pLC) / iod,
                dist(pRE, pRC) / iod,
                dist(pLC, pRC) / iod,
                dist(pLC, pN) / iod,
                dist(pRC, pN) / iod,
                dist(pLC, pML) / iod,
                dist(pRC, pMR) / iod,

                // Relative vertical/horizontal offsets from eye midpoint
                (pN.x - eyeMid.x) / iod,
                (pN.y - eyeMid.y) / iod,
                (mouthMid.x - eyeMid.x) / iod,
                (mouthMid.y - eyeMid.y) / iod,
                (pMB.y - mouthMid.y) / iod,

                // Proportions & bounding aspect
                iod / boxW,
                dist(eyeMid, pMB) / boxH,
                dist(pML, pMR) / dist(pLC, pRC),
                boxW / boxH
        };

        // L2-normalize the feature vector for stable cosine similarity
        double norm = 0.0;
        for (double v : raw) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            return null;
        }
        for (int i = 0; i < raw.length; i++) {
            raw[i] = Math.round((raw[i] / norm) * 100000.0) / 100000.0;
        }
        return raw;
    }

    private static double dist(PointF a, PointF b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
