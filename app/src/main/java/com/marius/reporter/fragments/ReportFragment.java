package com.marius.reporter.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.Report.Time;

import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {
    private Report mReport;
    private TimeEditor mTimeEditor;

    private TimeAdapter mAdapter;
    private RecyclerView mTimeRecyclerView;
    private FloatingActionButton mAddTimeButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimeEditor = new TimeEditor();

        mReport = new Report();
//        Random random = new Random();
//        for (int i = 0; i < 20; i++) {
//            mReport.addTime(random.nextInt(24), random.nextInt(60), random.nextInt(60));
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        mTimeEditor.init(v);

        mTimeRecyclerView = v.findViewById(R.id.times_recycler_view);
        mTimeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        mAddTimeButton = v.findViewById(R.id.add_time_button);
        mAddTimeButton.setOnClickListener(v1 -> {
            mReport.addTime(new Time());
            updateUI();
        });

        updateUI();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter));
        itemTouchHelper.attachToRecyclerView(mTimeRecyclerView);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void setCurrentTime(Time time, TextView timeHolderTextView) {
        mTimeEditor.setCurrentTime(time, timeHolderTextView);
    }

    public void updateUI() {
        List<Time> times = mReport.getTimes();

        if (mAdapter == null) {
            mAdapter = new TimeAdapter(times);
            mTimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTimes(times);
            mAdapter.notifyDataSetChanged();
        }
    }

    private static class TimeEditor {
        private NumberPicker mHourPicker;
        private NumberPicker mMinutePicker;
        private NumberPicker mSecondPicker;
        private Time mTime;

        private TextView mTimeHolderTextView;

        public TimeEditor() {
            mTime = new Time();
        }

        public void init(View parent) {
            mHourPicker = parent.findViewById(R.id.hour_picker);
            mMinutePicker = parent.findViewById(R.id.minute_picker);
            mSecondPicker = parent.findViewById(R.id.second_picker);

            mHourPicker.setMaxValue(23);
            mMinutePicker.setMaxValue(59);
            mSecondPicker.setMaxValue(59);
            mHourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setHours(newVal);
                updateText();
            });
            mMinutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setMinutes(newVal);
                updateText();
            });
            mSecondPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                mTime.setSeconds(newVal);
                updateText();
            });
            mHourPicker.setFormatter(TimeEditor::format);
            mMinutePicker.setFormatter(TimeEditor::format);
            mSecondPicker.setFormatter(TimeEditor::format);

            mHourPicker.setEnabled(false);
            mMinutePicker.setEnabled(false);
            mSecondPicker.setEnabled(false);
        }

        public static String format(int value) {
            return String.format(Locale.UK, "%02d", value);
        }

        public void setCurrentTime(Time time, TextView timeHolderTextView) {
            mTime = time;
            mHourPicker.setValue(mTime.getHours());
            mMinutePicker.setValue(mTime.getMinutes());
            mSecondPicker.setValue(mTime.getSeconds());

            if (!mHourPicker.isEnabled()) mHourPicker.setEnabled(true);
            if (!mMinutePicker.isEnabled()) mMinutePicker.setEnabled(true);
            if (!mSecondPicker.isEnabled()) mSecondPicker.setEnabled(true);

            mTimeHolderTextView = timeHolderTextView;
        }

        private void updateText() {
            if (mTimeHolderTextView != null)
                mTimeHolderTextView.setText(mTime.toString());
        }
    }

    private class TimeHolder extends RecyclerView.ViewHolder {
        private Time mTime;
        private TextView mTimeTextView;

        public TimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_time, parent, false));
            mTimeTextView = itemView.findViewById(R.id.item_time_text);
            itemView.setOnClickListener(v -> setCurrentTime(mTime, mTimeTextView));
        }

        public void bind(Time time) {
            mTime = time;
            mTimeTextView.setText(mTime.toString());
        }
    }

    private class TimeAdapter extends RecyclerView.Adapter<TimeHolder> {
        private List<Time> mTimes;
        private Time mRecentlyDeletedTime;
        private int mRecentlyDeletedTimePosition;

        public TimeAdapter(List<Time> times) {
            mTimes = times;
        }

        @Override
        public void onBindViewHolder(@NonNull TimeHolder holder, int position, @NonNull List<Object> payloads) {
            Time time = mTimes.get(position);
            holder.bind(time);
        }

        @NonNull
        @Override
        public TimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeHolder timeHolder, int i) {}

        @Override
        public int getItemCount() {
            return mTimes.size();
        }

        public void setTimes(List<Time> times) {
            mTimes = times;
        }

        public void deleteTime(int position) {
            mRecentlyDeletedTime = mTimes.remove(position);
            mRecentlyDeletedTimePosition = position;

            notifyItemRemoved(position);
            showUndoSnackbar();
        }

        private void showUndoSnackbar() {
            View view = getActivity().findViewById(R.id.constraint_layout);
            Snackbar snackbar = Snackbar.make(view, "1 Time Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", v -> undoDelete());
            snackbar.show();
        }

        private void undoDelete() {
            mTimes.add(mRecentlyDeletedTimePosition, mRecentlyDeletedTime);
            notifyItemInserted(mRecentlyDeletedTimePosition);
        }
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private TimeAdapter mAdapter;

        public SwipeToDeleteCallback(TimeAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;

        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int position = viewHolder.getAdapterPosition();
            mAdapter.deleteTime(position);
        }
    }
}
