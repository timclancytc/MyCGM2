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
import projects.tmc.mycgm2.Items.EGVSItem;

public class EGVSFetcher {

    public List<EGVSItem> fetchItems(Request request) {
        List<EGVSItem> items = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response != null) {
                //If status is OK 200
                if (response.isSuccessful()) {
                    String result = Objects.requireNonNull(response.body()).string();

                    JSONObject jsonBody = new JSONObject(result);
                    egvsParser(items, jsonBody);
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void egvsParser(List<EGVSItem> items, JSONObject jsonBody)
            throws JSONException, ParseException {

        JSONArray egvsJsonArray = jsonBody.getJSONArray("egvs");
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);

        for (int i = 0; i < egvsJsonArray.length(); i++) {
            JSONObject egvsJsonObject = egvsJsonArray.getJSONObject(i);

            EGVSItem item = new EGVSItem();

            String systemDateString = egvsJsonObject.getString("systemTime");
            Date date = simpleDateFormat.parse(systemDateString);
            item.setSystemTime(date);

            //Display date is saved as the current time on the system when the reading is entered
            //There is therefore no way to convert it, since it can't be known which timezone it was
            //entered in...
            String displayDateString = egvsJsonObject.getString("displayTime");
            date = simpleDateFormat.parse(displayDateString);
            item.setDisplayTime(date);

            item.setValue(egvsJsonObject.getInt("value"));

            item.setStatus(egvsJsonObject.getString("status"));
            item.setTrend(egvsJsonObject.getString("trend"));
            item.setValue(egvsJsonObject.getInt("trendRate"));

            items.add(item);
        }
    }
}

