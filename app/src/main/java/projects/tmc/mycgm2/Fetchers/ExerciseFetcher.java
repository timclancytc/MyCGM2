package projects.tmc.mycgm2.Fetchers;

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
import projects.tmc.mycgm2.Items.EventItem;

public class ExerciseFetcher {

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
                    exerciseParser(items, jsonBody);
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void exerciseParser(List<EventItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        JSONArray exerciseJsonArray = jsonBody.getJSONArray("events");
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);

        for (int i = 0; i < exerciseJsonArray.length(); i++) {
            JSONObject exerciseJsonObject = exerciseJsonArray.getJSONObject(i);

            EventItem item = new EventItem();

            String systemDateString = exerciseJsonObject.getString("systemTime");
            Date date = simpleDateFormat.parse(systemDateString);
            item.setSystemTime(date);

            //Display date is saved as the current time on the system when the reading is entered
            //There is therefore no way to convert it, since it can't be known which timezone it was
            //entered in...
            String displayDateString = exerciseJsonObject.getString("displayTime");
            date = simpleDateFormat.parse(displayDateString);
            item.setDisplayTime(date);
            item.setEventType(exerciseJsonObject.getString("eventType"));
            item.setEventSubType(exerciseJsonObject.getString("eventSubType"));
            item.setUnit(exerciseJsonObject.getString("unit"));
            String valueString = exerciseJsonObject.getString("value");
            item.setValue(!valueString.equals("null") ? Integer.valueOf(valueString) : 0);

            if (item.getEventType().equals("exercise")) {
                items.add(item);
            }
        }
    }
}

