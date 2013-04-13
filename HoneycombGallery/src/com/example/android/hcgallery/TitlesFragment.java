 

package com.example.android.hcgallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.android.hcgallery.helper.AlertDialogManager;
import com.example.android.hcgallery.helper.ConnectionDetector;
import com.example.android.hcgallery.helper.JsonParser;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;


public class TitlesFragment extends ListFragment implements ActionBar.TabListener {
	Context context;
    OnItemSelectedListener mListener;
    private int mCategory = 0;
    private int mCurPosition = 0;
    private boolean mDualFragments = false;
    private  String jsonS;
	ConnectionDetector cd;
	AlertDialogManager alert = new AlertDialogManager();
	private ProgressDialog pDialog;
	JsonParser jsonParser = new JsonParser();
	 
	String[] values ;
	String album_id, album_name,title;
	String json;
 
    ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
 
	private static final String URL_TRACKS = "http://api.androidhive.info/songs/album_tracks.php";

	 
	private static final String TAG_ID = "id";
	private static final String TAG_TITLE = "name";
	private static final String TAG_ALBUM = "album_id";
	private static final String TAG_DURATION = "duration";
	 
	 
    public interface OnItemSelectedListener {
        public void onItemSelected(int category, int position,String title);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Check that the container activity has implemented the callback interface
        try {
            mListener = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() 
                    + " must implement OnItemSelectedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent i = getActivity().getIntent();
     //  album_id = i.getStringExtra("album_id");
       album_id = "5";
        new loadTracks().execute();
    
        ContentFragment frag = (ContentFragment) getFragmentManager()
                .findFragmentById(R.id.content_frag);
        if (frag != null) mDualFragments = true;

        ActionBar bar = getActivity().getActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Must call in order to get callback to onCreateOptionsMenu()
        setHasOptionsMenu(true);

       // Directory.initializeDirectory();
        // for (int ji = 0; ji < Directory.getCategoryCount(); ji++) {
        //     bar.addTab(bar.newTab().setText(Directory.getCategory(ji).getName())
        //             .setTabListener(this));
        //  }

        //Current position should survive screen rotations.
        if (savedInstanceState != null) {
            mCategory = savedInstanceState.getInt("category");
            mCurPosition = savedInstanceState.getInt("listPosition");
           
        }

       // populateTitles(mCategory);
        ListView lv = getListView();
       // new LoadTracks().execute();
        lv.setCacheColorHint(Color.TRANSPARENT); // Improves scrolling performance

        if (mDualFragments) {
            // Highlight the currently selected item
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Enable drag and dropping
             
        }

        // If showing both fragments, select the appropriate list item by default
        if (mDualFragments) selectPosition(mCurPosition);

        // Attach a GlobalLayoutListener so that we get a callback when the layout
        // has finished drawing. This is necessary so that we can apply top-margin
        // to the ListView in order to dodge the ActionBar. Ordinarily, that's not
        // necessary, but we've set the ActionBar to "overlay" mode using our theme,
        // so the layout does not account for the action bar position on its own.
        ViewTreeObserver observer = getListView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(layoutListener);
    }

    @Override
    public void onDestroyView() {
      super.onDestroyView();
      // Always detach ViewTreeObserver listeners when the view tears down
      getListView().getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
    }

    /** Attaches an adapter to the fragment's ListView to populate it with items */
    public void populateTitles(int category) {
      //  DirectoryCategory cat = Directory.getCategory(category);
      //  String[] items = new String[cat.getEntryCount()];
      //  for (int i = 0; i < cat.getEntryCount(); i++)
     //       items[i] = cat.getEntry(i).getName();
        // Convenience method to attach an adapter to ListFragment's ListView
     //   setListAdapter(new ArrayAdapter<String>(getActivity(),
              //  R.layout.title_list_item, items));
      //  mCategory = category;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	TextView txt_song_name = (TextView) getView().findViewById(R.id.song_title);
    	 
    	title = ((TextView) v.findViewById(R.id.album_name)).getText().toString();
        mListener.onItemSelected(mCategory, position,title);
        mCurPosition = position;
        
    }

    /** Called to select an item from the listview */
    public void selectPosition(int position) {
        // Only if we're showing both fragments should the item be "highlighted"
        if (mDualFragments) {
            ListView lv = getListView();
            lv.setItemChecked(position, true);
        }
        // Calls the parent activity's implementation of the OnItemSelectedListener
        // so the activity can pass the event to the sibling fragment as appropriate
        mListener.onItemSelected(mCategory, position,title);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("listPosition", mCurPosition);
        outState.putInt("category", mCategory);
     
    }

     

    // Because the fragment doesn't have a reliable callback to notify us when
    // the activity's layout is completely drawn, this OnGlobalLayoutListener provides
    // the necessary callback so we can add top-margin to the ListView in order to dodge
    // the ActionBar. Which is necessary because the ActionBar is in overlay mode, meaning
    // that it will ordinarily sit on top of the activity layout as a top layer and
    // the ActionBar height can vary. Specifically, when on a small/normal size screen,
    // the action bar tabs appear in a second row, making the action bar twice as tall.
    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int barHeight = getActivity().getActionBar().getHeight();
            ListView listView = getListView();
            FrameLayout.LayoutParams params = (LayoutParams) listView.getLayoutParams();
            // The list view top-margin should always match the action bar height
            if (params.topMargin != barHeight) {
                params.topMargin = barHeight;
                listView.setLayoutParams(params);
            }
            // The action bar doesn't update its height when hidden, so make top-margin zero
            if (!getActivity().getActionBar().isShowing()) {
              params.topMargin = 0;
              listView.setLayoutParams(params);
            }
        }
    };


    /* The following are callbacks implemented for the ActionBar.TabListener,
     * which this fragment implements to handle events when tabs are selected.
     */

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        TitlesFragment titleFrag = (TitlesFragment) getFragmentManager()
                .findFragmentById(R.id.titles_frag);
        titleFrag.populateTitles(tab.getPosition());
        
        if (mDualFragments) {
            titleFrag.selectPosition(0);
        }
    }

    /* These must be implemented, but we don't use them */
    
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
    
    class loadTracks extends AsyncTask<String, String, String> {

 
        @Override
        protected void onPreExecute() {
             super.onPreExecute();
             
        }
 
        protected String doInBackground(String... param) {
         
        List<NameValuePair> params = new ArrayList<NameValuePair>();     			
        params.add(new BasicNameValuePair(TAG_ID, album_id));
          jsonS = jsonParser.makeHttpRequest(URL_TRACKS, "GET", params);
         
            Log.d("All tracks: ", jsonS);
             
            try {
               
             
            	JSONObject jObj = new JSONObject(jsonS);
				if (jObj != null) {
					String album_id = jObj.getString(TAG_ID);
					 
					JSONArray albums = jObj.getJSONArray("songs");

					if (albums != null) {
						// looping through All songs
						for (int i = 0; i < albums.length(); i++) {
							JSONObject c = albums.getJSONObject(i);

							// Storing each json item in variable
							String song_id = c.getString(TAG_ID);
							 
							String name = c.getString(TAG_TITLE);
							 
							HashMap<String, String> map = new HashMap<String, String>();
 
							 
							map.put(TAG_ID, song_id);
						 
							map.put(TAG_TITLE, name);
							 
                    
                        if(trackList.contains(map) != true)
                        {
                        	trackList.add(map);
                        }
                      
                    }} }
             //   }  
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

       
        @Override
        protected void onPostExecute(String result) {
          
            ListAdapter adapter = new SimpleAdapter(
					getActivity(), trackList,
					R.layout.list_tracks, new String[] { TAG_ID,
							TAG_TITLE,}, new int[] {
							R.id.album_id, R.id.album_name});
			// updating listview
			setListAdapter(adapter);
        }
    }
}
