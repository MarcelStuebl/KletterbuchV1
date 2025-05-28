package com.example.kletterbuchv1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class RouteDetailActivity extends AppCompatActivity {

    TextView nameView, difficultyView, lengthView, descriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        nameView = findViewById(R.id.nameView);
        difficultyView = findViewById(R.id.difficultyView);
        lengthView = findViewById(R.id.lengthView);
        descriptionView = findViewById(R.id.descriptionView);

        String routeJson = getIntent().getStringExtra("route");
        try {
            JSONObject route = new JSONObject(routeJson);
            nameView.setText(route.getString("name"));
            difficultyView.setText("Schwierigkeit: " + route.getString("difficulty"));
            lengthView.setText("Länge: " + route.getString("length"));
            descriptionView.setText(route.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
