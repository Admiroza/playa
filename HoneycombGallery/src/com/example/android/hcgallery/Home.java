
package com.example.android.hcgallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.android.hcgallery.helper.AlertDialogManager;
import com.example.android.hcgallery.helper.ConnectionDetector;
import com.example.android.hcgallery.helper.JsonParser;
 
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/** This is the main "launcher" activity.
 * When running on a "large" or larger screen, this activity displays both the
 * TitlesFragments and the Content Fragment. When on a smaller screen size, this
 * activity displays only the TitlesFragment. In which case, selecting a list
 * item opens the ContentActivity, holds only the ContentFragment. */
public class Home extends Activity {
	// Connection detector
	ConnectionDetector cd;
	
	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	
	// Progress Dialog
	private ProgressDialog pDialog;
	private ListView m_listview;
	// Creating JSON Parser object
	JsonParser jsonParser = new JsonParser();

	ArrayList<HashMap<String, String>> albumsList;

	String album_id;
	// albums JSON url
	private static final String URL_ALBUMS = "http://platinumplaya.co.za/ajax/getAlbum";

	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "title";
	 
 
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    cd = new ConnectionDetector(getApplicationContext());
	 
    // Check for internet connection
    if (!cd.isConnectingToInternet()) {
        // Internet Connection is not present
        alert.showAlertDialog(this, "Internet Connection Error",
                "Please connect to working Internet connection", false);
        // stop executing code by return
        return;
    }

		
			albumsList = new ArrayList<HashMap<String, String>>();
			 
			// Loading Albums JSON in Background Thread
			new LoadAlbums().execute(); 
			m_listview = (ListView) findViewById(R.id.list);
	
			m_listview.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {


			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				// on selecting a single album
				// TrackListActivity will be launched to show tracks inside the album
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				
				// send album id to tracklist activity to get list of songs under that album
				  album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
				i.putExtra("album_id", album_id);				
				
				startActivity(i);
			}
		});	
    
}
/**
 * Background Async Task to Load all Albums by making http request
 * */
class LoadAlbums extends AsyncTask<String, String, String> {

	/**
	 * Before starting background thread Show Progress Dialog
	 * */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pDialog = new ProgressDialog(Home.this);
		pDialog.setMessage("Listing Albums ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	/**
	 * getting Albums JSON
	 * */
	protected String doInBackground(String... args) {
		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		// getting JSON string from URL
		String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
				params);

		// Check your log cat for JSON reponse
		Log.d("Albums JSON: ", "> " + json);

		try {		
			JSONObject jsonObj = new JSONObject(json);
		    //JSONArray albums =  new  JSONArray
		    
			// looping through All albums
			for (int i = 0; i < 2; i++) {
				//JSONObject c = jsonObj.getJSONObject(i);
				album_id = jsonObj.getJSONObject("albums").getString("id");
				String id = jsonObj.getJSONObject("albums").getString("id");
				String name = jsonObj.getJSONObject("albums").getString("title");
				 

			
				HashMap<String, String> map = new HashMap<String, String>();

				
				map.put(TAG_ID, id);
				map.put(TAG_NAME, name);
				 

				albumsList.add(map);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	protected void onPostExecute(String file_url) {
		pDialog.dismiss();
		runOnUiThread(new Runnable() {
			public void run() {
				 
				m_listview = (ListView) findViewById(R.id.list);
				ListAdapter adapter = new SimpleAdapter(
						Home.this, albumsList,
						R.layout.list_albums, new String[] { TAG_ID,
								TAG_NAME, }, new int[] {
								R.id.album_id, R.id.album_name, });
				
				 
				 m_listview.setAdapter(adapter);
			}
		});
	}

}



}
