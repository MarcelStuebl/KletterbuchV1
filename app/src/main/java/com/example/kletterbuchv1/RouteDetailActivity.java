package com.example.kletterbuchv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RouteDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        ImageView imageView = findViewById(R.id.routeImageView);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        FloatingActionButton backFab = findViewById(R.id.backFab);

        String routeJson = getIntent().getStringExtra("route");

        try {
            JSONObject route = new JSONObject(routeJson);

            // Toolbar-Titel setzen
            toolbar.setTitle(route.getString("name"));

            // Lokales Bild laden
            String imagePath = route.getString("image");
            if (imagePath.startsWith("img/")) {
                imagePath = imagePath.substring(4);
            }

            File imageFile = new File(getFilesDir(), imagePath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Bild nicht gefunden: " + imagePath, Toast.LENGTH_SHORT).show();
            }

            // Klick auf das Bild → Zoom-Ansicht starten
            String finalImagePath = imagePath;
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(RouteDetailActivity.this, ImageZoomActivity.class);
                intent.putExtra("image", finalImagePath);  // Nur Dateiname, nicht URL
                startActivity(intent);
            });

        } catch (JSONException | IOException e) {
            Log.e("RouteDetailActivity", "Fehler beim Verarbeiten der Route-Daten", e);
            Toast.makeText(this, "Fehler beim Laden der Route-Daten", Toast.LENGTH_SHORT).show();
        }

        // Zurück-Button
        backFab.setOnClickListener(v -> finish());
    }
}
