package com.marius.reporter.activities;

import android.support.v4.app.Fragment;
import com.marius.reporter.activities.SingleFragmentActivity;
import com.marius.reporter.fragments.ReportFragment;

public class ReportActivity extends SingleFragmentActivity {

    public static boolean isDarkMode = true;

    @Override
    protected Fragment createFragment() {
        return new ReportFragment();
    }
}
