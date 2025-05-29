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

import java.io.IOException;
import java.io.InputStream;

public class ImageZoomActivity extends AppCompatActivity {

    private ImageView imageView;
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    // States für Touchmodus
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // Punkte für Touch
    private float startX = 0f, startY = 0f;
    private float oldDist = 1f;

    private Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        imageView = findViewById(R.id.zoomImageView);

        // Bild aus Assets laden
        String imageFileName = getIntent().getStringExtra("image");
        try {
            assert imageFileName != null;
            try (InputStream is = getAssets().open(imageFileName)) {
                bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);
            }
        } catch (IOException e) {
            Log.e("RouteDetailActivity", "Fehler beim Laden des Bildes aus den Assets", e);
            Toast.makeText(this, "Fehler beim Laden des Routenbildes", Toast.LENGTH_SHORT).show();
        }

        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(matrix);

        // Warten bis Layout fertig ist, um Bild an ImageView-Größe anzupassen
        imageView.post(this::fitImageToView);

        // Touch-Listener für Drag & Zoom
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
                            float px = event.getX(0) + (event.getX(1) - event.getX(0))/2;
                            float py = event.getY(0) + (event.getY(1) - event.getY(0))/2;
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

    // Berechnet Abstand zwischen zwei Fingern
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // Skaliert Bild initial so, dass es komplett in den ImageView passt (fit to screen)
    private void fitImageToView() {
        if (bitmap == null) return;

        float viewWidth = imageView.getWidth();
        float viewHeight = imageView.getHeight();

        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();

        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;

        // Minimum wählen, damit Bild komplett sichtbar ist
        float scale = Math.min(scaleX, scaleY);

        matrix.reset();
        matrix.postScale(scale, scale);

        // Bild zentrieren
        float redundantXSpace = viewWidth - (imageWidth * scale);
        float redundantYSpace = viewHeight - (imageHeight * scale);

        matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);

        imageView.setImageMatrix(matrix);
    }
}
