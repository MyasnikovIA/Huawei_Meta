package ru.miacomsoft.huawei_meta.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;




public class Dialog {
    public interface ConfirmDialogListener {
        void onConfirm(boolean isConfirmed);
    }

    public static void showConfirmDialog(Context context, String title, String message, final ConfirmDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirm(true);
                }
            }
        });

        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirm(false);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
