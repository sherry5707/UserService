/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ragentek.ypush.service.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


/// M:
import android.util.Log;

import com.ragentek.ypush.service.R;

/**
 * A dialog that prompts the user for the message deletion limits.
 */
public class EditDialog extends AlertDialog implements OnClickListener {

    private static final String NUMBER = "number";
    /// M: Code analyze 001, new feature, personal use on device by Hongduo Wang @{
    private int mInitialNumber;
    /// @}
    private final String TAG = "Mms/NumberPickerDialog";

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnNumberSetListener {

        /**
         * @param number The number that was set.
         */
        void onNumberSet(int number);
    }
    private final EditText mNumberEdit;
    private final OnNumberSetListener mCallback;

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param number The initial number.
     */
    public EditDialog(Context context,
            OnNumberSetListener callBack,
            int number,
            int title) {
        this(context, 0, callBack, number, title);
    }

    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param number The initial number.
     */
    public EditDialog(Context context,
            int theme,
            OnNumberSetListener callBack,
            int number,
            int title) {
        super(context, theme);
        mCallback = callBack;
        /// M: Code analyze 001, new feature, personal use on device by Hongduo Wang @{
        mInitialNumber = number;
        /// @}

        setTitle(title);

        setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.set), this);
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(R.string.no),
                (OnClickListener) null);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.edit_dialog, null);
        setView(view);
        
        mNumberEdit = (EditText) view.findViewById(R.id.number);
        mNumberEdit.setText(String.valueOf(mInitialNumber));
        mNumberEdit.setSelection(mNumberEdit.getText().length());
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
        	mNumberEdit.clearFocus();
            mCallback.onNumberSet(Integer.parseInt(mNumberEdit.getText().toString()));
            dialog.dismiss();
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
         state.putInt(NUMBER, Integer.parseInt(mNumberEdit.getText().toString()));
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int number = savedInstanceState.getInt(NUMBER);
        mNumberEdit.setText(String.valueOf(number));
    }
}
