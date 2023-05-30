package com.mrikso.anitube.app.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.snackbar.Snackbar;
import com.mrikso.anitube.app.R;

import java.util.List;
import java.util.Random;

public class ViewUtils {

    @ColorInt
    public static int getRandomMaterialColor(Context context) {
        int[] colors = context.getResources().getIntArray(R.array.material_colors);
        return colors[new Random().nextInt(colors.length)];
    }

    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static final void loadAvatar(ImageView view, String url) {

        DrawableCrossFadeFactory factory$iv =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        Glide.with(view.getContext())
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(view);
    }

    public static void loadBluredImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(new RequestOptions()
                        .transform(new FastBlurTransformation())
                        .dontAnimate())
                .into(view);
    }

    public static Drawable changeIconColor(Context context, @DrawableRes int drawable, @ColorRes int colorRes) {
        Drawable unwrappedDrawable = ContextCompat.getDrawable(context, drawable);
        assert unwrappedDrawable != null;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, colorRes));
        return wrappedDrawable;
    }

    public static void makeLinks(TextView textView, List<Pair<String, View.OnClickListener>> links) {
        SpannableString spannableString = new SpannableString(textView.getText().toString());
        int startIndexState = -1;

        for (Pair<String, View.OnClickListener> link : links) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    widget.invalidate();
                    assert link.second != null;
                    link.second.onClick(widget);
                }
            };
            assert link.first != null;
            int startIndexOfLink = textView.getText().toString().indexOf(link.first, startIndexState + 1);
            spannableString.setSpan(
                    clickableSpan,
                    startIndexOfLink,
                    startIndexOfLink + link.first.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    /**
     * Displays a snackbar to the user with a String resource.
     *
     * <p>NOTE: If there is an accessibility manager enabled on the device, such as LastPass, then
     * the snackbar animations will not work.
     *
     * @param activity the activity needed to create a snackbar.
     * @param resource the string resource to show to the user.
     */
    public static void showSnackbar(@NonNull Activity activity, @StringRes int resource) {
        View view = activity.findViewById(android.R.id.content);
        if (view == null) {
            return;
        }
        Snackbar.make(view, resource, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Displays a snackbar to the user with a string message.
     *
     * <p>NOTE: If there is an accessibility manager enabled on the device, such as LastPass, then
     * the snackbar animations will not work.
     *
     * @param activity the activity needed to create a snackbar.
     * @param message the string message to show to the user.
     */
    public static void showSnackbar(@NonNull Activity activity, @NonNull String message) {
        View view = activity.findViewById(android.R.id.content);
        if (view == null) {
            return;
        }
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    //    public static List<Integer> getAllMaterialColors(Context context)
    //            throws IOException, XmlPullParserException {
    //        XmlResourceParser xrp =
    //                context.getResources().getXml(R.xml.android_material_design_colours);
    //        List<Integer> allColors = new ArrayList<>();
    //        int nextEvent;
    //        while ((nextEvent = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
    //            String s = xrp.getName();
    //            if ("color".equals(s)) {
    //                String color = xrp.nextText();
    //                allColors.add(Color.parseColor(color));
    //            }
    //        }
    //        return allColors;
    //    }
}
