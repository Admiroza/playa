// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.hcgallery;

import android.app.Activity;
import android.os.Bundle;

 
public class ContentActivity extends Activity {
  

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle extras = getIntent().getExtras();
       

      setContentView(R.layout.content_activity);

      if (extras != null) {
        // Take the info from the intent and deliver it to the fragment so it can update
        int category = extras.getInt("category");
        int position = extras.getInt("position");
        String title = extras.getString("title");
        ContentFragment frag = (ContentFragment) getFragmentManager().findFragmentById(R.id.content_frag);
        frag.updateContent(category, position,title);
      }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      
  }
}
