package com.example.kletterbuchv1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

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

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        // Zurück-Button
        backFab.setOnClickListener(v -> finish());
    }
}
