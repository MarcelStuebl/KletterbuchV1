package com.example.kletterbuchv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> mountainNames = new ArrayList<>();
    ArrayList<JSONObject> mountainObjects = new ArrayList<>();
    ListView mountainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mountainListView = findViewById(R.id.mountainListView);
        loadMountains();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mountainNames);
        mountainListView.setAdapter(adapter);

        mountainListView.setOnItemClickListener((adapterView, view, position, l) -> {
            JSONObject selectedMountain = mountainObjects.get(position);
            Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
            intent.putExtra("mountain", selectedMountain.toString());
            startActivity(intent);
        });

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Kletterrouten App");

    }

    private void loadMountains() {
        try {
            String json = loadJSONFromAsset();
            JSONObject obj = new JSONObject(json);
            JSONArray mountainsArray = obj.getJSONArray("mountains");
            for (int i = 0; i < mountainsArray.length(); i++) {
                JSONObject mountain = mountainsArray.getJSONObject(i);
                mountainNames.add(mountain.getString("name"));
                mountainObjects.add(mountain);
            }
        } catch (JSONException e) {
            Log.e("RouteDetailActivity", "Fehler beim Parsen des JSON-Objekts", e);
            Toast.makeText(this, "Fehler beim Laden der Route-Daten", Toast.LENGTH_SHORT).show();
        }
    }

    private String loadJSONFromAsset() {
        try (InputStream is = getAssets().open("data.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException ex) {
            Log.e("AssetLoader", "Fehler beim Lesen der JSON-Datei", ex);
            return null;
        }
    }

}
