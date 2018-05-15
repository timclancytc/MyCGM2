package projects.tmc.mycgm2;

import android.annotation.SuppressLint;
import android.net.ParseException;
import android.support.v7.app.AppCompatActivity;


import java.io.IOException;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {

    /*CONSTANT FOR THE AUTHORIZATION PROCESS*/

    /****FILL THIS WITH YOUR INFORMATION*********/
    //This is the public api key of our application
    private static final String API_KEY = "JolxaxR4NTAVkDfaPrEKwU4b7i2n9GX4";
    //This is the private api key of our application
    private static final String SECRET_KEY = "Wp4lYJv4Ad32hfNu";
    //This is any string we want to use. This will be used for avoid CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private static final String STATE = "E3ZYKC1T6H2yP4z";
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
    //We use a made up url that we will intercept when redirecting. Avoid Uppercases.
    private static final String REDIRECT_URI = "http://projects.tmc.mycgm.redirecturl";
    /*********************************************/

    //These are constants used to build the urls
    private static final String AUTHORIZATION_URL = "https://api.dexcom.com/v1/oauth2/login";
    private static final String ACCESS_TOKEN_URL = "https://api.dexcom.com/v1/oauth2/token";
    private static final String SECRET_KEY_PARAM = "client_secret";

    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String SCOPE_PARAM = "scope";
    private static final String STATE_PARAM = "state";

    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String SCOPE_VALUE = "offline_access";


    /*---------------------------------------*/
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";

    private WebView webView;
    private ProgressDialog pd;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        //get the webView from the layout
        webView = findViewById(R.id.main_activity_web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        //Request focus for the webview
        webView.requestFocus(View.FOCUS_DOWN);

        //Show a progress dialog to the user
        pd = ProgressDialog.show(this, "", this.getString(R.string.loading), true);

        //Set a custom web view client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
                //This method will be called when the Auth proccess redirect to our RedirectUri.
                //We will check the url looking for our RedirectUri.
                if (authorizationUrl.startsWith(REDIRECT_URI)) {
                    Uri uri = Uri.parse(authorizationUrl);
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        return true;
                    }

                    //Generate URL for requesting Access Token
                    Request accessTokenRequest = getAccessTokenRequest(authorizationToken);

                    //We make the request in a AsyncTask
                    new PostRequestAsyncTask().execute(accessTokenRequest);

                } else {
                    //Default behaviour
                    webView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        //Get the authorization Url
        String authUrl = getAuthorizationUrl();
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl);
    }

    /**
     * Method that generates the url for get the access token from the Service
     *
     * @return Url
     */
    private static Request getAccessTokenRequest(String authorizationToken) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String bodyString = SECRET_KEY_PARAM + EQUALS + SECRET_KEY +
                AMPERSAND + CLIENT_ID_PARAM + EQUALS + API_KEY +
                AMPERSAND + "code=" + authorizationToken +
                AMPERSAND + "grant_type=authorization_code" +
                AMPERSAND + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI;


        RequestBody body = RequestBody.create(mediaType, bodyString);
        return new Request.Builder()
                .url(ACCESS_TOKEN_URL)
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();
    }

    /**
     * Method that generates the url for get the authorization token from the Service
     *
     * @return Url
     */
    private static String getAuthorizationUrl() {
        return AUTHORIZATION_URL
                + QUESTION_MARK + CLIENT_ID_PARAM + EQUALS + API_KEY
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + SCOPE_PARAM + EQUALS + SCOPE_VALUE
                + AMPERSAND + STATE_PARAM + EQUALS + STATE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class PostRequestAsyncTask extends AsyncTask<Request, Void, Boolean> {



        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(AuthActivity.this, "", AuthActivity.this.getString(R.string.loading), true);
        }

        @Override
        protected Boolean doInBackground(Request... requests) {

            Request request = requests[0];

            OkHttpClient httpClient = new OkHttpClient();

            try {
                Response response = httpClient.newCall(request).execute();
                if (response != null) {
                    //If status is OK 200
                    if (response.isSuccessful()) {

                        //noinspection ConstantConditions
                        String result = response.body().string();

                        //Convert the string result to a JSON Object
                        JSONObject resultJson = new JSONObject(result);
                        //Extract data from JSON Response
                        int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
                        String accessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;

                        if (expiresIn > 0 && accessToken != null) {

                            //Calculate date of expiration
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.SECOND, expiresIn);
                            long expireDate = calendar.getTimeInMillis();

                            ////Store both expires in and access token in shared preferences
                            SharedPreferences preferences = AuthActivity.this.getSharedPreferences("user_info", 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putLong("expires", expireDate);
                            editor.putString("accessToken", accessToken);
                            editor.apply();

                            return true;
                        }
                    }
                }
            } catch (IOException | ParseException | JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (status) {
                //If everything went Ok, change to another activity.
                Intent startMainActivity = new Intent(AuthActivity.this, MainActivity.class);
                AuthActivity.this.startActivity(startMainActivity);
            }
        }

    }
}
