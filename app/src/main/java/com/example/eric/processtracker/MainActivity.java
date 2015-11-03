package com.example.eric.processtracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.eric.processtracker.service.ProcessListBackgroundService;

public class MainActivity extends FragmentActivity {

    private TextView mTitle;
    private TextView mSizeText;
    private ProcessListFragment mProcessListFragment;
    private Intent mBackgroundServiceIntent;
    private ProcessSizeFragment.Listener mProcessSizeChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = (TextView) findViewById(R.id.title);
        mSizeText = (TextView) findViewById(R.id.size);

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

        mProcessSizeChangeListener = new ProcessSizeFragment.Listener() {
            @Override
            public void onSizeUpdate(int size) {
                mSizeText.setText(Integer.toString(size));
            }};

        ProcessSizeFragment processSizeFragment =
                (ProcessSizeFragment) getSupportFragmentManager().findFragmentByTag(
                        ProcessSizeFragment.FRAGMENT_TAG);
        if (processSizeFragment != null) {
            processSizeFragment.setListener(mProcessSizeChangeListener);
        }

        mProcessListFragment = (ProcessListFragment) getSupportFragmentManager().findFragmentByTag(
                        ProcessListFragment.FRAGMENT_TAG);
        if (mProcessListFragment == null) {
            mProcessListFragment = new ProcessListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.top_container, mProcessListFragment, ProcessListFragment.FRAGMENT_TAG)
                    .commit();
        }
        mProcessListFragment.setListener(
                new ProcessListFragment.Listener() {
                    @Override
                    public void onProcessSelected(int pid, final String name) {
                        if (getSupportFragmentManager().findFragmentByTag(
                                ProcessSizeFragment.FRAGMENT_TAG) != null) {
                            return;
                        }

                        String parts[] = name.split("[\\/\\.]");
                        mTitle.setText(parts[parts.length - 1]);

                        ProcessSizeFragment fragment = ProcessSizeFragment.newInstance(pid, name);
                        fragment.setListener(mProcessSizeChangeListener);
                        getSupportFragmentManager().beginTransaction()
                                .addToBackStack(ProcessListFragment.FRAGMENT_TAG)
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
                        mSizeText.setText(null);
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
