package com.example.eric.processtracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.eric.processtracker.service.ProcessListBackgroundService;

public class MainActivity extends FragmentActivity {

    private TextView mTitle;
    private ProcListFragment mProcListFragment;
    private Intent mBackgroundServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = (TextView) findViewById(R.id.title);

        mBackgroundServiceIntent = new Intent(this, ProcessListBackgroundService.class);
        Button stopServiceButton = (Button) findViewById(R.id.exit_button);
        stopServiceButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopService(mBackgroundServiceIntent);
                        finish();
                    }
                });

        mProcListFragment = (ProcListFragment) getSupportFragmentManager().findFragmentByTag(
                        ProcListFragment.FRAGMENT_TAG);
        if (mProcListFragment == null) {
            mProcListFragment = new ProcListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.top_container, mProcListFragment, ProcListFragment.FRAGMENT_TAG)
                    .commit();
        }
        mProcListFragment.setListener(
                new ProcListFragment.Listener() {
                    @Override
                    public void onProcessSelected(int pid, final String name) {
                        if (getSupportFragmentManager().findFragmentByTag(
                                ProcessSizeFragment.FRAGMENT_TAG) != null) {
                            return;
                        }

                        ProcessSizeFragment fragment = ProcessSizeFragment.newInstance(pid, name);
                        fragment.setListener(
                                new ProcessSizeFragment.Listener() {
                                    @Override
                                    public void onSizeUpdate(int size) {
                                        Log.e("yinlokh", "size update");
                                        mTitle.setText(name + " " + size);
                                    }
                                });
                        getSupportFragmentManager().beginTransaction()
                                .addToBackStack(ProcListFragment.FRAGMENT_TAG)
                                .setCustomAnimations(
                                        android.R.anim.fade_in,
                                        android.R.anim.fade_out,
                                        android.R.anim.fade_in,
                                        android.R.anim.fade_out)
                                .replace(
                                        R.id.top_container,
                                        fragment,
                                        ProcessSizeFragment.FRAGMENT_TAG)
                                .commit();
                    }

                    @Override
                    public void onResume() {
                        mTitle.setText(getText(R.string.all_processes_label));
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(mBackgroundServiceIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
