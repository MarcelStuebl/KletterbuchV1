package com.example.kletterbuchv1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_URL = "http://192.168.1.51:8080/data/data.json"; // ⚠️ Ersetze mit deinem lokalen Server!
    private static final String DATA_FILE_NAME = "data.json";

    private final ArrayList<String> mountainNames = new ArrayList<>();
    private final ArrayList<JSONObject> mountainObjects = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mountainListView = findViewById(R.id.mountainListView);
        Button downloadButton = findViewById(R.id.downloadButton);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Kletterrouten App");

        // Adapter für ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mountainNames);
        mountainListView.setAdapter(adapter);

        // Klick auf Liste: Routen anzeigen
        mountainListView.setOnItemClickListener((adapterView, view, position, l) -> {
            JSONObject selectedMountain = mountainObjects.get(position);
            Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
            intent.putExtra("mountain", selectedMountain.toString());
            startActivity(intent);
        });

        // Klick auf Download-Button
        downloadButton.setOnClickListener(v -> downloadData());
    }

    private void downloadData() {
        new Thread(() -> {
            try {
                URL url = new URL(DATA_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    throw new IOException("HTTP error code: " + conn.getResponseCode());
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                // Speichern im internen Speicher
                String jsonData = builder.toString();
                try (FileOutputStream fos = openFileOutput(DATA_FILE_NAME, MODE_PRIVATE)) {
                    fos.write(jsonData.getBytes(StandardCharsets.UTF_8));
                }

                // UI-Update
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Daten erfolgreich heruntergeladen", Toast.LENGTH_SHORT).show();
                    loadMountains();
                });

            } catch (IOException e) {
                Log.e("Download", "Fehler beim Herunterladen", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Fehler beim Herunterladen der Daten", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void loadMountains() {
        try {
            mountainNames.clear();
            mountainObjects.clear();

            String json = loadJSONFromInternalStorage();
            if (json == null) {
                Toast.makeText(this, "Keine Daten vorhanden", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject obj = new JSONObject(json);
            JSONArray mountainsArray = obj.getJSONArray("mountains");

            for (int i = 0; i < mountainsArray.length(); i++) {
                JSONObject mountain = mountainsArray.getJSONObject(i);
                mountainNames.add(mountain.getString("name"));
                mountainObjects.add(mountain);
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("MainActivity", "Fehler beim Parsen des JSON-Objekts", e);
            Toast.makeText(this, "Fehler beim Laden der Daten", Toast.LENGTH_SHORT).show();
        }
    }

    private String loadJSONFromInternalStorage() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                openFileInput(DATA_FILE_NAME), StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();

        } catch (IOException ex) {
            Log.e("FileRead", "Fehler beim Lesen der JSON-Datei", ex);
            return null;
        }
    }
}
