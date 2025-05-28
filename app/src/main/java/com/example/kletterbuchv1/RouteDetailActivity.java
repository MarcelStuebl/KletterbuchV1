package com.example.kletterbuchv1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class RouteDetailActivity extends AppCompatActivity {

    TextView difficultyView, lengthView, descriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        difficultyView = findViewById(R.id.difficultyView);
        lengthView = findViewById(R.id.lengthView);
        descriptionView = findViewById(R.id.descriptionView);


        String routeJson = getIntent().getStringExtra("route");
        try {
            JSONObject route = new JSONObject(routeJson);
            difficultyView.setText("Schwierigkeit: " + route.getString("difficulty"));
            lengthView.setText("Länge: " + route.getString("length"));
            descriptionView.setText(route.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageView imageView = findViewById(R.id.routeImageView);

        try {
            JSONObject route = new JSONObject(routeJson);
            difficultyView.setText("Schwierigkeit: " + route.getString("difficulty"));
            lengthView.setText("Länge: " + route.getString("length"));
            descriptionView.setText(route.getString("description"));

            MaterialToolbar toolbar = findViewById(R.id.topAppBar);
            toolbar.setTitle(route.getString("name"));

            // Bild laden (aus assets)
            String imageFileName = route.getString("image");
            InputStream is = getAssets().open(imageFileName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);

            // Klick zum Vergrößern
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(RouteDetailActivity.this, ImageZoomActivity.class);
                intent.putExtra("image", imageFileName);
                startActivity(intent);
            });

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }


        FloatingActionButton backFab = findViewById(R.id.backFab);
        backFab.setOnClickListener(v -> finish());

    }


}
