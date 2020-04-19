package com.example.parkmapproject.userrating;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PlacesAPI {
    private static String urlString;
    private final static String ERROR_TAG = "PlacesAPI";
    private JSONObject jsonObject;

    public PlacesAPI() {}
    public PlacesAPI(String placeId, String apiKey) throws IOException, JSONException {
        this.urlString = "https://maps.googleapis.com/maps/api/place/details/json?placeid="
                + placeId + "&key=" + apiKey;
        jsonObject = readJsonFromUrl();
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
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            String json = readAll(reader);
            reader.close();
            in.close();
            return new JSONObject(json);
        } finally {
            connection.disconnect();
        }
    }

    public ArrayList<UserRating> getUserRatings() throws IOException, JSONException {
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray userRatings = result.getJSONArray("reviews");
        ArrayList<UserRating> mUserRatings = new ArrayList<>();
        if (userRatings != null) {
            for (int i = 0; i < userRatings.length(); ++i) {
                JSONObject rating = (JSONObject)userRatings.get(i);
                String name = rating.getString("author_name");
                String comment = rating.getString("text");
                int ratings = rating.getInt("rating");
                long timestamp = rating.getLong("time");
                mUserRatings.add(new UserRating(name, comment, ratings, timestamp));
            }
        }
        return mUserRatings;
    }
}
