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
import android.util.Log;
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

    //Tag for logs
    private static final String TAG = "AuthActivity";

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

    private static final String GRANT_TYPE_PARAM = "grant_type";
    private static final String GRANT_TYPE = "authorization_code";


    /*---------------------------------------*/
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";

    private WebView webView;
    private ProgressDialog pd;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Log.i(TAG, "onCreate");
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
                    Log.i(TAG, "Authorize/Authorization starts with redirect_uri");
                    Log.i(TAG, "Authorize/authorizationUrl:" + authorizationUrl);
                    Uri uri = Uri.parse(authorizationUrl);
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(STATE)) {
                        Log.e(TAG, "Authorize/State token doesn't match");
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: " + authorizationToken);

                    //Generate URL for requesting Access Token
                    Request accessTokenRequest = getAccessTokenRequest(authorizationToken);

                    Log.i(TAG, "Authorize/accessTokenRequest: " + accessTokenRequest.toString());
                    //We make the request in a AsyncTask
                    new PostRequestAsyncTask().execute(accessTokenRequest);

                } else {
                    //Default behaviour
                    Log.i("Authorize", "Redirecting to: " + authorizationUrl);
                    webView.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        //Get the authorization Url
        String authUrl = getAuthorizationUrl();
        Log.i("Authorize", "Loading Auth Url: " + authUrl);
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl);
    }

    /**
     * Method that generates the url for get the access token from the Service
     *
     * @return Url
     */
    private static String getAccessTokenUrl(String authorizationToken) {
        return ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + API_KEY
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + REDIRECT_URI
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + SECRET_KEY;
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

    private class PostRequestAsyncTask extends AsyncTask<Request, Void, Boolean> {



        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(AuthActivity.this, "", AuthActivity.this.getString(R.string.loading), true);
        }

        @Override
        protected Boolean doInBackground(Request... requests) {
            Log.i(TAG, "PostRequestAsyncTask/doInBackground");

            Request request = requests[0];

            Log.i(TAG, "Request: " + request.toString());

            OkHttpClient httpClient = new OkHttpClient();

            try {
                Response response = httpClient.newCall(request).execute();
                if (response != null) {
                    //If status is OK 200
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Authorize/response is successful");
                        Log.i(TAG, "Authorize/response is successful second");

                        String result = response.body().string();

                        Log.i(TAG, "Authorize/result: " + result);
                        //Convert the string result to a JSON Object
                        JSONObject resultJson = new JSONObject(result);
                        //Extract data from JSON Response
                        int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
                        String accessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;

                        if (expiresIn > 0 && accessToken != null) {
                            Log.i("Authorize", "This is the access Token: " + accessToken + ". It will expires in " + expiresIn + " secs");

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
            } catch (IOException e) {
                Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
            } catch (ParseException e) {
                Log.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e("Authorize", "Error with Http response " + e.getLocalizedMessage());
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
