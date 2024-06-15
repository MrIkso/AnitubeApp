package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrikso.anitube.app.R;

public class DialogUtils {
    public static AlertDialog getProDialog(Context activity, @StringRes int id) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_proress, null);
        RelativeLayout root = view.findViewById(R.id.root);
        TextView msg = view.findViewById(R.id.msg);
        // root.setBackgroundColor(activity.getResources().getColor(R.color.window_bg));
        // msg.setTextColor(activity.getResources().getColor(R.color.text_color_primary));
        msg.setText(activity.getString(id));
        builder.setCancelable(false);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
        return alertDialog;
    }

    public static void cancelDialog(AlertDialog alertDialog) {
        if (alertDialog != null) alertDialog.dismiss();
    }

    public static void showMessage(Context context, @StringRes int titleRes, @StringRes int messageRes) {
        showMessage(context, context.getString(titleRes), context.getString(messageRes));
    }

    public static void showMessage(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void showConfirmation(
            Context context, @StringRes int titleRes, @StringRes int messageRes, Runnable onYes) {
        showConfirmation(context, context.getString(titleRes), context.getString(messageRes), onYes, null);
    }

    public static void showConfirmation(
            Context context, @StringRes int titleRes, @StringRes int messageRes, Runnable onYes, Runnable onNo) {
        showConfirmation(context, context.getString(titleRes), context.getString(messageRes), onYes, onNo);
    }

    public static void showConfirmation(Context context, String title, String message, Runnable onYes, Runnable onNo) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    onYes.run();
                    //dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (onNo != null) {
                        onNo.run();
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
}
