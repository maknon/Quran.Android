package com.maknoon.quran;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

public class MyEditText extends TextInputEditText
{
    public MyEditText(@NonNull Context context)
    {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    boolean launchKeyboard = false;

    public void launchKeyboard(boolean launchKeyboard)
    {
        this.launchKeyboard = launchKeyboard;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        if (hasWindowFocus && launchKeyboard)
        {
            requestFocus();
            post(() -> {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(this, 0);
            });
        }
    }
}