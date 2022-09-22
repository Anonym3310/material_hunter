package material.hunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import material.hunter.RecyclerViewAdapter.ServicesRecyclerViewAdapter;
import material.hunter.RecyclerViewAdapter.ServicesRecyclerViewAdapterDeleteItems;
import material.hunter.RecyclerViewData.ServicesData;
import material.hunter.SQL.ServicesSQL;
import material.hunter.models.ServicesModel;
import material.hunter.utils.PathsUtil;
import material.hunter.viewmodels.ServicesViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Services extends ThemedActivity {

    private Activity activity;
    private Context context;
    private ServicesRecyclerViewAdapter adapter;
    private Button addButton;
    private Button deleteButton;
    private Button moveButton;
    private ActionBar actionBar;
    private static int targetPositionId;

    public static View _view;

    MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;

        setContentView(R.layout.services_activity);

        _view = getWindow().getDecorView();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ServicesViewModel servicesViewModel =
                ViewModelProviders.of(this).get(ServicesViewModel.class);
        servicesViewModel.init(context);
        servicesViewModel
                .getLiveDataServicesModelList()
                .observe(this, ServicesModelList -> adapter.notifyDataSetChanged());

        adapter =
                new ServicesRecyclerViewAdapter(
                        context, servicesViewModel.getLiveDataServicesModelList().getValue());
        RecyclerView recyclerViewServiceTitle =
                findViewById(R.id.f_services_recyclerviewServiceTitle);
        recyclerViewServiceTitle.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerViewServiceTitle.setAdapter(adapter);

        addButton = findViewById(R.id.f_services_addItemButton);
        deleteButton = findViewById(R.id.f_services_deleteItemButton);
        moveButton = findViewById(R.id.f_services_moveItemButton);

        SwipeRefreshLayout o = findViewById(R.id.f_services_scrollView);
        o.setOnRefreshListener(
                () -> {
                    ServicesData.getInstance().refreshData();
                    new Handler(Looper.getMainLooper())
                            .postDelayed(() -> o.setRefreshing(false), 512);
                });

        File sql_folder = new File(PathsUtil.APP_SD_SQLBACKUP_PATH);
        if (!sql_folder.exists()) {
            PathsUtil.showSnack(_view, "Creating directory for backing up dbs...", false);
            try {
                sql_folder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                PathsUtil.showSnack(
                        _view,
                        "Failed to create directory " + PathsUtil.APP_SD_SQLBACKUP_PATH,
                        false);
            }
        }

        addItem();
        deleteItems();
        moveItems();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.services, menu);
        final MenuItem searchItem = menu.findItem(R.id.f_services_action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnSearchClickListener(
                v -> menu.setGroupVisible(R.id.f_services_menu_group1, false));
        searchView.setOnCloseListener(
                () -> {
                    menu.setGroupVisible(R.id.f_services_menu_group1, true);
                    return false;
                });
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.input_dialog_view, null);
        final EditText storedpathEditText = promptView.findViewById(R.id.cdw_et);

        switch (item.getItemId()) {
            case R.id.f_services_menu_backupDB:
                storedpathEditText.setText(PathsUtil.APP_SD_SQLBACKUP_PATH + "/FragmentServices");
                MaterialAlertDialogBuilder adbBackup = new MaterialAlertDialogBuilder(activity);
                adbBackup.setTitle("Full path to where you want to save the database:");
                adbBackup.setView(promptView);
                adbBackup.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbBackup.setPositiveButton("OK", (dialog, which) -> {});
                final AlertDialog adBackup = adbBackup.create();
                adBackup.setOnShowListener(
                        dialog -> {
                            final Button buttonOK =
                                    adBackup.getButton(DialogInterface.BUTTON_POSITIVE);
                            buttonOK.setOnClickListener(
                                    v -> {
                                        String returnedResult =
                                                ServicesData.getInstance()
                                                        .backupData(
                                                                ServicesSQL.getInstance(context),
                                                                storedpathEditText
                                                                        .getText()
                                                                        .toString());
                                        if (returnedResult == null) {
                                            PathsUtil.showSnack(
                                                    _view,
                                                    "db is successfully backup to "
                                                            + storedpathEditText
                                                                    .getText()
                                                                    .toString(),
                                                    false);
                                        } else {
                                            dialog.dismiss();
                                            new MaterialAlertDialogBuilder(context)
                                                    .setTitle("Failed to backup the DB.")
                                                    .setMessage(returnedResult)
                                                    .create()
                                                    .show();
                                        }
                                        dialog.dismiss();
                                    });
                        });
                adBackup.show();
                break;
            case R.id.f_services_menu_restoreDB:
                storedpathEditText.setText(PathsUtil.APP_SD_SQLBACKUP_PATH + "/FragmentServices");
                MaterialAlertDialogBuilder adbRestore = new MaterialAlertDialogBuilder(activity);
                adbRestore.setTitle("Full path of the db file from where you want to restore:");
                adbRestore.setView(promptView);
                adbRestore.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                adbRestore.setPositiveButton("OK", (dialog, which) -> {});
                final AlertDialog adRestore = adbRestore.create();
                adRestore.setOnShowListener(
                        dialog -> {
                            final Button buttonOK =
                                    adRestore.getButton(DialogInterface.BUTTON_POSITIVE);
                            buttonOK.setOnClickListener(
                                    v -> {
                                        String returnedResult =
                                                ServicesData.getInstance()
                                                        .restoreData(
                                                                ServicesSQL.getInstance(context),
                                                                storedpathEditText
                                                                        .getText()
                                                                        .toString());
                                        if (returnedResult == null) {
                                            PathsUtil.showSnack(
                                                    _view,
                                                    "db is successfully restored to "
                                                            + storedpathEditText
                                                                    .getText()
                                                                    .toString(),
                                                    false);
                                        } else {
                                            dialog.dismiss();
                                            new MaterialAlertDialogBuilder(context)
                                                    .setTitle("Failed to restore the DB.")
                                                    .setMessage(returnedResult)
                                                    .create()
                                                    .show();
                                        }
                                        dialog.dismiss();
                                    });
                        });
                adRestore.show();
                break;
            case R.id.f_services_menu_ResetToDefault:
                ServicesData.getInstance().resetData(ServicesSQL.getInstance(context));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        ServicesData.getInstance().refreshData();
    }

    private void addItem() {
        addButton.setOnClickListener(
                v -> {
                    List<ServicesModel> servicesModelList =
                            ServicesData.getInstance().servicesModelListFull;
                    if (servicesModelList == null) return;
                    final LayoutInflater inflater =
                            (LayoutInflater)
                                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View promptViewAdd = inflater.inflate(R.layout.services_dialog_add, null);
                    final EditText titleEditText =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_et_title);
                    final EditText startCmdEditText =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_et_startcommand);
                    final EditText stopCmdEditText =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_et_stopcommand);
                    final EditText checkstatusCmdEditText =
                            promptViewAdd.findViewById(
                                    R.id.f_services_add_adb_et_checkstatuscommand);
                    final SwitchMaterial runOnChrootStartSwitch =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_switch_runonboot);
                    final Spinner insertPositions =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_spr_positions);
                    final Spinner insertTitles =
                            promptViewAdd.findViewById(R.id.f_services_add_adb_spr_titles);

                    ArrayList<String> serviceNameArrayList = new ArrayList<>();
                    for (ServicesModel servicesModel : servicesModelList) {
                        serviceNameArrayList.add(servicesModel.getServiceName());
                    }

                    ArrayAdapter<String> arrayAdapter =
                            new ArrayAdapter<>(
                                    context,
                                    android.R.layout.simple_spinner_item,
                                    serviceNameArrayList);
                    arrayAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);

                    insertPositions.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(
                                        AdapterView<?> parent, View view, int position, long id) {
                                    // if Insert to Top
                                    if (position == 0) {
                                        insertTitles.setVisibility(View.INVISIBLE);
                                        targetPositionId = 1;
                                        // if Insert to Bottom
                                    } else if (position == 1) {
                                        insertTitles.setVisibility(View.INVISIBLE);
                                        targetPositionId = servicesModelList.size() + 1;
                                        // if Insert Before
                                    } else if (position == 2) {
                                        insertTitles.setVisibility(View.VISIBLE);
                                        insertTitles.setAdapter(arrayAdapter);
                                        insertTitles.setOnItemSelectedListener(
                                                new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(
                                                            AdapterView<?> parent,
                                                            View view,
                                                            int position,
                                                            long id) {
                                                        targetPositionId = position + 1;
                                                    }

                                                    @Override
                                                    public void onNothingSelected(
                                                            AdapterView<?> parent) {}
                                                });
                                        // if Insert After
                                    } else {
                                        insertTitles.setVisibility(View.VISIBLE);
                                        insertTitles.setAdapter(arrayAdapter);
                                        insertTitles.setOnItemSelectedListener(
                                                new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(
                                                            AdapterView<?> parent,
                                                            View view,
                                                            int position,
                                                            long id) {
                                                        targetPositionId = position + 2;
                                                    }

                                                    @Override
                                                    public void onNothingSelected(
                                                            AdapterView<?> parent) {}
                                                });
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });

                    MaterialAlertDialogBuilder adbAdd = new MaterialAlertDialogBuilder(activity);
                    adbAdd.setPositiveButton("OK", (dialog, which) -> {});
                    final AlertDialog adAdd = adbAdd.create();
                    adAdd.setView(promptViewAdd);
                    adAdd.setCancelable(true);
                    adAdd.setOnShowListener(
                            dialog -> {
                                final Button buttonAdd =
                                        adAdd.getButton(DialogInterface.BUTTON_POSITIVE);
                                buttonAdd.setOnClickListener(
                                        v1 -> {
                                            if (titleEditText.getText().toString().isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context, "Title cannot be empty", false);
                                            } else if (startCmdEditText
                                                    .getText()
                                                    .toString()
                                                    .isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context,
                                                        "Start Command cannot be empty",
                                                        false);
                                            } else if (stopCmdEditText
                                                    .getText()
                                                    .toString()
                                                    .isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context,
                                                        "Stop Command cannot be empty",
                                                        false);
                                            } else if (checkstatusCmdEditText
                                                    .getText()
                                                    .toString()
                                                    .isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context,
                                                        "Check Status Command cannot be empty",
                                                        false);
                                            } else {
                                                ArrayList<String> dataArrayList = new ArrayList<>();
                                                dataArrayList.add(
                                                        titleEditText.getText().toString());
                                                dataArrayList.add(
                                                        startCmdEditText.getText().toString());
                                                dataArrayList.add(
                                                        stopCmdEditText.getText().toString());
                                                dataArrayList.add(
                                                        checkstatusCmdEditText
                                                                .getText()
                                                                .toString());
                                                dataArrayList.add(
                                                        runOnChrootStartSwitch.isChecked()
                                                                ? "1"
                                                                : "0");
                                                ServicesData.getInstance()
                                                        .addData(
                                                                targetPositionId,
                                                                dataArrayList,
                                                                ServicesSQL.getInstance(context));
                                                adAdd.dismiss();
                                            }
                                        });
                            });
                    adAdd.show();
                });
    }

    private void deleteItems() {
        deleteButton.setOnClickListener(
                v -> {
                    List<ServicesModel> servicesModelList =
                            ServicesData.getInstance().servicesModelListFull;
                    if (servicesModelList == null) return;
                    final LayoutInflater inflater =
                            (LayoutInflater)
                                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View promptViewDelete =
                            inflater.inflate(R.layout.dialog_delete, null, false);
                    final RecyclerView recyclerViewDeleteItem =
                            promptViewDelete.findViewById(R.id.recyclerview);
                    ServicesRecyclerViewAdapterDeleteItems servicesRecyclerViewAdapterDeleteItems =
                            new ServicesRecyclerViewAdapterDeleteItems(context, servicesModelList);

                    LinearLayoutManager linearLayoutManagerDelete =
                            new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    recyclerViewDeleteItem.setLayoutManager(linearLayoutManagerDelete);
                    recyclerViewDeleteItem.setAdapter(servicesRecyclerViewAdapterDeleteItems);

                    MaterialAlertDialogBuilder adbDelete = new MaterialAlertDialogBuilder(activity);
                    adbDelete.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    adbDelete.setPositiveButton("Delete", (dialog, which) -> {});
                    final AlertDialog adDelete = adbDelete.create();
                    adDelete.setMessage("Select the service you want to remove: ");
                    adDelete.setView(promptViewDelete);
                    adDelete.setCancelable(true);
                    // If you want the dialog to stay open after clicking OK, you need to do it this
                    // way...
                    adDelete.setOnShowListener(
                            dialog -> {
                                final Button buttonDelete =
                                        adDelete.getButton(DialogInterface.BUTTON_POSITIVE);
                                buttonDelete.setOnClickListener(
                                        v1 -> {
                                            RecyclerView.ViewHolder viewHolder;
                                            ArrayList<Integer> selectedPosition = new ArrayList<>();
                                            ArrayList<Integer> selectedTargetIds =
                                                    new ArrayList<>();
                                            for (int i = 0;
                                                    i < recyclerViewDeleteItem.getChildCount();
                                                    i++) {
                                                viewHolder =
                                                        recyclerViewDeleteItem
                                                                .findViewHolderForAdapterPosition(
                                                                        i);
                                                if (viewHolder != null) {
                                                    CheckBox box =
                                                            viewHolder.itemView.findViewById(
                                                                    R.id.itemCheckBox);
                                                    if (box.isChecked()) {
                                                        selectedPosition.add(i);
                                                        selectedTargetIds.add(i + 1);
                                                    }
                                                }
                                            }
                                            if (selectedPosition.size() != 0) {
                                                ServicesData.getInstance()
                                                        .deleteData(
                                                                selectedPosition,
                                                                selectedTargetIds,
                                                                ServicesSQL.getInstance(context));
                                                PathsUtil.showSnack(
                                                        _view,
                                                        "Successfully deleted "
                                                                + selectedPosition.size()
                                                                + " items.",
                                                        false);
                                                adDelete.dismiss();
                                            } else {
                                                PathsUtil.showMessage(
                                                        context, "Nothing to be deleted.", false);
                                            }
                                        });
                            });
                    adDelete.show();
                });
    }

    private void moveItems() {
        moveButton.setOnClickListener(
                v -> {
                    List<ServicesModel> servicesModelList =
                            ServicesData.getInstance().servicesModelListFull;
                    if (servicesModelList == null) return;
                    final LayoutInflater inflater =
                            (LayoutInflater)
                                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View promptViewMove = inflater.inflate(R.layout.dialog_move, null, false);
                    final Spinner titlesBefore =
                            promptViewMove.findViewById(R.id.move_titlesbefore);
                    final Spinner titlesAfter = promptViewMove.findViewById(R.id.move_titlesafter);
                    final Spinner actions = promptViewMove.findViewById(R.id.move_actions);

                    ArrayList<String> serviceNameArrayList = new ArrayList<>();
                    for (ServicesModel servicesModel : servicesModelList) {
                        serviceNameArrayList.add(servicesModel.getServiceName());
                    }

                    ArrayAdapter<String> arrayAdapter =
                            new ArrayAdapter<>(
                                    context,
                                    android.R.layout.simple_spinner_item,
                                    serviceNameArrayList);
                    arrayAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    titlesBefore.setAdapter(arrayAdapter);
                    titlesAfter.setAdapter(arrayAdapter);

                    MaterialAlertDialogBuilder adbMove = new MaterialAlertDialogBuilder(activity);
                    adbMove.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    adbMove.setPositiveButton("Move", (dialog, which) -> {});
                    final AlertDialog adMove = adbMove.create();
                    adMove.setView(promptViewMove);
                    adMove.setCancelable(true);
                    adMove.setOnShowListener(
                            dialog -> {
                                final Button buttonMove =
                                        adMove.getButton(DialogInterface.BUTTON_POSITIVE);
                                buttonMove.setOnClickListener(
                                        v1 -> {
                                            int originalPositionIndex =
                                                    titlesBefore.getSelectedItemPosition();
                                            int targetPositionIndex =
                                                    titlesAfter.getSelectedItemPosition();
                                            if (originalPositionIndex == targetPositionIndex
                                                    || (actions.getSelectedItemPosition() == 0
                                                            && targetPositionIndex
                                                                    == (originalPositionIndex + 1))
                                                    || (actions.getSelectedItemPosition() == 1
                                                            && targetPositionIndex
                                                                    == (originalPositionIndex
                                                                            - 1))) {
                                                PathsUtil.showMessage(
                                                        context,
                                                        "You are moving the item to the same"
                                                                + " position, nothing to be moved.",
                                                        false);
                                            } else {
                                                if (actions.getSelectedItemPosition() == 1)
                                                    targetPositionIndex += 1;
                                                ServicesData.getInstance()
                                                        .moveData(
                                                                originalPositionIndex,
                                                                targetPositionIndex,
                                                                ServicesSQL.getInstance(context));
                                                PathsUtil.showSnack(
                                                        _view, "Successfully moved item.", false);
                                                adMove.dismiss();
                                            }
                                        });
                            });
                    adMove.show();
                });
    }
}
