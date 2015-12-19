package com.exploremuse.brain.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jidleman on 8/7/2015.
 */
public class ViewUtil {
    private static Typeface APP_FONT;

    private static void initializeFonts(final Context context) {
        APP_FONT = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Regular.ttf");
    }

    public static void overrideFonts(final Context context, final View v) {
        if(APP_FONT == null)
            initializeFonts(context);

        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView && !excludeView(v)) {
                ((TextView)v).setTypeface(APP_FONT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
    }

    private static boolean excludeView(View v) {
        /*switch(v.getId()) {
            case R.id.menu_intro_text:
            case R.id.profile_header:
                return true;
        }*/
        return false;
    }
}
