package com.erakk.lnreader.UI.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.erakk.lnreader.Constants;
import com.erakk.lnreader.LNReaderApplication;
import com.erakk.lnreader.R;
import com.erakk.lnreader.UI.fragment.BookmarkFragment;
import com.erakk.lnreader.UI.fragment.DownloadFragment;
import com.erakk.lnreader.UI.fragment.MainFragment;
import com.erakk.lnreader.UI.fragment.SearchFragment;
import com.erakk.lnreader.UI.fragment.UpdateInfoFragment;
import com.erakk.lnreader.callback.ICallbackEventData;
import com.erakk.lnreader.callback.IExtendedCallbackNotifier;
import com.erakk.lnreader.task.AsyncTaskResult;
import com.erakk.lnreader.task.CheckDBReadyTask;

public class MainActivity extends BaseActivity implements IExtendedCallbackNotifier<AsyncTaskResult<Boolean>> {
    private static final String TAG = MainActivity.class.toString();
    private final Context ctx = this;

    private AlertDialog dialog;
    private CheckDBReadyTask task = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout(R.layout.fragactivity_framework);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        init();
    }

    // region private method

    private void init() {
        String initialFragment = getIntent().getStringExtra(Constants.EXTRA_INITIAL_FRAGMENT);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (initialFragment == null) {
            transaction.replace(R.id.mainFrame, new MainFragment());
        } else if (initialFragment.equalsIgnoreCase(UpdateInfoFragment.class.toString())) {
            transaction.replace(R.id.mainFrame, new UpdateInfoFragment());
        } else if (initialFragment.equalsIgnoreCase(DownloadFragment.class.toString())) {
            transaction.replace(R.id.mainFrame, new DownloadFragment());
        } else if (initialFragment.equalsIgnoreCase(BookmarkFragment.class.toString())) {
            transaction.replace(R.id.mainFrame, new BookmarkFragment());
        } else if (initialFragment.equalsIgnoreCase(SearchFragment.class.toString())) {
            transaction.replace(R.id.mainFrame, new SearchFragment());
        } else {
            Toast.makeText(this, "Nothing!!", Toast.LENGTH_LONG).show();
        }
        if (initialFragment != null)
            transaction.addToBackStack(initialFragment);
        transaction.commit();

        if (isFirstRun()) {
            // Show copyrights
            new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.terms_of_use)).setMessage(getResources().getString(R.string.bakatsuki_message) + getString(R.string.bakatsuki_copyrights) + "\n\nBy clicking \"I Agree\" below, you confirm that you have read the TLG Translation Common Agreement in it's entirety.").setPositiveButton(getResources().getString(R.string.agree), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setFirstRun();
                }
            }).setNegativeButton(getResources().getString(R.string.disagree), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }

        // check db access
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            checkDBAccess();
        }

        Log.d(TAG, "Initial Fragment:" + initialFragment);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "popping fragment stack");
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isFirstRun() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_FIRST_RUN, true);
    }

    private void setFirstRun() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(Constants.PREF_FIRST_RUN, false);
        edit.commit();
    }

    @SuppressLint({"NewApi"})
    private void checkDBAccess() {
        task = new CheckDBReadyTask(this);
        String key = TAG + ":CheckDBAccess";
        boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);
        if (isAdded) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
        } else {
            CheckDBReadyTask tempTask = (CheckDBReadyTask) LNReaderApplication.getInstance().getTask(key);
            if (tempTask != null) {
                task = tempTask;
                task.owner = this;
            }
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Checking...");
        alertDialogBuilder.setCancelable(false);

        dialog = alertDialogBuilder.create();
        dialog.setTitle("Checking DB access");

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    // endregion

    // region IExtendedCallbackNotifier implementation

    @Override
    public void onCompleteCallback(ICallbackEventData message, AsyncTaskResult<Boolean> result) {
        if (dialog != null) {
            dialog.dismiss();
            Toast.makeText(this, message.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressCallback(ICallbackEventData message) {
        if (dialog != null) {
            dialog.setMessage(message.getMessage());
        }
    }

    @Override
    public boolean downloadListSetup(String taskId, String message, int setupType, boolean hasError) {
        // TODO Auto-generated method stub
        return false;
    }
    // endregion


}
