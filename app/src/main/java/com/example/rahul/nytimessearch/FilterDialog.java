package com.example.rahul.nytimessearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog for the filter options
 */
public class FilterDialog extends DialogFragment {

    @Bind(R.id.sort_order) Spinner orderSpinner;
    @Bind(R.id.btnSave) Button btnSave;
    @Bind(R.id.datepickerEt) EditText datePickerEt;

    public interface SaveListener {

        void setOrder(String order);
    }

    public FilterDialog() {
    }

    public static FilterDialog newInstance() {
        FilterDialog filterDialog = new FilterDialog();
        Bundle args = new Bundle();
        args.putString("title", "hello");
        filterDialog.setArguments(args);
        return filterDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container);;
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(adapter);
    }

    @OnClick(R.id.btnSave)
    public void saveFilters(View view) {
        System.out.println("Clicked save");
        SaveListener listener = (SaveListener) getActivity();
        listener.setOrder(orderSpinner.getSelectedItem().toString());
        // TODO(rahul): pass other filters here
        dismiss();
    }

    @OnClick(R.id.datepickerEt)
    public void showDatePicker(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datepicker");
    }

    public void updateDate(String date) {
        datePickerEt.setText(date);
    }

}
