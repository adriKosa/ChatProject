package com.example.adria.chatproject.Utilities;

import android.app.Activity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

public class ProgressBarUtil{

    private ProgressBar progressBar;
    private Activity activityRef;
    private View rootView;

    public ProgressBarUtil(Activity activityRef, ProgressBar progressBar){
        this.progressBar = progressBar;
        this.activityRef = activityRef;
        rootView = activityRef.getWindow().getDecorView().getRootView();
        progressBar.setVisibility(View.GONE);
    }




    public void setProgressBar(){
        Log.i("PROGRESBAR", "set progress bar");
        progressBar.setVisibility(View.VISIBLE);
        activityRef.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        InputMethodManager imm = (InputMethodManager) activityRef.getSystemService(Activity.INPUT_METHOD_SERVICE);
        rootView = activityRef.getCurrentFocus();

        if (rootView == null){
            rootView = new View (activityRef);
        }
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public void dismisProgressBar(){
        progressBar.setVisibility(View.GONE);
        activityRef.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
