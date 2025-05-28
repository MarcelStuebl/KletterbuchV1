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

import java.io.IOException;
import java.io.InputStream;
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
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
