package com.marius.reporter.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.marius.reporter.database.report.ReportBaseHelper;
import com.marius.reporter.database.report.ReportCursorWrapper;
import com.marius.reporter.database.report.ReportDbSchema.ReportTable;
import com.marius.reporter.models.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReportRepo {
    @SuppressWarnings("unused")
    private static final String TAG = "ReportRepo";

    private static ReportRepo instance;
    public static ReportRepo getInstance(Context context) {
        if (instance == null) {
            instance = new ReportRepo(context);
        }
        return instance;
    }

    private SQLiteDatabase mDatabase;
    private TimeRepo mTimeRepo;

    private ReportRepo(Context context) {
        mDatabase = new ReportBaseHelper(context.getApplicationContext()).getWritableDatabase();
        mTimeRepo = TimeRepo.getInstance(context);
    }

    public void addReport(Report report) {
        ContentValues values = getContentValues(report);

        mDatabase.insert(ReportTable.NAME, null, values);

        mTimeRepo.addTimes(report);
    }

    public List<Report> getReports() {
        List<Report> reports = new ArrayList<>();

        try (ReportCursorWrapper cursor = queryReports(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                reports.add(cursor.getReport());
                cursor.moveToNext();
            }
        }

        for (Report report : reports) {
            mTimeRepo.getTimes(report);
        }

        return reports;
    }

    public Report getReport(UUID id) {

        try (ReportCursorWrapper cursor = queryReports(
                ReportTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            Report report = cursor.getReport();
            mTimeRepo.getTimes(report);
            return report;
        }
    }

    public void updateReport(Report report) {
        mTimeRepo.addTimes(report);

        String uuidString = report.getId().toString();
        ContentValues values = getContentValues(report);

        mDatabase.update(ReportTable.NAME, values,
                ReportTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public void deleteReport(Report report) {
        mTimeRepo.deleteTimes(report);
        String uuidString = report.getId().toString();

        String[] whereArgs = {uuidString};
        Log.d(TAG, String.format("whereArgs: %s", Arrays.toString(whereArgs)));

        int result = mDatabase.delete(ReportTable.NAME,
                ReportTable.Cols.UUID + " = ?",
                whereArgs);
        if (result == 1)
            Log.i(TAG, String.format("Deleted Report %s", report.toString()));
        else if (result == 0)
            Log.w(TAG, String.format("No Report found for %s", report.toString()));
        else
            Log.e(TAG, String.format("Deleted more than one report (%d) for %s", result, report.toString()));
    }

    private static ContentValues getContentValues(Report report) {
        ContentValues values = new ContentValues();
        values.put(ReportTable.Cols.UUID, report.getId().toString());
        values.put(ReportTable.Cols.FLYER_NAME, report.getFlyerName());
        values.put(ReportTable.Cols.REMAINING_FLYERS, report.getRemainingFlyers());
        values.put(ReportTable.Cols.WITH_REMAINING_FLYERS, report.isWithRemainingFlyers());
        values.put(ReportTable.Cols.GPS_NAME, report.getGPSName());

        return values;
    }

    private ReportCursorWrapper queryReports(String whereClause, String[] whereArgs) {
        @SuppressLint("Recycle") Cursor cursor = mDatabase.query(
                ReportTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null // orderBy
        );

        return new ReportCursorWrapper(cursor);
    }
}
