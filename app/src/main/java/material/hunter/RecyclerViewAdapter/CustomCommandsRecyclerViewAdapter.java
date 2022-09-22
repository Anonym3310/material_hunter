package material.hunter.RecyclerViewAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import material.hunter.R;
import material.hunter.RecyclerViewData.CustomCommandsData;
import material.hunter.SQL.CustomCommandsSQL;
import material.hunter.models.CustomCommandsModel;
import material.hunter.utils.PathsUtil;

import java.util.ArrayList;
import java.util.List;

public class CustomCommandsRecyclerViewAdapter extends RecyclerView.Adapter<CustomCommandsRecyclerViewAdapter.ItemViewHolder> implements Filterable {

    private static final String TAG = "CustomCommandsRecycleView";
    private Activity activity;
    private Context context;
    private List<CustomCommandsModel> customCommandsModelList;
    private Filter CustomCommandsModelListFilter =
            new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values =
                                new ArrayList<>(
                                        CustomCommandsData.getInstance()
                                                .customCommandsModelListFull);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<CustomCommandsModel> tempCustomCommandsModelList = new ArrayList<>();
                        for (CustomCommandsModel customCommandsModel :
                                CustomCommandsData.getInstance().customCommandsModelListFull) {
                            if (customCommandsModel
                                    .getLabel()
                                    .toLowerCase()
                                    .contains(filterPattern)) {
                                tempCustomCommandsModelList.add(customCommandsModel);
                            }
                        }
                        results.values = tempCustomCommandsModelList;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    CustomCommandsData.getInstance().getCustomCommandsModels().getValue().clear();
                    CustomCommandsData.getInstance()
                            .getCustomCommandsModels()
                            .getValue()
                            .addAll((List<CustomCommandsModel>) results.values);
                    CustomCommandsData.getInstance()
                            .getCustomCommandsModels()
                            .postValue(
                                    CustomCommandsData.getInstance()
                                            .getCustomCommandsModels()
                                            .getValue());
                }
            };

    public CustomCommandsRecyclerViewAdapter(
            Activity activity, Context context, List<CustomCommandsModel> customCommandsModelList) {
        this.activity = activity;
        this.context = context;
        this.customCommandsModelList = customCommandsModelList;
    }

    @NonNull
    @Override
    public CustomCommandsRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int i) {
        View view =
                LayoutInflater.from(context)
                        .inflate(R.layout.custom_commands_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.commandLabelTextView.setText(
                customCommandsModelList.get(position).getLabel());
        itemViewHolder.infoTextView.setText(
                customCommandsModelList.get(position).getEnv() + ", ");
        itemViewHolder.infoTextView.append(
                customCommandsModelList.get(position).getMode() + ", ");
        itemViewHolder.infoTextView.append(
                customCommandsModelList.get(position).getRunOnBoot().equals("1")
                        ? "run on boot"
                        : "don't run on boot");
        itemViewHolder.card.setOnLongClickListener(
                v -> {
                    final LayoutInflater mInflater =
                            (LayoutInflater)
                                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View promptViewEdit =
                            mInflater.inflate(R.layout.custom_commands_dialog_edit, null);
                    final TextInputEditText commandLabelEditText =
                            promptViewEdit.findViewById(
                                    R.id.f_customcommands_edit_adb_et_label);
                    final TextInputEditText commandEditText =
                            promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_et_command);
                    final Spinner sendToSpinner =
                            promptViewEdit.findViewById(R.id.f_customcommands_edit_adb_spr_sendto);
                    final Spinner execModeSpinner =
                            promptViewEdit.findViewById(
                                    R.id.f_customcommands_edit_adb_spr_execmode);
                    final SwitchMaterial runOnBootSwitch =
                            promptViewEdit.findViewById(
                                    R.id.f_customcommands_edit_adb_switch_runonboot);

                    commandLabelEditText.setText(
                            CustomCommandsData.getInstance()
                                    .customCommandsModelListFull
                                    .get(
                                            CustomCommandsData.getInstance()
                                                    .customCommandsModelListFull
                                                    .indexOf(customCommandsModelList.get(position)))
                                    .getLabel());
                    commandEditText.setText(
                            CustomCommandsData.getInstance()
                                    .customCommandsModelListFull
                                    .get(
                                            CustomCommandsData.getInstance()
                                                    .customCommandsModelListFull
                                                    .indexOf(customCommandsModelList.get(position)))
                                    .getCommand());
                    sendToSpinner.setSelection(
                            CustomCommandsData.getInstance()
                                            .customCommandsModelListFull
                                            .get(
                                                    CustomCommandsData.getInstance()
                                                            .customCommandsModelListFull
                                                            .indexOf(
                                                                    customCommandsModelList.get(
                                                                            position)))
                                            .getEnv()
                                            .equals("android")
                                    ? 0
                                    : 1);
                    execModeSpinner.setSelection(
                            CustomCommandsData.getInstance()
                                            .customCommandsModelListFull
                                            .get(
                                                    CustomCommandsData.getInstance()
                                                            .customCommandsModelListFull
                                                            .indexOf(
                                                                    customCommandsModelList.get(
                                                                            position)))
                                            .getMode()
                                            .equals("interactive")
                                    ? 0
                                    : 1);
                    runOnBootSwitch.setChecked(
                            CustomCommandsData.getInstance()
                                    .customCommandsModelListFull
                                    .get(
                                            CustomCommandsData.getInstance()
                                                    .customCommandsModelListFull
                                                    .indexOf(customCommandsModelList.get(position)))
                                    .getRunOnBoot()
                                    .equals("1"));

                    MaterialAlertDialogBuilder adbEdit = new MaterialAlertDialogBuilder(context);
                    adbEdit.setView(promptViewEdit);
                    adbEdit.setCancelable(true);
                    adbEdit.setPositiveButton("OK", (dialog, which) -> {});
                    final AlertDialog adEdit = adbEdit.create();
                    adEdit.setOnShowListener(
                            dialog -> {
                                final Button buttonEdit =
                                        adEdit.getButton(DialogInterface.BUTTON_POSITIVE);
                                buttonEdit.setOnClickListener(
                                        v1 -> {
                                            if (commandLabelEditText
                                                    .getText()
                                                    .toString()
                                                    .isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context, "Label cannot be empty", false);
                                            } else if (commandEditText
                                                    .getText()
                                                    .toString()
                                                    .isEmpty()) {
                                                PathsUtil.showMessage(
                                                        context,
                                                        "Command string cannot be empty",
                                                        false);
                                            } else {
                                                ArrayList<String> dataArrayList = new ArrayList<>();
                                                dataArrayList.add(
                                                        commandLabelEditText.getText().toString());
                                                dataArrayList.add(
                                                        commandEditText.getText().toString());
                                                dataArrayList.add(
                                                        sendToSpinner.getSelectedItem().toString());
                                                dataArrayList.add(
                                                        execModeSpinner
                                                                .getSelectedItem()
                                                                .toString());
                                                dataArrayList.add(
                                                        runOnBootSwitch.isChecked() ? "1" : "0");
                                                CustomCommandsData.getInstance()
                                                        .editData(
                                                                CustomCommandsData.getInstance()
                                                                        .customCommandsModelListFull
                                                                        .indexOf(
                                                                                customCommandsModelList
                                                                                        .get(
                                                                                                position)),
                                                                dataArrayList,
                                                                CustomCommandsSQL.getInstance(
                                                                        context));
                                                adEdit.dismiss();
                                            }
                                        });
                            });
                    adEdit.show();
                    return false;
                });
        itemViewHolder.runButton.setOnClickListener(
                v -> CustomCommandsData.getInstance().runCommandforitem(activity, context, position));
    }

    @Override
    public int getItemCount() {
        return customCommandsModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public Filter getFilter() {
        return CustomCommandsModelListFilter;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView card;
        private final TextView commandLabelTextView;
        private final TextView infoTextView;
        private final Button runButton;

        private ItemViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.custom_command_card);
            commandLabelTextView =
                    view.findViewById(R.id.f_customcommands_recyclerview_tv_cmdlabel);
            infoTextView =
                    view.findViewById(R.id.f_customcommands_recyclerview_tv_info);
            runButton = view.findViewById(R.id.f_customcommands_recyclerview_btn_run);
        }
    }
}