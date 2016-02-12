package com.example.rahul.nytimessearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog for the filterMenuItem options
 */
public class FilterDialog extends DialogFragment {

    @Bind(R.id.sortOrderSpinner) Spinner orderSpinner;
    @Bind(R.id.btnSave) Button btnSave;
    @Bind(R.id.datepickerEt) EditText datePickerEt;

    @Bind(R.id.chkArts) CheckBox chkArts;
    @Bind(R.id.chkStyle) CheckBox chkStyle;
    @Bind(R.id.chkSports) CheckBox chkSports;

    private Calendar calendar;
    private String sortOrder;
    private ArrayList<String> newsDesks;

    public interface SaveListener {
        void setFilters(Calendar beginDate, String order, ArrayList<String> newsDesks);
    }

    public FilterDialog() {
    }

    public static FilterDialog newInstance(Calendar beginDate, String sort,
            ArrayList<String> newsDesks) {
        FilterDialog filterDialog = new FilterDialog();
        Bundle args = new Bundle();
        args.putSerializable("calendar", beginDate);
        args.putString("sort", sort);
        args.putStringArrayList("newsDesks", newsDesks);
        filterDialog.setArguments(args);
        return filterDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Serializable calendarObj = bundle.getSerializable("calendar");
            if (calendarObj != null) {
                calendar = (Calendar) calendarObj;
            }
            sortOrder = bundle.getString("sort");
            newsDesks = bundle.getStringArrayList("newsDesks");
        }
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Reset the previous state of the filters
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(adapter);
        if (!TextUtils.isEmpty(sortOrder)) {
            int spinnerPosition = adapter.getPosition(sortOrder);
            orderSpinner.setSelection(spinnerPosition);
        }
        updateDate(calendar);
        // TODO: figure out better way to do this
        if (newsDesks != null) {
            for (String desk : newsDesks) {
                if (desk.equals("\"Arts\"")) {
                    chkArts.setChecked(true);
                }
                if (desk.equals("\"Fashion and Style\"")) {
                    chkStyle.setChecked(true);
                }
                if (desk.equals("\"Sports\"")) {
                    chkSports.setChecked(true);
                }
            }
        }
    }

    @OnClick(R.id.btnSave)
    public void saveFilters(View view) {
        String sortParam = orderSpinner.getSelectedItem().toString();
        if (newsDesks == null) {
            newsDesks = new ArrayList<>();
        }
        newsDesks.clear();
        if (chkArts.isChecked()) {
            newsDesks.add("\"Arts\"");
        }
        if (chkStyle.isChecked()) {
            newsDesks.add("\"Fashion and Style\"");
        }
        if (chkSports.isChecked()) {
            newsDesks.add("\"Sports\"");
        }
        SaveListener listener = (SaveListener) getActivity();
        listener.setFilters(calendar, sortParam, newsDesks);
        dismiss();
    }

    @OnClick(R.id.datepickerEt)
    public void showDatePicker(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datepicker");
    }

    public void updateDate(Calendar calendarSet) {
        if (calendarSet == null) {
            return;
        }
        calendar = calendarSet;

        SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd");
        datePickerEt.setText(format.format(calendarSet.getTime()));
    }

}
