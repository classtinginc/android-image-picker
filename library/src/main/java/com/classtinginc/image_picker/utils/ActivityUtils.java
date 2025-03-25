package com.classtinginc.image_picker.utils;

import androidx.appcompat.app.ActionBar;

/**
 * Created by classting on 28/06/2019.
 */

public class ActivityUtils {

    public static void setNavigation(ActionBar actionBar, int title) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(title);
    }

    public static void setNavigation(ActionBar actionBar, int title, int icon) {
        setNavigation(actionBar, title);
        actionBar.setHomeAsUpIndicator(icon);
    }
}
