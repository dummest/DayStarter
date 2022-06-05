package com.example.daystarter.ui.weather;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.example.daystarter.R;
import androidx.annotation.NonNull;

public class ProgressDialog extends Dialog {
    public ProgressDialog(@NonNull Context context) {
        super(context);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_progress);
    }
}
