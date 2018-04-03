package com.greenorange.myuiaccount.Util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.util.List;

public class UiUtils {

    /** Show a simple toast at the bottom */
    public static void showToastAtBottom(Context context, final int messageId) {
        UiUtils.showToastAtBottom(context, context.getApplicationContext().getString(messageId));
    }

    /** Show a simple toast at the bottom */
    public static void showToastAtBottom(Context context, final String message) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /** Show a simple toast at the default position */
    public static void showToast(Context context, final int messageId) {
        final Toast toast = Toast.makeText(context.getApplicationContext(),
                context.getApplicationContext().getString(messageId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /** Show a simple toast at the default position */
    public static void showToast(Context context, final int pluralsMessageId, final int count) {
        final Toast toast = Toast.makeText(context.getApplicationContext(),
                context.getApplicationContext().getResources().getQuantityString(pluralsMessageId, count),
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    public static CharSequence commaEllipsize(
            final String text,
            final TextPaint paint,
            final int width,
            final String oneMore,
            final String more) {
        CharSequence ellipsized = TextUtils.commaEllipsize(
                text,
                paint,
                width,
                oneMore,
                more);
        if (TextUtils.isEmpty(ellipsized)) {
            ellipsized = text;
        }
        return ellipsized;
    }


    public static Rect getMeasuredBoundsOnScreen(final View view) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1],
                location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(final Activity activity, final int color) {
        if (OsUtil.isAtLeastL()) {
            // To achieve the appearance of an 80% opacity blend against a black background,
            // each color channel is reduced in value by 20%.
            final int blendedRed = (int) Math.floor(0.8 * Color.red(color));
            final int blendedGreen = (int) Math.floor(0.8 * Color.green(color));
            final int blendedBlue = (int) Math.floor(0.8 * Color.blue(color));

            activity.getWindow().setStatusBarColor(
                    Color.rgb(blendedRed, blendedGreen, blendedBlue));
        }
    }

    public static void lockOrientation(final Activity activity) {
        final int orientation = activity.getResources().getConfiguration().orientation;
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        // rotation tracks the rotation of the device from its natural orientation
        // orientation tracks whether the screen is landscape or portrait.
        // It is possible to have a rotation of 0 (device in its natural orientation) in portrait
        // (phone), or in landscape (tablet), so we have to check both values to determine what to
        // pass to setRequestedOrientation.
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        }
    }

    public static void unlockOrientation(final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public static int getPaddingStart(final View view) {
        return OsUtil.isAtLeastJB_MR1() ? view.getPaddingStart() : view.getPaddingLeft();
    }

    public static int getPaddingEnd(final View view) {
        return OsUtil.isAtLeastJB_MR1() ? view.getPaddingEnd() : view.getPaddingRight();
    }




   

    /**
     * Get the activity that's hosting the view, typically casting view.getContext() as an Activity
     * is sufficient, but sometimes the context is a context wrapper, in which case we need to case
     * the base context
     */
    public static Activity getActivity(final View view) {
        if (view == null) {
            return null;
        }
        return getActivity(view.getContext());
    }

    /**
     * Get the activity for the supplied context, typically casting context as an Activity
     * is sufficient, but sometimes the context is a context wrapper, in which case we need to case
     * the base context
     */
    public static Activity getActivity(final Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        // We've hit a non-activity context such as an app-context
        return null;
    }
}
