 
package com.example.android.hcgallery;

import android.app.ActionBar;
import android.app.Fragment;
 
import android.content.Intent;
 
import android.os.Bundle;
import android.text.Html;
import android.view.ActionMode;
 
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
 
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
 
import android.widget.TextView;
  

 
public class ContentFragment extends Fragment {
     
    private int mCategory = 0;
    private int mCurPosition = 0;
    private boolean mSystemUiVisible = true;
    private boolean mSoloFragment = false;
 
    private String title;
    // Current action mode (contextual action bar, a.k.a. CAB)
    private ActionMode mCurrentActionMode;

    /** This is where we initialize the fragment's UI and attach some
     * event listeners to UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
      

		View view = inflater.inflate(R.layout.content_welcome, container, false);
		return view;
 
    }

    /** This is where we perform additional setup for the fragment that's either
     * not related to the fragment's layout or must be done after the layout is drawn.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set member variable for whether this fragment is the only one in the activity
        Fragment listFragment = getFragmentManager().findFragmentById(R.id.titles_frag);
        mSoloFragment = listFragment == null ? true : false;

        if (mSoloFragment) {
            // The fragment is alone, so enable up navigation
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            // Must call in order to get callback to onOptionsItemSelected()
            setHasOptionsMenu(true);
        }

        // Current position and UI visibility should survive screen rotations.
        if (savedInstanceState != null) {
            setSystemUiVisible(savedInstanceState.getBoolean("systemUiVisible"));
            if (mSoloFragment) {
                // Restoring these members is not necessary when this fragment
                // is combined with the TitlesFragment, because when the TitlesFragment
                // is restored, it selects the appropriate item and sends the event
                // to the updateContentAndRecycleBitmap() method itself
                mCategory = savedInstanceState.getInt("category");
                mCurPosition = savedInstanceState.getInt("listPosition");
              title = savedInstanceState.getString("title");
				updateContent(mCategory, mCurPosition,title);
            }
        }

        if (mSoloFragment) {
        
          ActionBar bar = getActivity().getActionBar();
          bar.setTitle(title);
        }
    }
    public void setText(String item) {
		TextView view = (TextView) getView().findViewById(R.id.album_name);
		view.setText(item);
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // This callback is used only when mSoloFragment == true (see onActivityCreated above)
        switch (item.getItemId()) {
        case android.R.id.home:
            // App icon in Action Bar clicked; go up
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Reuse the existing instance
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("listPosition", mCurPosition);
        outState.putInt("category", mCategory);
        outState.putString("title", title);
        outState.putBoolean("systemUiVisible", mSystemUiVisible);
    }

    /** Toggle whether the system UI (status bar / system bar) is visible.
     *  This also toggles the action bar visibility.
     * @param show True to show the system UI, false to hide it.
     */
    void setSystemUiVisible(boolean show) {
        mSystemUiVisible = show;

        Window window = getActivity().getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        View view = getView();
        ActionBar actionBar = getActivity().getActionBar();

        if (show) {
            // Show status bar (remove fullscreen flag)
            window.setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Show system bar
            view.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
            // Show action bar
            actionBar.show();
        } else {
            // Add fullscreen flag (hide status bar)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Hide system bar
            view.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
            // Hide action bar
            actionBar.hide();
        }
        window.setAttributes(winParams);
    }
 

    void updateContent(int category, int position,String titl) {
    	title = titl;
        mCategory = category;
        mCurPosition = position;
        TextView txt_album_name = (TextView) getView().findViewById(R.id.album_name);
        txt_album_name.setText(Html.fromHtml("<b>Album:</b> " + title));
        if (mCurrentActionMode != null) {
            mCurrentActionMode.finish();
        }     
    } 
}
