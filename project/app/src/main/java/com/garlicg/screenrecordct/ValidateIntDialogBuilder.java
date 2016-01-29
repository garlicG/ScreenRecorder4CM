package com.garlicg.screenrecordct;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.garlicg.screenrecordct.util.DisplayUtils;

public class ValidateIntDialogBuilder {


    interface Callback{
        boolean onValidate(int value);
        void onOk(int value);
    }


    public static AlertDialog build(final Context context, int value, CharSequence hint, final Callback callback){
        AlertDialog.Builder ab = new AlertDialog.Builder(context);

        final EditText editText = new EditText(context);
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        editText.setHint(hint);
        String valueText = String.valueOf(value);
        editText.setText(valueText);
        editText.setSelection(valueText.length());
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(String.valueOf(Integer.MAX_VALUE).length() - 1);
        editText.setFilters(new InputFilter[]{lengthFilter});

        int dp16 = DisplayUtils.dpToPx(context.getResources(), 16);
        ab.setView(editText, dp16, dp16, dp16, 0);

        ab.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int value = Integer.parseInt(editText.getText().toString());
                callback.onOk(value);
            }
        });
        final AlertDialog ad = ab.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, 0);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button button = ad.getButton(DialogInterface.BUTTON_POSITIVE);
                if (TextUtils.isEmpty(s)) {
                    button.setEnabled(false);
                }
                else {
                    try {
                        int value = Integer.parseInt(s.toString());
                        button.setEnabled(callback.onValidate(value));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        button.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return ad;
    }
}
