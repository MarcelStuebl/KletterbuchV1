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

import java.io.IOException;
import java.io.InputStream;

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

            // Toolbar-Titel
            toolbar.setTitle(route.getString("name"));

            // Bild aus Assets laden
            String imageFileName = route.getString("image");
            try (InputStream is = getAssets().open(imageFileName)) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);
            }

            // Bildklick → ZoomActivity starten
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(RouteDetailActivity.this, ImageZoomActivity.class);
                intent.putExtra("image", imageFileName);
                startActivity(intent);
            });

        } catch (JSONException e) {
            Log.e("RouteDetailActivity", "Fehler beim Parsen des JSON-Objekts", e);
            Toast.makeText(this, "Fehler beim Laden der Route-Daten", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("RouteDetailActivity", "Fehler beim Laden des Bildes aus den Assets", e);
            Toast.makeText(this, "Fehler beim Laden des Routenbildes", Toast.LENGTH_SHORT).show();
        }

        // Zurück-Button
        backFab.setOnClickListener(v -> finish());
    }
}
