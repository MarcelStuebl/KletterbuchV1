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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_URL = "http://192.168.1.51:8080/data/data.json";
    private static final String DATA_FILE_NAME = "data.json";

    private final ArrayList<String> mountainNames = new ArrayList<>();
    private final ArrayList<JSONObject> mountainObjects = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mountainListView = findViewById(R.id.mountainListView);
        Button syncButton = findViewById(R.id.downloadButton);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Kletterrouten App");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mountainNames);
        mountainListView.setAdapter(adapter);

        mountainListView.setOnItemClickListener((adapterView, view, position, l) -> {
            JSONObject selectedMountain = mountainObjects.get(position);
            Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
            intent.putExtra("mountain", selectedMountain.toString());
            startActivity(intent);
        });

        syncButton.setOnClickListener(v -> syncDataFromServer());

        loadDataFromLocalStorage();
    }

    private void loadDataFromLocalStorage() {
        String json = readJsonFromFile();

        if (json == null) {
            Toast.makeText(this, "Keine lokalen Daten gefunden. Bitte synchronisieren.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mountainNames.clear();
            mountainObjects.clear();

            JSONObject obj = new JSONObject(json);
            JSONArray mountainsArray = obj.getJSONArray("mountains");

            for (int i = 0; i < mountainsArray.length(); i++) {
                JSONObject mountain = mountainsArray.getJSONObject(i);
                mountainNames.add(mountain.getString("name"));
                mountainObjects.add(mountain);
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("MainActivity", "Fehler beim Parsen von JSON", e);
            Toast.makeText(this, "Ungültige Datenstruktur", Toast.LENGTH_SHORT).show();
        }
    }

    private String readJsonFromFile() {
        File file = new File(getFilesDir(), DATA_FILE_NAME);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException ex) {
            Log.e("MainActivity", "Fehler beim Lesen der Datei", ex);
            return null;
        }
    }

    private void saveJsonToFile(String jsonData) {
        try (FileOutputStream fos = openFileOutput(DATA_FILE_NAME, MODE_PRIVATE)) {
            fos.write(jsonData.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("MainActivity", "Fehler beim Speichern", e);
        }
    }

    private void syncDataFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL(DATA_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    throw new IOException("HTTP-Fehler: " + conn.getResponseCode());
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                String jsonData = builder.toString();

                saveJsonToFile(jsonData);
                downloadAllImages(jsonData);

                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(this, "Daten und Bilder synchronisiert", Toast.LENGTH_SHORT).show();
                    loadDataFromLocalStorage();
                });

            } catch (IOException | JSONException e) {
                Log.e("MainActivity", "Fehler beim Herunterladen", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Fehler beim Herunterladen der Daten", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void downloadAllImages(String jsonData) throws JSONException {
        JSONObject data = new JSONObject(jsonData);
        JSONArray mountains = data.getJSONArray("mountains");

        for (int i = 0; i < mountains.length(); i++) {
            JSONObject mountain = mountains.getJSONObject(i);
            if (mountain.has("image")) downloadImage(mountain.getString("image"));

            if (mountain.has("routes")) {
                JSONArray routes = mountain.getJSONArray("routes");
                for (int j = 0; j < routes.length(); j++) {
                    JSONObject route = routes.getJSONObject(j);
                    if (route.has("image")) downloadImage(route.getString("image"));
                }
            }
        }
    }

    private void downloadImage(String imagePath) {
        try {
            if (imagePath.startsWith("img/")) {
                imagePath = imagePath.substring(4);
            }
            String imageUrl = "http://192.168.1.51:8080/images/" + imagePath;
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream();
                     FileOutputStream fos = openFileOutput(imagePath, MODE_PRIVATE)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Fehler beim Bild-Download: " + imagePath, e);
        }
    }
}
