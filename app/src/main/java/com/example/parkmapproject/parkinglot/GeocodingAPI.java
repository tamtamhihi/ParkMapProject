package com.example.parkmapproject.parkinglot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GeocodingAPI {
    private static String urlString;
    private final static String ERROR_TAG = "GeocodingAPI";

    public GeocodingAPI() {}

    public GeocodingAPI(float latitude, float longitude, String apiKey) throws MalformedURLException {
        urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + String.valueOf(latitude) + ","
                + String.valueOf(longitude)
                + "&key=" + apiKey;
    }

    private static String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line + "\n");
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl() throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream in = connection.getInputStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String json = readAll(reader);
            reader.close();
            in.close();
            return new JSONObject(json);
        } finally {
            connection.disconnect();
        }
    }

    public ArrayList<String> getPlaceId() throws IOException, JSONException {
        JSONObject jsonObject = readJsonFromUrl();
        JSONArray results = jsonObject.getJSONArray("results");
        ArrayList<String> possibleIds = new ArrayList<>();
        for (int i = 0; i < results.length(); ++i) {
            String placeId = ((JSONObject)results.get(i)).getString("place_id");
            if (placeId != null)
                possibleIds.add(placeId);
        }
        return possibleIds;
    }
}
