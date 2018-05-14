package projects.tmc.mycgm2;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class CarbFetcher {

    private static final String TAG = "CarbFetcher";

    public List<EventItem> fetchItems(Request request) {
        List<EventItem> items = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response != null) {
                //If status is OK 200
                if (response.isSuccessful()) {
                    String result = Objects.requireNonNull(response.body()).string();

                    Log.i(TAG, "resultString in try: " + result);
                    JSONObject jsonBody = new JSONObject(result);
                    carbParser(items, jsonBody);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception:" + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            Log.e(TAG, "Parse Exception " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return items;
    }

    public void carbParser(List<EventItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        Log.i(TAG, "JSON String:" + jsonBody);

        JSONArray carbJsonArray = jsonBody.getJSONArray("events");
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);

        for (int i = 0; i < carbJsonArray.length(); i++) {
            JSONObject carbJsonObject = carbJsonArray.getJSONObject(i);

            EventItem item = new EventItem();

            String systemDateString = carbJsonObject.getString("systemTime");
            Date date = simpleDateFormat.parse(systemDateString);
            item.setSystemTime(date);

            //Display date is saved as the current time on the system when the reading is entered
            //There is therefore no way to convert it, since it can't be known which timezone it was
            //entered in...
            String displayDateString = carbJsonObject.getString("displayTime");
            date = simpleDateFormat.parse(displayDateString);
            item.setDisplayTime(date);

            item.setEventType(carbJsonObject.getString("eventType"));
            item.setEventSubType(carbJsonObject.getString("eventSubType"));
            item.setValue(carbJsonObject.getInt("value"));
            item.setUnit(carbJsonObject.getString("unit"));


            if (item.getEventType().equals("carbs")) {
                items.add(item);
            }

        }
    }
}

