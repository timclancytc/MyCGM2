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
import projects.tmc.mycgm2.Items.CalibrationItem;

public class CalibrationFetcher {

    public List<CalibrationItem> fetchItems(Request request) {
        List<CalibrationItem> items = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response != null) {
                //If status is OK 200
                if (response.isSuccessful()) {
                    String result = Objects.requireNonNull(response.body()).string();

                    JSONObject jsonBody = new JSONObject(result);
                    calibrationsParser(items, jsonBody);
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void calibrationsParser(List<CalibrationItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        JSONArray calibrationsJsonArray = jsonBody.getJSONArray("calibrations");
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);

        for (int i = 0; i < calibrationsJsonArray.length(); i++) {
            JSONObject calibrationJsonObject = calibrationsJsonArray.getJSONObject(i);

            CalibrationItem item = new CalibrationItem();

            String systemDateString = calibrationJsonObject.getString("systemTime");
            Date date = simpleDateFormat.parse(systemDateString);
            item.setSystemTime(date);

            //Display date is saved as the current time on the system when the reading is entered
            //There is therefore no way to convert it, since it can't be known which timezone it was
            //entered in...
            String displayDateString = calibrationJsonObject.getString("displayTime");
            date = simpleDateFormat.parse(displayDateString);
            item.setDisplayTime(date);

            item.setUnit(calibrationJsonObject.getString("unit"));
            item.setValue(calibrationJsonObject.getInt("value"));

            items.add(item);
        }
    }
}

