package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mrikso.anitube.app.R;

import java.util.Random;

public class ViewUtils {

    @ColorInt
    public static int getRandomMaterialColor(Context context) {
        int[] colors = context.getResources().getIntArray(R.array.material_colors);
        return colors[new Random().nextInt(colors.length)];
    }

    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(view);
    }

    public static Drawable changeIconColor(
            Context context, @DrawableRes int drawable, @ColorRes int colorRes) {
        Drawable unwrappedDrawable = ContextCompat.getDrawable(context, drawable);
        assert unwrappedDrawable != null;
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, colorRes));
        return wrappedDrawable;
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
