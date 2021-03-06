package com.marius.reporter.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.marius.reporter.R;
import com.marius.reporter.database.ReportRepo;
import com.marius.reporter.fragments.ReportFragment;
import com.marius.reporter.models.Report;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ReportPagerActivity extends ThemedActivity implements ReportFragment.Callbacks {
    @SuppressWarnings("unused")
    private static final String TAG = "ReportPagerActivity";

    private static final String EXTRA_REPORT_ID = "com.marius.reporter.activities.report_id";

    private ViewPager mViewPager;
    private List<Report> mReports;

    static Intent newIntent(Context packageContext, UUID reportId) {
        Intent intent = new Intent(packageContext, ReportPagerActivity.class);
        intent.putExtra(EXTRA_REPORT_ID, reportId.toString());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_pager);

        UUID reportId = UUID.fromString(getIntent().getStringExtra(EXTRA_REPORT_ID));

        mViewPager = findViewById(R.id.report_view_pager);
        mReports = ReportRepo.getInstance(this).getReports();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                Report report = mReports.get(i);
                return ReportFragment.newInstance(report.getId());
            }

            @Override
            public int getCount() {
                return mReports.size();
            }
        });

        for (int i = 0; i < mReports.size(); i++) {
            if (mReports.get(i).getId().equals(reportId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onReportUpdated(Report report) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
