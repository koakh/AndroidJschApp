package com.koakh.jschapp.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koakh.jschapp.R;
import com.koakh.jschapp.model.Content;

import java.util.List;
import java.util.Map;

/**
 * Created by mario on 01/02/2015.
 */

public class ContentArrayAdapter extends ArrayAdapter<Content> {
  public ContentArrayAdapter(Context context, int resource) {
    super(context, resource);
  }
}
