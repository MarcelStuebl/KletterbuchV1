package com.example.kletterbuchv1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RouteListActivity extends AppCompatActivity {

    ArrayList<String> routeNames = new ArrayList<>();
    ArrayList<JSONObject> routeObjects = new ArrayList<>();
    ListView routeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        routeListView = findViewById(R.id.routeListView);

        String mountainJson = getIntent().getStringExtra("mountain");
        try {
            JSONObject mountain = new JSONObject(mountainJson);
            JSONArray routesArray = mountain.getJSONArray("routes");
            for (int i = 0; i < routesArray.length(); i++) {
                JSONObject route = routesArray.getJSONObject(i);
                routeNames.add(route.getString("name"));
                routeObjects.add(route);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routeNames);
        routeListView.setAdapter(adapter);

        routeListView.setOnItemClickListener((adapterView, view, position, l) -> {
            JSONObject selectedRoute = routeObjects.get(position);
            Intent intent = new Intent(RouteListActivity.this, RouteDetailActivity.class);
            intent.putExtra("route", selectedRoute.toString());
            startActivity(intent);
        });
    }
}
