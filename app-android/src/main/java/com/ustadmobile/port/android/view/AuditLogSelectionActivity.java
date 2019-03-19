package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AuditLogSelectionPresenter;
import com.ustadmobile.core.view.AuditLogSelectionView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class AuditLogSelectionActivity extends UstadBaseActivity implements AuditLogSelectionView,
        SelectClazzesDialogFragment.ClazzSelectDialogListener,
        SelectMultipleTreeDialogFragment.MultiSelectTreeDialogListener,
        SelectTwoDatesDialogFragment.CustomTimePeriodDialogListener {

    private Toolbar toolbar;
    private AuditLogSelectionPresenter mPresenter;
    private Spinner timePeriodSpinner;
    private TextView classesTextView;
    private TextView locationsTextView;
    private TextView personTextView;
    private TextView actorTextView;


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_audit_log_selection);

        //Toolbar:
        toolbar = findViewById(R.id.activity_audit_log_selection_toolbar);
        toolbar.setTitle(getText(R.string.audit_log));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        timePeriodSpinner = findViewById(R.id.activity_audit_log_time_period_spinner);
        classesTextView = findViewById(R.id.activity_audit_log_classes_edittext);
        locationsTextView = findViewById(R.id.activity_audit_log_location_edittext);
        personTextView = findViewById(R.id.activity_audit_log_person_edittext);
        actorTextView = findViewById(R.id.activity_audit_log_changed_by_edittext);


        //Call the Presenter
        mPresenter = new AuditLogSelectionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        updateClassesIfEmpty();
        updateLocationIfEmpty();
        updatePeopleIfEmpty();
        updateActorIfEmpty();


        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleTimePeriodSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        classesTextView.setOnClickListener(v -> mPresenter.goToSelectClassesDialog());

        locationsTextView.setOnClickListener(v -> mPresenter.goToLocationDialog());

        personTextView.setOnClickListener(v -> mPresenter.goToPersonDialog());

        actorTextView.setOnClickListener(v -> mPresenter.goToActorDialog());


        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_audit_log_selection_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }

    @Override
    public void onSelectClazzesResult(HashMap<String, Long> selectedClazzes) {
        StringBuilder classesSelectedString = new StringBuilder();
        Iterator<String> selectedClazzesNameIterator = selectedClazzes.keySet().iterator();
        while(selectedClazzesNameIterator.hasNext()){
            classesSelectedString.append(selectedClazzesNameIterator.next());
            if(selectedClazzesNameIterator.hasNext()){
                classesSelectedString.append(", ");
            }
        }
        List<Long> selectedClassesList = new ArrayList<>(selectedClazzes.values());
        mPresenter.setSelectedClasses(selectedClassesList);

        updateClazzesSelected(classesSelectedString.toString());
    }

    @Override
    public void onLocationResult(HashMap<String, Long> selectedLocations) {
        Iterator<String> selectedLocationsNameIterator = selectedLocations.keySet().iterator();
        StringBuilder locationsSelectedString = new StringBuilder();
        while(selectedLocationsNameIterator.hasNext()){
            locationsSelectedString.append(selectedLocationsNameIterator.next());
            if(selectedLocationsNameIterator.hasNext()){
                locationsSelectedString.append(", ");
            }
        }
        List<Long> selectedLocationList = new ArrayList<>(selectedLocations.values());
        mPresenter.setSelectedLocations(selectedLocationList);

        updateLocationsSelected(locationsSelectedString.toString());
    }

    @Override
    public void onCustomTimesResult(long from, long to) {
        mPresenter.setFromTime(from);
        mPresenter.setToTime(to);

        Toast.makeText(
                getApplicationContext(),
                "Custom date from : " + from + " to " + to,
                Toast.LENGTH_SHORT
        ).show();

    }

    /** Will add "ALL" to Classes if called
     *
     */
    public void updateClassesIfEmpty(){
        updateClazzesSelected(getText(R.string.all).toString());
        mPresenter.setSelectedClasses(null);
    }

    /** Will add "ALL" to Locations if called
     *
     */
    public void updateLocationIfEmpty(){
        updateLocationsSelected(getText(R.string.all).toString());
        mPresenter.setSelectedLocations(null);
    }

    /** Will add "ALL" to People if called
     *
     */
    public void updatePeopleIfEmpty(){
        updatePeopleSelected(getText(R.string.all).toString());
        mPresenter.setSelectedPeople(null);
    }

    /** Will add "ALL" to Actors if called
     *
     */
    public void updateActorIfEmpty(){
        updateActorsSelected(getText(R.string.all).toString());
        mPresenter.setSelectedActors(null);
    }


    @Override
    public void populateTimePeriod(HashMap<Integer, String> options) {

        String[] timePeriodPresets = options.values().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, timePeriodPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);
    }

    @Override
    public void updateLocationsSelected(String locations) {
        locationsTextView.setText(locations);
        if(locations.equals("")){
            updateLocationIfEmpty();
        }
    }

    @Override
    public void updateClazzesSelected(String clazzes) {
        classesTextView.setText(clazzes);
        if(clazzes.equals("")){
            updateClassesIfEmpty();
        }
    }

    @Override
    public void updatePeopleSelected(String people) {
        personTextView.setText(people);
        if(people.equals("")){
            updatePeopleIfEmpty();
        }
    }

    @Override
    public void updateActorsSelected(String actors) {
        actorTextView.setText(actors);
        if(actors.equals("")){
            updateActorIfEmpty();
        }
    }


}
