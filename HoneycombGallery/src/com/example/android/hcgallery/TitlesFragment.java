/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.hcgallery;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	ConnectionDetector cd;
	AlertDialogManager alert = new AlertDialogManager();
	private ProgressDialog pDialog;
	JsonParser jsonParser = new JsonParser();
	JSONArray albums = null;
	String[] values ;
	String album_id, album_name;
	String json;
 
    ArrayList<HashMap<String, String>> trackList = new ArrayList<HashMap<String, String>>();
 
	private static final String URL_TRACKS = "http://www.platinumplaya.co.za/ajax/getTracks";

	 
	private static final String TAG_ID = "id";
	private static final String TAG_TITLE = "name";
	private static final String TAG_ALBUM = "album";
	private static final String TAG_DURATION = "duration";
	 
	 
    public interface OnItemSelectedListener {
        public void onItemSelected(int category, int position);
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
       album_id = i.getStringExtra("album_id");
       
        new loadTracks().execute();
    
        ContentFragment frag = (ContentFragment) getFragmentManager()
                .findFragmentById(R.id.content_frag);
        if (frag != null) mDualFragments = true;

        ActionBar bar = getActivity().getActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Must call in order to get callback to onCreateOptionsMenu()
        setHasOptionsMenu(true);

        Directory.initializeDirectory();
        for (int ji = 0; ji < Directory.getCategoryCount(); ji++) {
            bar.addTab(bar.newTab().setText(Directory.getCategory(ji).getName())
                    .setTabListener(this));
        }

        //Current position should survive screen rotations.
        if (savedInstanceState != null) {
            mCategory = savedInstanceState.getInt("category");
            mCurPosition = savedInstanceState.getInt("listPosition");
            bar.selectTab(bar.getTabAt(mCategory));
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
        DirectoryCategory cat = Directory.getCategory(category);
        String[] items = new String[cat.getEntryCount()];
        for (int i = 0; i < cat.getEntryCount(); i++)
            items[i] = cat.getEntry(i).getName();
        // Convenience method to attach an adapter to ListFragment's ListView
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.title_list_item, items));
        mCategory = category;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Send the event to the host activity via OnItemSelectedListener callback
        mListener.onItemSelected(mCategory, position);
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
        mListener.onItemSelected(mCategory, position);
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

        

		/**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
             super.onPreExecute();
             pDialog = new ProgressDialog(getActivity());
             Log.d("Activity Context", getActivity().getApplicationContext().toString() + "");
             pDialog.setMessage("Loading Categories. Please wait...");
             pDialog.setIndeterminate(false);
             pDialog.setCancelable(false);
             pDialog.show();
        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... param) {
         
        List<NameValuePair> params = new ArrayList<NameValuePair>();     			
        params.add(new BasicNameValuePair(TAG_ID, album_id));
        String jsonS = jsonParser.makeHttpRequest(URL_TRACKS, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All tracks: ", jsonS);
            int num = 1;
            try {
                // Checking for SUCCESS TAG
             
                JSONObject json = new JSONObject(jsonS);
                //String success = json.getString("success");
               // if (success == "1") {
                    // products found
                    // Getting Array of Products
               
                    JSONArray category_list = json.getJSONArray("songs");
                    Log.d("Category List JSON Array", category_list.toString() + "");
                    // looping through All Products
                    for (int j = 1; j < category_list.length(); j++) {
                        JSONObject c = category_list.getJSONObject(j);

                     // Storing each json item in variable
						String song_id = c.getString(TAG_ID);
						 
						String name = c.getString(TAG_TITLE);
						String duration = c.getString(TAG_DURATION);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                     // adding each child node to HashMap key => value
						map.put("album_id", album_id);
						map.put(TAG_ID, song_id);
						 
						map.put(TAG_TITLE, name);
						map.put(TAG_DURATION, duration);
  
                        num = num + 1;
                        // adding HashList to ArrayList
                        if(trackList.contains(map) != true)
                        {
                        	trackList.add(map);
                        }
                        Log.d("Category List", trackList.toString() + " ");
                    }
             //   } 
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();


            ListAdapter adapter = new SimpleAdapter(
					getActivity(), trackList,
					R.layout.list_tracks, new String[] { "album_id", TAG_ID, "track_no",
							TAG_TITLE, TAG_DURATION }, new int[] {
							R.id.album_id, R.id.song_id, R.id.track_no, R.id.album_name, R.id.song_duration });
			// updating listview
			setListAdapter(adapter);
        }
    }
}
