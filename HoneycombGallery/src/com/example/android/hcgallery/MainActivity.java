
package com.example.android.hcgallery;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

 
public class MainActivity extends Activity implements TitlesFragment.OnItemSelectedListener {

    private Animator mCurrentTitlesAnimator;
    private boolean mDualFragments = false;
    private boolean mTitlesHidden = false;
    String album_id,song_id;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        setContentView(R.layout.main);

        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);

        ContentFragment frag = (ContentFragment) getFragmentManager()
                .findFragmentById(R.id.content_frag);
        if (frag != null) mDualFragments = true;
        
        if (mTitlesHidden) {
            getFragmentManager().beginTransaction()
                    .hide(getFragmentManager().findFragmentById(R.id.titles_frag)).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If not showing both fragments 
        if (!mDualFragments) {
           //TODO
        } else {
           //
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        

        case R.id.menu_toggleTitles:
            toggleVisibleTitles();
            return true;
      

        default:
            return super.onOptionsItemSelected(item);
        }
    }

   
    public void toggleVisibleTitles() {
         
        final FragmentManager fm = getFragmentManager();
        final TitlesFragment f = (TitlesFragment) fm
                .findFragmentById(R.id.info_frag);
        final View titlesView = f.getView();
 
        final boolean isPortrait = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;

        final boolean shouldShow = f.isHidden() || mCurrentTitlesAnimator != null;

      
        if (mCurrentTitlesAnimator != null)
            mCurrentTitlesAnimator.cancel();
  
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                titlesView,
                PropertyValuesHolder.ofInt(
                        isPortrait ? "bottom" : "right",
                        shouldShow ? getResources().getDimensionPixelSize(R.dimen.titles_size)
                                   : 0),
                PropertyValuesHolder.ofFloat("alpha", shouldShow ? 1 : 0)
        );

        
        final ViewGroup.LayoutParams lp = titlesView.getLayoutParams();
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                
                if (isPortrait) {
                    lp.height = (Integer) valueAnimator.getAnimatedValue();
                } else {
                    lp.width = (Integer) valueAnimator.getAnimatedValue();
                }
                titlesView.setLayoutParams(lp);
            }
        });

        if (shouldShow) {
            fm.beginTransaction().show(f).commit();
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    mCurrentTitlesAnimator = null;
                    mTitlesHidden = false;
                    invalidateOptionsMenu();
                }
            });

        } else {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                boolean canceled;

                @Override
                public void onAnimationCancel(Animator animation) {
                    canceled = true;
                    super.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (canceled)
                        return;
                    mCurrentTitlesAnimator = null;
                    fm.beginTransaction().hide(f).commit();
                    mTitlesHidden = true;
                    invalidateOptionsMenu();
                }
            });
        }

        
        objectAnimator.start();
        mCurrentTitlesAnimator = objectAnimator;

        
    }

    
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
    	//TODO
        super.onSaveInstanceState(outState);
        
    }

 
    public void onItemSelected(int category, int position,String title) {

      if (!mDualFragments) {
           
          Intent intent = new Intent(this, ContentActivity.class);
          intent.putExtra("category", category);
          intent.putExtra("position", position);
          intent.putExtra("title", title);
          startActivity(intent);
      } else {
          
          ContentFragment frag = (ContentFragment) getFragmentManager()
                  .findFragmentById(R.id.content_frag);
          frag.updateContent(category, position,title);
      }
    }
 

    
}
