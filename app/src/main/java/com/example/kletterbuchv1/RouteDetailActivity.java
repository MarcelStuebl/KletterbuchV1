package com.example.kletterbuchv1;

import com.bumptech.glide.Glide;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;


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

            // Bild vom Server laden (anstatt aus Assets)
            String baseUrl = "http://192.168.1.51:8080/images/";
            String imagePath = route.getString("image");

// Falls imagePath schon mit "img/" beginnt, entferne das oder korrigiere hier:
            if (imagePath.startsWith("img/")) {
                imagePath = imagePath.substring(4); // entfernt "img/"
            }

            String imageUrl = baseUrl + imagePath;

            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);

            // Bildklick → ZoomActivity starten
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(RouteDetailActivity.this, ImageZoomActivity.class);
                intent.putExtra("image", imageUrl);  // Übergib URL statt Dateiname
                startActivity(intent);
            });

        } catch (JSONException e) {
            Log.e("RouteDetailActivity", "Fehler beim Parsen des JSON-Objekts", e);
            Toast.makeText(this, "Fehler beim Laden der Route-Daten", Toast.LENGTH_SHORT).show();
        }

        // Zurück-Button
        backFab.setOnClickListener(v -> finish());
    }
}
