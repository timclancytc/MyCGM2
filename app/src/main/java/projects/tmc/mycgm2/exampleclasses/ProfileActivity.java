package projects.tmc.mycgm2.exampleclasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ProfileActivity extends Activity {

//	private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
//	private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
//	private static final String QUESTION_MARK = "?";
//	private static final String EQUALS = "=";
//
//	private TextView welcomeText;
//	private ProgressDialog pd;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_profile);
//		welcomeText = (TextView) findViewById(R.id.activity_profile_welcome_text);
//
//		//Request basic profile of the user
//		SharedPreferences preferences = this.getSharedPreferences("user_info", 0);
//		String accessToken = preferences.getString("accessToken", null);
//		if(accessToken!=null){
//			String profileUrl = getProfileUrl(accessToken);
//			new GetProfileRequestAsyncTask().execute(profileUrl);
//		}
//	}
//
//	private static final String getProfileUrl(String accessToken){
//		return PROFILE_URL
//				+QUESTION_MARK
//				+OAUTH_ACCESS_TOKEN_PARAM+EQUALS+accessToken;
//	}
//
//	private class GetProfileRequestAsyncTask extends AsyncTask<String, Void, JSONObject> {
//
//		@Override
//		protected void onPreExecute(){
//			pd = ProgressDialog.show(ProfileActivity.this, "", ProfileActivity.this.getString(R.string.loading),true);
//		}
//
//		@Override
//		protected JSONObject doInBackground(String... urls) {
//			if(urls.length>0){
//				String url = urls[0];
//				HttpClient httpClient = new DefaultHttpClient();
//				HttpGet httpget = new HttpGet(url);
//				httpget.setHeader("x-li-format", "json");
//				try{
//					HttpResponse response = httpClient.execute(httpget);
//					if(response!=null){
//						//If status is OK 200
//						if(response.getStatusLine().getStatusCode()==200){
//							String result = EntityUtils.toString(response.getEntity());
//							//Convert the string result to a JSON Object
//							return new JSONObject(result);
//						}
//					}
//				}catch(IOException e){
//					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
//				} catch (JSONException e) {
//					Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
//				}
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(JSONObject data){
//			if(pd!=null && pd.isShowing()){
//				pd.dismiss();
//			}
//			if(data!=null){
//
//				try {
//					String welcomeTextString = String.format(getString(R.string.welcome_text),data.getString("firstName"),data.getString("lastName"),data.getString("headline"));
//					welcomeText.setText(welcomeTextString);
//				} catch (JSONException e) {
//					Log.e("Authorize","Error Parsing json "+e.getLocalizedMessage());
//				}
//			}
//		}
//
//
//	};
}
