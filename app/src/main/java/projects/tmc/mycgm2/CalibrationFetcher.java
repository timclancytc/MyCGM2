package projects.tmc.mycgm2;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CalibrationFetcher {

    private static final String TAG = "CalibrationFetcher";

    public List<CalibrationItem> fetchItems(Request request) {
        List<CalibrationItem> items = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response != null) {
                //If status is OK 200
                if (response.isSuccessful()) {
                    String result = Objects.requireNonNull(response.body()).string();

                    Log.i(TAG, "resultString in try: " + result);
                    JSONObject jsonBody = new JSONObject(result);
                    calibrationsParser(items, jsonBody);
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


    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void calibrationsParser(List<CalibrationItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        Log.i(TAG, "JSON String:" + jsonBody);

        JSONArray calibrationsJsonArray = jsonBody.getJSONArray("calibrations");

        for (int i = 0; i < calibrationsJsonArray.length(); i++) {
            JSONObject calibrationJsonObject = calibrationsJsonArray.getJSONObject(i);

            CalibrationItem item = new CalibrationItem();

            String systemDateString = calibrationJsonObject.getString("systemTime");
            Instant instant = Instant.parse(systemDateString);
            item.setSystemTime(java.util.Date.from(instant));

            String displayDateString = calibrationJsonObject.getString("displayTime");
            instant = Instant.parse(displayDateString);
            item.setDisplayTime(java.util.Date.from(instant));

            item.setUnit(calibrationJsonObject.getString("unit"));
            item.setValue(calibrationJsonObject.getInt("value"));

            items.add(item);
        }
    }
}

