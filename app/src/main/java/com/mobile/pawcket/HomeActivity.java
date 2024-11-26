package com.mobile.pawcket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.mobile.pawcket.BuildConfig;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobile.pawcket.utils.UserManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {
    private PreviewView viewFinder;
    private ImageView ivCapturedImage, ivUserProfile;
    private ImageButton ibSend, ibGallery, ibCameraCapture, ibCameraSwitch, ibClose;
    private TextView tvSharePublic;
    private ConstraintLayout clTopBarContainer, clCapturedImageContainer, clHistoryActionContainer;
    private EditText etCaption;
    private CardView cardViewFinder;
    private ImageCapture imageCapture;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private ExecutorService cameraExecutor;
    private DatabaseReference reference;
    private Uri currentImageUri;
    private UserManager userManager;
    Map<String, String> cloudinaryConfig = new HashMap<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    currentImageUri = uri;
                    ivCapturedImage.setImageURI(uri);
                    showImageMode();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userManager = UserManager.getInstance(this);

        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        viewFinder = findViewById(R.id.viewFinder);
        ivCapturedImage = findViewById(R.id.ivCapturedImage);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        ibSend = findViewById(R.id.ibSend);
        ibGallery = findViewById(R.id.ibGallery);
        ibCameraCapture = findViewById(R.id.ibCameraCapture);
        ibCameraSwitch = findViewById(R.id.ibCameraSwitch);
        ibClose = findViewById(R.id.ibClose);
        cardViewFinder = findViewById(R.id.cardViewFinder);
        tvSharePublic = findViewById(R.id.tvSharePublic);
        clTopBarContainer = findViewById(R.id.clTopBarContainer);
        clCapturedImageContainer = findViewById(R.id.clCapturedImageContainer);
        etCaption = findViewById(R.id.etCaption);
        clHistoryActionContainer = findViewById(R.id.clHistoryActionContainer);

        reference = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_URL)
                .getReference("histories");

        cloudinaryConfig.put("cloud_name", BuildConfig.CLOUDINARY_NAME);
        cloudinaryConfig.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        cloudinaryConfig.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

        try {
            MediaManager.init(this, cloudinaryConfig);
        } catch (IllegalStateException e) {
            // Cloudinary already initialized
        }

        ibCameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        ibCameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        ibGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage.launch("image/*");
            }
        });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCameraView();
            }
        });

        ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        clHistoryActionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.stay);
            }
        });

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .setJpegQuality(100)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    viewFinder.setScaleX(-1f);
                } else {
                    viewFinder.setScaleX(1f);
                }

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Camera initialization failed",
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(
                getOutputDirectory(),
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        currentImageUri = Uri.fromFile(photoFile);

                        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                            Matrix matrix = new Matrix();
                            matrix.preScale(-1.0f, 1.0f);
                            Bitmap flippedBitmap = Bitmap.createBitmap(
                                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true
                            );

                            try (FileOutputStream out = new FileOutputStream(photoFile)) {
                                flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                bitmap.recycle();
                                flippedBitmap.recycle();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        ivCapturedImage.setScaleX(1f);
                        ivCapturedImage.setImageURI(null);
                        ivCapturedImage.setImageURI(currentImageUri);
                        showImageMode();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Toast.makeText(HomeActivity.this,
                                "Photo capture failed: " + exc.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void uploadImage() {
        if (currentImageUri == null) return;

        ibSend.setEnabled(false);
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        try {
            File imageFile = getFileFromUri(currentImageUri);
            String requestId = MediaManager.get().upload(Uri.fromFile(imageFile))
                    .option("folder", "pawcket")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            saveToFirebase(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                Toast.makeText(HomeActivity.this,
                                        "Upload failed: " + error.getDescription(),
                                        Toast.LENGTH_SHORT).show();
                                ibSend.setEnabled(true);
                            });
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {

                        }
                    })
                    .dispatch();

        } catch (IOException e) {
            Toast.makeText(this, "Failed to prepare image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            ibSend.setEnabled(true);
        }
    }

    private void saveToFirebase(String imageUrl) {
        String username = userManager.getUsername();
        String historyId = reference.push().getKey();
        String caption = etCaption.getText().toString();

        if (historyId == null) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to create history entry",
                        Toast.LENGTH_SHORT).show();
                ibSend.setEnabled(true);
            });
            return;
        }

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("imageUrl", imageUrl);
        historyData.put("caption", caption);
        historyData.put("username", username);
        historyData.put("timestamp", System.currentTimeMillis());

        reference.child(historyId).setValue(historyData)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Upload successful",
                            Toast.LENGTH_SHORT).show();
                    ibSend.setEnabled(true);
                    resetCameraView();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this,
                            "Failed to save to database: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    ibSend.setEnabled(true);
                }));
    }

    private void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_FRONT) ?
                CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File destinationFile = new File(getCacheDir(), "temp_image.jpg");

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return destinationFile;
        }
    }

    private void resetCameraView() {
        cardViewFinder.setVisibility(View.VISIBLE);
        clCapturedImageContainer.setVisibility(View.GONE);
        clTopBarContainer.setVisibility(View.VISIBLE);
        ibCameraCapture.setVisibility(View.VISIBLE);
        ibSend.setVisibility(View.GONE);
        ibGallery.setVisibility(View.VISIBLE);
        ibCameraSwitch.setVisibility(View.VISIBLE);
        tvSharePublic.setVisibility(View.GONE);
        ibClose.setVisibility(View.GONE);
        clHistoryActionContainer.setVisibility(View.VISIBLE);
        etCaption.setText("");
        currentImageUri = null;
    }

    private void showImageMode() {
        clTopBarContainer.setVisibility(View.INVISIBLE);
        cardViewFinder.setVisibility(View.GONE);
        clCapturedImageContainer.setVisibility(View.VISIBLE);
        ibSend.setVisibility(View.VISIBLE);
        ibCameraCapture.setVisibility(View.GONE);
        ibGallery.setVisibility(View.GONE);
        ibCameraSwitch.setVisibility(View.GONE);
        ibClose.setVisibility(View.VISIBLE);
        tvSharePublic.setVisibility(View.VISIBLE);
        clHistoryActionContainer.setVisibility(View.GONE);
    }

    private File getOutputDirectory() {
        File mediaDir = getExternalMediaDirs()[0];
        if (mediaDir != null) {
            File appDir = new File(mediaDir, getString(R.string.app_name));
            appDir.mkdirs();
            return appDir;
        }
        return getFilesDir();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}