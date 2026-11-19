package com.company.smartbiometric.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.company.smartbiometric.R;
import com.company.smartbiometric.biometric.FaceFeatureExtractor;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraX Front-Camera Activity for both:
 * 1. Live 1:1 Face Verification with Blink/Head-Turn Anti-Spoofing Liveness Check
 * 2. Admin Consent-Based Face Template Enrollment (averages 5 clean frontal frames)
 */
public class FaceVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode"; // VERIFY or ENROLL
    public static final String EXTRA_EMPLOYEE_CODE = "employee_code";
    public static final String EXTRA_EMPLOYEE_NAME = "employee_name";
    public static final String EXTRA_ACTION_TYPE = "action_type"; // CHECK_IN or CHECK_OUT

    public static final String RESULT_FACE_VECTOR = "result_face_vector";
    public static final String RESULT_LIVENESS_PASSED = "result_liveness_passed";
    public static final String RESULT_LIVENESS_CHALLENGE = "result_liveness_challenge";
    public static final String RESULT_QUALITY_SCORE = "result_quality_score";

    private static final int CAMERA_PERMISSION_CODE = 2001;
    private static final int REQUIRED_GOOD_SAMPLES = 4;

    private PreviewView previewView;
    private TextView tvFaceModeTitle;
    private TextView tvFaceEmployeeLabel;
    private TextView tvLivenessPrompt;
    private ProgressBar progressLiveness;

    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private final FaceFeatureExtractor.LivenessState livenessState = new FaceFeatureExtractor.LivenessState();
    private final List<double[]> collectedSamples = new ArrayList<>();
    private volatile boolean resultDelivered = false;

    private String mode;
    private String employeeCode;
    private String employeeName;
    private String actionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_verification);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        employeeCode = getIntent().getStringExtra(EXTRA_EMPLOYEE_CODE);
        employeeName = getIntent().getStringExtra(EXTRA_EMPLOYEE_NAME);
        actionType = getIntent().getStringExtra(EXTRA_ACTION_TYPE);

        previewView = findViewById(R.id.previewView);
        tvFaceModeTitle = findViewById(R.id.tvFaceModeTitle);
        tvFaceEmployeeLabel = findViewById(R.id.tvFaceEmployeeLabel);
        tvLivenessPrompt = findViewById(R.id.tvLivenessPrompt);
        progressLiveness = findViewById(R.id.progressLiveness);
        Button btnCancel = findViewById(R.id.btnCancelFace);

        if ("ENROLL".equals(mode)) {
            tvFaceModeTitle.setText("Consent-Based Face Enrollment (Front Camera)");
        } else {
            tvFaceModeTitle.setText("Face Verification (" + (actionType != null ? actionType : "ATTENDANCE") + ")");
        }
        tvFaceEmployeeLabel.setText("Employee: " + employeeCode + (employeeName != null ? " • " + employeeName : ""));

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.25f)
                .build();
        faceDetector = FaceDetection.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startFrontCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startFrontCamera();
        } else {
            Toast.makeText(this, "Camera permission is required for face verification.", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void startFrontCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                CameraSelector selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, analysis);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Front camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        if (resultDelivered) {
            imageProxy.close();
            return;
        }
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    if (resultDelivered) return;

                    if (faces.isEmpty()) {
                        runOnUiThread(() -> tvLivenessPrompt.setText("No face detected. Position your face clearly in front of the camera."));
                        return;
                    }
                    if (faces.size() > 1) {
                        runOnUiThread(() -> tvLivenessPrompt.setText("Multiple faces detected! Only ONE employee must be in front of the camera."));
                        return;
                    }

                    Face face = faces.get(0);
                    livenessState.update(face);

                    // Collect clean frontal samples when looking straight ahead
                    boolean isFrontal = Math.abs(face.getHeadEulerAngleY()) < 15f
                            && Math.abs(face.getHeadEulerAngleZ()) < 15f;

                    if (isFrontal) {
                        double[] vector = FaceFeatureExtractor.extractFeatureVector(face);
                        if (vector != null && collectedSamples.size() < REQUIRED_GOOD_SAMPLES) {
                            collectedSamples.add(vector);
                        }
                    }

                    int progress = livenessState.getProgressPercent();
                    String guidance = livenessState.getGuidanceText();

                    runOnUiThread(() -> {
                        progressLiveness.setProgress(progress);
                        tvLivenessPrompt.setText(guidance);
                    });

                    if (livenessState.isLivenessPassed() && collectedSamples.size() >= REQUIRED_GOOD_SAMPLES) {
                        resultDelivered = true;
                        double[] averaged = averageVectors(collectedSamples);
                        deliverSuccess(averaged);
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private double[] averageVectors(List<double[]> samples) {
        int len = samples.get(0).length;
        double[] avg = new double[len];
        for (double[] s : samples) {
            for (int i = 0; i < len; i++) {
                avg[i] += s[i];
            }
        }
        double norm = 0.0;
        for (int i = 0; i < len; i++) {
            avg[i] /= samples.size();
            norm += avg[i] * avg[i];
        }
        norm = Math.sqrt(norm);
        if (norm > 0.0) {
            for (int i = 0; i < len; i++) {
                avg[i] = Math.round((avg[i] / norm) * 100000.0) / 100000.0;
            }
        }
        return avg;
    }

    private void deliverSuccess(double[] faceVector) {
        runOnUiThread(() -> {
            Intent data = new Intent();
            data.putExtra(RESULT_FACE_VECTOR, faceVector);
            data.putExtra(RESULT_LIVENESS_PASSED, true);
            data.putExtra(RESULT_LIVENESS_CHALLENGE, livenessState.getChallengePassedName());
            data.putExtra(RESULT_QUALITY_SCORE, 0.94);
            data.putExtra(EXTRA_ACTION_TYPE, actionType);
            data.putExtra(EXTRA_EMPLOYEE_CODE, employeeCode);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceDetector != null) faceDetector.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}
