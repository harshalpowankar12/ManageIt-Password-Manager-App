package com.hpdevelopers.manageit;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class ViewUtils {
    private ViewUtils() {

    }

    public static void resetTextInputErrorsOnTextChanged(TextInputLayout... textInputLayouts) {
        for (final TextInputLayout inputLayout : textInputLayouts) {
            EditText editText = inputLayout.getEditText();
            if(editText != null) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                    }

                    @Override
                    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

                    }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        if(inputLayout.getError() != null) inputLayout.setError(null);
                    }
                });
            }
        }
    }
}
