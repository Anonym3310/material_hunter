package material.hunter.RecyclerViewAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import material.hunter.R;
import material.hunter.RecyclerViewData.ServicesData;
import material.hunter.SQL.ServicesSQL;
import material.hunter.models.ServicesModel;
import material.hunter.utils.PathsUtil;

import java.util.ArrayList;
import java.util.List;

public class ServicesRecyclerViewAdapter extends RecyclerView.Adapter<ServicesRecyclerViewAdapter.ItemViewHolder> implements Filterable {

    private final Context context;
    private final List<ServicesModel> servicesModelList;
    private final Filter ServicesModelListFilter =
            new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values =
                                new ArrayList<>(ServicesData.getInstance().servicesModelListFull);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<ServicesModel> tempServicesModelList = new ArrayList<>();
                        for (ServicesModel servicesModel :
                                ServicesData.getInstance().servicesModelListFull) {
                            if (servicesModel
                                    .getServiceName()
                                    .toLowerCase()
                                    .contains(filterPattern)) {
                                tempServicesModelList.add(servicesModel);
                            }
                        }
                        results.values = tempServicesModelList;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    ServicesData.getInstance().getServicesModels().getValue().clear();
                    ServicesData.getInstance()
                            .getServicesModels()
                            .getValue()
                            .addAll((List<ServicesModel>) results.values);
                    ServicesData.getInstance()
                            .getServicesModels()
                            .postValue(ServicesData.getInstance().getServicesModels().getValue());
                }
            };

    public ServicesRecyclerViewAdapter(Context context, List<ServicesModel> servicesModelList) {
        this.context = context;
        this.servicesModelList = servicesModelList;
    }

    @NonNull
    @Override
    public ServicesRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int i) {
        View view =
                LayoutInflater.from(context)
                        .inflate(R.layout.services_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int position) {
        Spannable tempStatusTextView =
                new SpannableString(servicesModelList.get(position).getStatus());
        tempStatusTextView.setSpan(
                new ForegroundColorSpan(
                        servicesModelList.get(position).getStatus().startsWith("[+]")
                                ? Color.GREEN
                                : Color.parseColor("#D81B60")),
                0,
                servicesModelList.get(position).getStatus().length(),
                0);
        itemViewHolder.nametextView.setText(servicesModelList.get(position).getServiceName());
        itemViewHolder.mSwitch.setChecked(
                servicesModelList.get(position).getStatus().startsWith("[+]"));
        itemViewHolder.statustextView.setText(tempStatusTextView);
        itemViewHolder.card.setOnLongClickListener(
                v -> {
                    final ViewGroup nullParent = null;
                    final LayoutInflater mInflater =
                            (LayoutInflater)
                                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View promptViewEdit =
                            mInflater.inflate(R.layout.services_dialog_edit, nullParent);
                    final TextInputEditText titleEditText =
                            promptViewEdit.findViewById(R.id.f_services_edit_adb_et_title);
                    final TextInputEditText startCmdEditText =
                            promptViewEdit.findViewById(R.id.f_services_edit_adb_et_startcommand);
                    final TextInputEditText stopCmdEditText =
                            promptViewEdit.findViewById(R.id.f_services_edit_adb_et_stopcommand);
                    final TextInputEditText checkstatusCmdEditText =
                            promptViewEdit.findViewById(
                                    R.id.f_services_edit_adb_et_checkstatuscommand);
                    final SwitchMaterial runOnChrootStartSwitch =
                            promptViewEdit.findViewById(
                                    R.id.f_services_edit_adb_switch_runonboot);

                    titleEditText.setText(
                            ServicesData.getInstance()
                                    .servicesModelListFull
                                    .get(
                                            ServicesData.getInstance()
                                                    .servicesModelListFull
                                                    .indexOf(servicesModelList.get(position)))
                                    .getServiceName());
                    startCmdEditText.setText(
                            ServicesData.getInstance()
                                    .servicesModelListFull
                                    .get(
                                            ServicesData.getInstance()
                                                    .servicesModelListFull
                                                    .indexOf(servicesModelList.get(position)))
                                    .getCommandforStartService());
                    stopCmdEditText.setText(
                            ServicesData.getInstance()
                                    .servicesModelListFull
                                    .get(
                                            ServicesData.getInstance()
                                                    .servicesModelListFull
                                                    .indexOf(servicesModelList.get(position)))
                                    .getCommandforStopService());
                    checkstatusCmdEditText.setText(
                            ServicesData.getInstance()
                                    .servicesModelListFull
                                    .get(
                                            ServicesData.getInstance()
                                                    .servicesModelListFull
                                                    .indexOf(servicesModelList.get(position)))
                                    .getCommandforCheckServiceStatus());
                    runOnChrootStartSwitch.setChecked(
                            ServicesData.getInstance()
                                    .servicesModelListFull
                                    .get(
                                            ServicesData.getInstance()
                                                    .servicesModelListFull
                                                    .indexOf(servicesModelList.get(position)))
                                    .getRunOnChrootStart()
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
                                                        context, "String cannot be empty", false);
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
                                                        .editData(
                                                                ServicesData.getInstance()
                                                                        .servicesModelListFull
                                                                        .indexOf(
                                                                                servicesModelList
                                                                                        .get(
                                                                                                position)),
                                                                dataArrayList,
                                                                ServicesSQL.getInstance(context));
                                                adEdit.dismiss();
                                            }
                                        });
                            });
                    adEdit.show();
                    return false;
                });

        itemViewHolder.mSwitch.setOnClickListener(
                v -> {
                    if (itemViewHolder.mSwitch.isChecked()) {
                        ServicesData.getInstance()
                                .startServiceforItem(position, itemViewHolder.mSwitch, context);
                    } else {
                        ServicesData.getInstance()
                                .stopServiceforItem(position, itemViewHolder.mSwitch, context);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return servicesModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public Filter getFilter() {
        return ServicesModelListFilter;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView card;
        private final TextView nametextView;
        private final TextView statustextView;
        private SwitchMaterial mSwitch;

        private ItemViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.service_card);
            nametextView = view.findViewById(R.id.f_services_recyclerview_servicetitle_tv);
            mSwitch = view.findViewById(R.id.f_services_recyclerview_switch_toggle);
            statustextView = view.findViewById(R.id.f_services_recyclerview_serviceresult_tv);
        }
    }
}