package com.example.kletterbuchv1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageZoomActivity extends AppCompatActivity {

    private ImageView imageView;
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private float startX = 0f, startY = 0f;
    private float oldDist = 1f;

    private Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        imageView = findViewById(R.id.zoomImageView);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        String imageName = getIntent().getStringExtra("image");

        // Lokales Bild laden
        try {
            assert imageName != null;
            File imageFile = new File(getFilesDir(), imageName);
            if (!imageFile.exists()) {
                Toast.makeText(this, "Bild nicht gefunden", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            imageView.setImageBitmap(bitmap);
            imageView.post(this::fitImageToView);

        } catch (IOException e) {
            Log.e("ImageZoomActivity", "Fehler beim Laden des Bildes: " + imageName, e);
            Toast.makeText(this, "Fehler beim Laden des Bildes: " + imageName, Toast.LENGTH_SHORT).show();
            finish();
        }

        imageView.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    startX = event.getX();
                    startY = event.getY();
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - startX;
                        float dy = event.getY() - startY;
                        matrix.postTranslate(dx, dy);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            float px = (event.getX(0) + event.getX(1)) / 2;
                            float py = (event.getY(0) + event.getY(1)) / 2;
                            matrix.postScale(scale, scale, px, py);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            imageView.setImageMatrix(matrix);
            return true;
        });
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void fitImageToView() {
        if (bitmap == null) return;

        float viewWidth = imageView.getWidth();
        float viewHeight = imageView.getHeight();

        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();

        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        matrix.reset();
        matrix.postScale(scale, scale);

        float redundantXSpace = viewWidth - (imageWidth * scale);
        float redundantYSpace = viewHeight - (imageHeight * scale);

        matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
        imageView.setImageMatrix(matrix);
    }
}
