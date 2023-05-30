package com.mrikso.anitube.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.util.ConvertUtils;
import com.google.android.material.color.MaterialColors;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.utils.ViewUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarViewWithOnline extends FrameLayout {
    public static final boolean DRAW_ONLINES = true;
    CircleImageView avatarView;
    ImageView online;
    private boolean isOnline = false;

    public AvatarViewWithOnline(Context context) {
        super(context);
        init();
    }

    public AvatarViewWithOnline(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AvatarViewWithOnline(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        avatarView = new CircleImageView(getContext());
        avatarView.setBorderColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary));
        avatarView.setBorderWidth(ConvertUtils.dp2px(2f));

        addView(avatarView);

        online = new ImageView(getContext());
        online.setImageResource(R.drawable.ic_circle_status_offline);
        online.setVisibility(GONE);
        addView(
                online,
                new LayoutParams(
                        ConvertUtils.dp2px(15f),
                        ConvertUtils.dp2px(15f),
                        Gravity.RIGHT | Gravity.BOTTOM | Gravity.END));
    }

    public void bind(String url) {
        ViewUtils.loadImage(avatarView, url);
    }

    public void setOnline(boolean online) {
        if (DRAW_ONLINES) {
            this.online.setVisibility(VISIBLE);
            isOnline = online;
            this.online.setImageResource(
                    isOnline ? R.drawable.ic_circle_status_online : R.drawable.ic_circle_status_offline);

        } else {
            this.online.setVisibility(GONE);
        }
    }
}
