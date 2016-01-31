package com.garlicg.screenrecordct;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.garlicg.screenrecordct.util.DisplayUtils;
import com.garlicg.screenrecordct.util.ViewFinder;

public class InputSecondDialogBuilder {


    interface Callback{
        boolean onValidate(int value);
        void onOk(int value);
    }


    public static AlertDialog build(final Context context, int value, CharSequence unit, final Callback callback){
        AlertDialog.Builder ab = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input_second, null, false);
        final EditText editText = ViewFinder.byId(view, R.id.number);
        String valueText = String.valueOf(value);
        editText.setText(valueText);
        editText.setSelection(valueText.length());
        TextView unitView = ViewFinder.byId(view , R.id.unit);
        unitView.setText(unit);

        int dp16 = DisplayUtils.dpToPx(context.getResources(), 16);
        ab.setView(view, dp16, dp16, dp16, 0);

        ab.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int value = 0;
                CharSequence s = editText.getText();
                if (!TextUtils.isEmpty(s)) {
                    value = Integer.parseInt(s.toString());
                }
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
                int value = 0;
                if (!TextUtils.isEmpty(s)) {
                    value = Integer.parseInt(s.toString());
                }
                button.setEnabled(callback.onValidate(value));

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return ad;
    }
}
