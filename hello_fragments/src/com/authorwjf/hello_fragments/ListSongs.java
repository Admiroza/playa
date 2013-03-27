package com.authorwjf.hello_fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.authorwjf.hello_fragments.helper.AlertDialogManager;
import com.authorwjf.hello_fragments.helper.ConnectionDetector;
import com.authorwjf.hello_fragments.helper.JsonParser;
import com.authorwjf.hello_fragments.ListSongs;
import com.authorwjf.hello_fragments.R;
import com.authorwjf.hello_fragments.ListSongs.LoadTracks;
 
import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ListSongs extends ListFragment{
	// Connection detector
		ConnectionDetector cd;
		
		// Alert dialog manager
		AlertDialogManager alert = new AlertDialogManager();
		
		// Progress Dialog
		private ProgressDialog pDialog;

		// Creating JSON Parser object
		JsonParser jsonParser = new JsonParser();

		ArrayList<HashMap<String, String>> tracksList;

		// tracks JSONArray
		JSONArray albums = null;
		
		// Album id
		String album_id, album_name;

		// tracks JSON url
		// id - should be posted as GET params to get track list (ex: id = 5)
		private static final String URL_ALBUMS = "http://api.androidhive.info/songs/album_tracks.php";

		// ALL JSON node names
		private static final String TAG_SONGS = "songs";
		private static final String TAG_ID = "id";
		private static final String TAG_NAME = "name";
		private static final String TAG_ALBUM = "album";
		private static final String TAG_DURATION = "duration";
		
		private Button buttonPlayStop;
		private MediaPlayer mediaPlayer;  
		private final Handler handler = new Handler();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Hashmap for ListView
				tracksList = new ArrayList<HashMap<String, String>>();

				// Loading tracks in Background Thread
				new LoadTracks().execute();
				
		//String[] values = new String[] { "Enterprise", "Star Trek", "Next Generation", "Deep Space 9", "Voyager"};
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, values);
		//setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		DetailFrag frag = (DetailFrag) getFragmentManager().findFragmentById(R.id.frag_capt);
		if (frag != null && frag.isInLayout()) {
			frag.setText(getCapt(item));
		}
	}
	
	private String getCapt(String ship) {
		if (ship.toLowerCase().contains("enterprise")) {
			return "Johnathan Archer";
		}
		if (ship.toLowerCase().contains("star trek")) {
			return "James T. Kirk";
		}
		if (ship.toLowerCase().contains("next generation")) {
			return "Jean-Luc Picard";
		}
		if (ship.toLowerCase().contains("deep space 9")) {
			return "Benjamin Sisko";
		}
		if (ship.toLowerCase().contains("voyager")) {
			return "Kathryn Janeway";
		}
		return "???";
	}
	class LoadTracks extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		 

		/**
		 * getting tracks json and parsing
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			// post album id as GET parameter
			params.add(new BasicNameValuePair(TAG_ID, "1"));

			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
					params);

			// Check your log cat for JSON reponse
			Log.d("Track List JSON: ", json);

			try {
				JSONObject jObj = new JSONObject(json);
				if (jObj != null) {
					String album_id = jObj.getString(TAG_ID);
					album_name = jObj.getString(TAG_ALBUM);
					albums = jObj.getJSONArray(TAG_SONGS);

					if (albums != null) {
						// looping through All songs
						for (int i = 0; i < albums.length(); i++) {
							JSONObject c = albums.getJSONObject(i);

							// Storing each json item in variable
							String song_id = c.getString(TAG_ID);
							// track no - increment i value
							String track_no = String.valueOf(i + 1);
							String name = c.getString(TAG_NAME);
							String duration = c.getString(TAG_DURATION);

							// creating new HashMap
							HashMap<String, String> map = new HashMap<String, String>();

							// adding each child node to HashMap key => value
							map.put("album_id", album_id);
							map.put(TAG_ID, song_id);
							map.put("track_no", track_no + ".");
							map.put(TAG_NAME, name);
							map.put(TAG_DURATION, duration);

							// adding HashList to ArrayList
							tracksList.add(map);
						}
					} else {
						Log.d("Albums: ", "null");
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all tracks
			Activity act = getActivity();
			
			// updating UI from Background Thread
			act.runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							getActivity(), tracksList,
							R.layout.list_tracks, new String[] { "album_id", TAG_ID, "track_no",
									TAG_NAME, TAG_DURATION }, new int[] {
									R.id.album_id, R.id.song_id, R.id.track_no, R.id.album_name, R.id.song_duration });
					// updating listview
					setListAdapter(adapter);
					
					// Change Activity Title with Album name
					 
				}
			});

		}

	}
}
