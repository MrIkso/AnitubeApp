package com.mrikso.anitube.app.utils.html;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * author : wuliang
 * e-mail : wuliang6661@163.com
 * date   : 2020/5/2816:33
 * desc   : textview 加载html 中有图片的加载器
 * version: 1.0
 */

public class ImageGetterUtils {

    private static final String LOG_TAG = ImageGetterUtils.class.getSimpleName();
    public static MyImageGetter getImageGetter(Context context) {
        return new MyImageGetter(context);
    }

    public static class MyImageGetter implements Html.ImageGetter {

        private URLDrawable urlDrawable = null;
        private final Context context;

        public MyImageGetter(Context context) {
            this.context = context;
        }

        @Override
        public Drawable getDrawable(final String source) {
            urlDrawable = new URLDrawable();
            Glide.with(context).asBitmap()
                    .load(source)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    urlDrawable.bitmap = changeBitmapSize(resource);
                    urlDrawable.setBounds(0, 0, changeBitmapSize(resource).getWidth(), changeBitmapSize(resource).getHeight());
                   // textView.invalidate();
                   //textView.setText(textView.getText());//不加这句显示不出来图片，原因不详
                }
            });

            return urlDrawable;
        }

        public static class URLDrawable extends BitmapDrawable {
            public Bitmap bitmap;

            @Override
            public void draw(Canvas canvas) {
                super.draw(canvas);
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, 0, 0, getPaint());
                }
            }
        }

        private Bitmap changeBitmapSize(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
           /* Log.d(LOG_TAG, "width:" + width);
            Log.d(LOG_TAG, "height:" + height);
          */
            int newWidth = width;
            int newHeight = height;

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            bitmap.getWidth();

            bitmap.getHeight();

           // Log.d(LOG_TAG, "newWidth" + bitmap.getWidth());

          //  Log.d(LOG_TAG, "newHeight" + bitmap.getHeight());

            return bitmap;

        }


    }
}
