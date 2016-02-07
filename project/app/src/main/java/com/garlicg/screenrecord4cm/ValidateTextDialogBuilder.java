package com.garlicg.screenrecord4cm;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.garlicg.screenrecord4cm.util.DisplayUtils;

public class ValidateTextDialogBuilder {


    interface Callback{
        boolean onValidate(CharSequence value);
        void onOk(CharSequence value);
    }


    public static AlertDialog build(final Context context, CharSequence value, CharSequence hint, int maxLength ,final Callback callback){
        AlertDialog.Builder ab = new AlertDialog.Builder(context);

        final EditText editText = new EditText(context);
        editText.setHint(hint);
        editText.setText(value);
        editText.setSelection(value.length());
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(new InputFilter[]{lengthFilter});

        int dp16 = DisplayUtils.dpToPx(context.getResources(), 16);
        ab.setView(editText, dp16, dp16, dp16, 0);

        ab.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onOk(editText.getText());
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
                button.setEnabled(callback.onValidate(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return ad;
    }
}
