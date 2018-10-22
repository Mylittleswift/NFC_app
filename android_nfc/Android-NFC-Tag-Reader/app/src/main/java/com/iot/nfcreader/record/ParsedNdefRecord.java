/*
 * Copyright (C) 2016 Joey
 *
 */

package com.iot.nfcreader.record;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public interface ParsedNdefRecord {

    /**
     * Returns a view to display this record.
     */
    View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset);

}
