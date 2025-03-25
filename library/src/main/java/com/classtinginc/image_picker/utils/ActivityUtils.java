package com.classtinginc.image_picker.utils;

import androidx.appcompat.app.ActionBar;

/**
 * Created by classting on 28/06/2019.
 */

public class ActivityUtils {

    public static void setNavigation(ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    public static void setNavigation(ActionBar actionBar, int icon) {
        setNavigation(actionBar);
        actionBar.setHomeAsUpIndicator(icon);
    }
}
