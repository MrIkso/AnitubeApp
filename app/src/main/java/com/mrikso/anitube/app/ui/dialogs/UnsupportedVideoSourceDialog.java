package com.mrikso.anitube.app.ui.dialogs;

import android.content.Context;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.IntentUtils;

public class UnsupportedVideoSourceDialog {

    public static void show(Context context, String ifRame) {
        DialogUtils.showConfirmation(
                context,
                R.string.error_dialog_title,
                R.string.error_dialog_message_unsupport_video_source,
                () -> {
                    IntentUtils.openInBrowser(context, ifRame);
                });
    }
}
