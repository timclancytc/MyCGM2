package projects.tmc.mycgm2;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CalibrationFetcher {

    private static final String TAG = "CalibrationFetcher";
    private static final String API_KEY = "59b41311fbcccd7521629930b8f86282";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public List<CalibrationItem> fetchItems() {
        List<CalibrationItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.search")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("tags", "fujifilm")
                    .appendQueryParameter("has_geo", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s,geo")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return items;
    }

    private String getUrlString(String url) throws IOException {
        return new String(getUrlBytes(url));
    }

    public void parseItems(List<CalibrationItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject calibrationJsonObject = photoJsonArray.getJSONObject(i);
            CalibrationItem item = new CalibrationItem();
            String systemDateString = calibrationJsonObject.getString("systemTime");
            item.setSystemTime(DateFormat.getDateInstance().parse(systemDateString));
            String displayDateString = calibrationJsonObject.getString("displayTime");
            item.setDisplayTime(DateFormat.getDateInstance().parse(displayDateString));
            item.setUnit(calibrationJsonObject.getString("unit"));
            item.setValue(calibrationJsonObject.getInt("value"));

            items.add(item);

        }
    }
}
