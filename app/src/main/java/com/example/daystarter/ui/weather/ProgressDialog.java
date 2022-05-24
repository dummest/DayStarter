package com.example.daystarter.ui.weather;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import com.example.daystarter.R;
import androidx.annotation.NonNull;

public class ProgressDialog extends Dialog {
    public ProgressDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);
    }
}
