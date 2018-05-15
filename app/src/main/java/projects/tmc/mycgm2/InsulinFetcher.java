package projects.tmc.mycgm2;

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

class InsulinFetcher {

    public List<EventItem> fetchItems(Request request) {
        List<EventItem> items = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response != null) {
                //If status is OK 200
                if (response.isSuccessful()) {
                    String result = Objects.requireNonNull(response.body()).string();

                    JSONObject jsonBody = new JSONObject(result);
                    insulinParser(items, jsonBody);
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void insulinParser(List<EventItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        JSONArray insulinJsonArray = jsonBody.getJSONArray("events");
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);

        for (int i = 0; i < insulinJsonArray.length(); i++) {
            JSONObject insulinJsonObject = insulinJsonArray.getJSONObject(i);

            EventItem item = new EventItem();

            String systemDateString = insulinJsonObject.getString("systemTime");
            Date date = simpleDateFormat.parse(systemDateString);
            item.setSystemTime(date);

            //Display date is saved as the current time on the system when the reading is entered
            //There is therefore no way to convert it, since it can't be known which timezone it was
            //entered in...
            String displayDateString = insulinJsonObject.getString("displayTime");
            date = simpleDateFormat.parse(displayDateString);
            item.setDisplayTime(date);
            item.setEventType(insulinJsonObject.getString("eventType"));
            item.setEventSubType(insulinJsonObject.getString("eventSubType"));
            item.setUnit(insulinJsonObject.getString("unit"));
            item.setValue(insulinJsonObject.getInt("value"));

            if (item.getEventType().equals("insulin")) {
                items.add(item);
            }
        }
    }
}

