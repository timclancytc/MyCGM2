package projects.tmc.mycgm2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CalibrationParsingTest {
    private static final String TAG = "CalibrationParsingTest";

    @Test
    public void parseCalibrationsTest() {
        CalibrationFetcher cf = new CalibrationFetcher();
        List<CalibrationItem> items = new ArrayList<>();

        try {
            InputStream in =
                    CalibrationParsingTest.class.getResourceAsStream("/json/calibrations.json");
            String testString = getString(in);

            JSONObject jsonBodyTest = new JSONObject(testString);

            cf.calibrationsParser(items, jsonBodyTest);
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }

        assertEquals(2, items.size());
    }

    private String getString(InputStream in) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                sb.append(line);
            }

        } catch (IOException ioe) {
            System.out.println(TAG + ": IO Exception");
        }

        return sb.toString();
    }
}
