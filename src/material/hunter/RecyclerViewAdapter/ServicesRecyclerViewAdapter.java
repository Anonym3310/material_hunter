package material.hunter.RecyclerViewAdapter;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import material.hunter.R;
import material.hunter.RecyclerViewData.ServicesData;
import material.hunter.SQL.ServicesSQL;
import material.hunter.models.ServicesModel;
import material.hunter.utils.NhPaths;

import java.util.ArrayList;
import java.util.List;

public class ServicesRecyclerViewAdapter extends RecyclerView.Adapter<ServicesRecyclerViewAdapter.ItemViewHolder> implements Filterable {
    private static final String TAG = "KaliServiceRecycleView";
    private Context context;
    private List<ServicesModel> servicesModelList;
    private Filter KaliServicesModelListFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<>(ServicesData.getInstance().servicesModelListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                List<ServicesModel> tempServicesModelList = new ArrayList<>();
                for (ServicesModel servicesModel : ServicesData.getInstance().servicesModelListFull) {
                    if (servicesModel.getServiceName().toLowerCase().contains(filterPattern)) {
                        tempServicesModelList.add(servicesModel);
                    }
                }
                results.values = tempServicesModelList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ServicesData.getInstance().getKaliServicesModels().getValue().clear();
            ServicesData.getInstance().getKaliServicesModels().getValue().addAll((List<ServicesModel>) results.values);
            ServicesData.getInstance().getKaliServicesModels().postValue(ServicesData.getInstance().getKaliServicesModels().getValue());
        }
    };

    public ServicesRecyclerViewAdapter(Context context, List<ServicesModel> servicesModelList) {
        this.context = context;
        this.servicesModelList = servicesModelList;
    }

    @NonNull
    @Override
    public ServicesRecyclerViewAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.services_recyclerview_servicetitle, viewGroup, false);
        return new ServicesRecyclerViewAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int position) {
        Spannable tempStatusTextView = new SpannableString(servicesModelList.get(position).getStatus());
        tempStatusTextView.setSpan(new ForegroundColorSpan(servicesModelList.get(position).getStatus().startsWith("[+]") ? Color.GREEN : Color.parseColor("#D81B60")), 0, servicesModelList.get(position).getStatus().length(), 0);
        itemViewHolder.nametextView.setText(servicesModelList.get(position).getServiceName());
        //itemViewHolder.runOnChrootStartCheckbox.setChecked(servicesModelList.get(position).getRunOnChrootStart().equals("1"));
        itemViewHolder.mSwitch.setChecked(servicesModelList.get(position).getStatus().startsWith("[+]"));
        itemViewHolder.statustextView.setText(tempStatusTextView);
        itemViewHolder.nametextView.setOnLongClickListener(v -> {
            final ViewGroup nullParent = null;
            final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View promptViewEdit = mInflater.inflate(R.layout.services_edit_dialog_view, nullParent);
            final EditText titleEditText = promptViewEdit.findViewById(R.id.f_services_edit_adb_et_title);
            final EditText startCmdEditText = promptViewEdit.findViewById(R.id.f_services_edit_adb_et_startcommand);
            final EditText stopCmdEditText = promptViewEdit.findViewById(R.id.f_services_edit_adb_et_stopcommand);
            final EditText checkstatusCmdEditText = promptViewEdit.findViewById(R.id.f_services_edit_adb_et_checkstatuscommand);
            final CheckBox runOnChrootStartCheckbox = promptViewEdit.findViewById(R.id.f_services_edit_adb_checkbox_runonboot);
            final FloatingActionButton readmeButton1 = promptViewEdit.findViewById(R.id.f_services_edit_btn_info_fab1);
            final FloatingActionButton readmeButton2 = promptViewEdit.findViewById(R.id.f_services_edit_btn_info_fab2);
            final FloatingActionButton readmeButton3 = promptViewEdit.findViewById(R.id.f_services_edit_btn_info_fab3);
            final FloatingActionButton readmeButton4 = promptViewEdit.findViewById(R.id.f_services_edit_btn_info_fab4);

            readmeButton1.setOnClickListener(view -> {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.services_howto_startservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final androidx.appcompat.app.AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton2.setOnClickListener(view -> {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.services_howto_stopservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final androidx.appcompat.app.AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton3.setOnClickListener(view -> {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.services_howto_checkservice))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final androidx.appcompat.app.AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            readmeButton4.setOnClickListener(view -> {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(context);
                adb.setTitle("HOW TO USE:")
                        .setMessage(context.getString(R.string.services_howto_runServiceOnBoot))
                        .setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                final androidx.appcompat.app.AlertDialog ad = adb.create();
                ad.setCancelable(true);
                ad.show();
            });

            titleEditText.setText(ServicesData.getInstance().servicesModelListFull.get(
                    ServicesData.getInstance().servicesModelListFull.indexOf(
                            servicesModelList.get(position))).getServiceName());
            startCmdEditText.setText(ServicesData.getInstance().servicesModelListFull.get(
                    ServicesData.getInstance().servicesModelListFull.indexOf(
                            servicesModelList.get(position))).getCommandforStartService());
            stopCmdEditText.setText(ServicesData.getInstance().servicesModelListFull.get(
                    ServicesData.getInstance().servicesModelListFull.indexOf(
                            servicesModelList.get(position))).getCommandforStopService());
            checkstatusCmdEditText.setText(ServicesData.getInstance().servicesModelListFull.get(
                    ServicesData.getInstance().servicesModelListFull.indexOf(
                            servicesModelList.get(position))).getCommandforCheckServiceStatus());
            runOnChrootStartCheckbox.setChecked(ServicesData.getInstance().servicesModelListFull.get(
                    ServicesData.getInstance().servicesModelListFull.indexOf(
                            servicesModelList.get(position))).getRunOnChrootStart().equals("1"));
            AlertDialog.Builder adbEdit = new AlertDialog.Builder(context);
            adbEdit.setView(promptViewEdit);
            adbEdit.setCancelable(true);
            adbEdit.setPositiveButton("OK", (dialog, which) -> {
            });
            final AlertDialog adEdit = adbEdit.create();
            adEdit.setOnShowListener(dialog -> {
                final Button buttonEdit = adEdit.getButton(DialogInterface.BUTTON_POSITIVE);
                buttonEdit.setOnClickListener(v1 -> {
                    if (titleEditText.getText().toString().isEmpty()) {
                        NhPaths.showMessage(context, "Title cannot be empty");
                    } else if (startCmdEditText.getText().toString().isEmpty()) {
                        NhPaths.showMessage(context, "Start Command cannot be empty");
                    } else if (stopCmdEditText.getText().toString().isEmpty()) {
                        NhPaths.showMessage(context, "Stop Command cannot be empty");
                    } else if (checkstatusCmdEditText.getText().toString().isEmpty()) {
                        NhPaths.showMessage(context, "String cannot be empty");
                    } else {
                        ArrayList<String> dataArrayList = new ArrayList<>();
                        dataArrayList.add(titleEditText.getText().toString());
                        dataArrayList.add(startCmdEditText.getText().toString());
                        dataArrayList.add(stopCmdEditText.getText().toString());
                        dataArrayList.add(checkstatusCmdEditText.getText().toString());
                        dataArrayList.add(runOnChrootStartCheckbox.isChecked() ? "1" : "0");
                        ServicesData.getInstance().editData(ServicesData.getInstance().servicesModelListFull.indexOf(
                                servicesModelList.get(position)), dataArrayList, ServicesSQL.getInstance(context));
                        adEdit.dismiss();
                    }
                });
            });
            adEdit.show();
            return false;
        });

        //itemViewHolder.runOnChrootStartCheckbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            //ArrayList<String> dataArrayList = new ArrayList<>();
            //dataArrayList.add(servicesModelList.get(position).getServiceName());
            //dataArrayList.add(servicesModelList.get(position).getCommandforStartService());
            //dataArrayList.add(servicesModelList.get(position).getCommandforStopService());
            //dataArrayList.add(servicesModelList.get(position).getCommandforCheckServiceStatus());
            //if (isChecked) {
                //dataArrayList.add("1");
            //} else {
                //dataArrayList.add("0");
            //}
            //ServicesData.getInstance().updateRunOnChrootStartServices(position, dataArrayList, ServicesSQL.getInstance(context));
        //});

        itemViewHolder.mSwitch.setOnClickListener(v -> {
            if (itemViewHolder.mSwitch.isChecked()) {
                ServicesData.getInstance().startServiceforItem(position, itemViewHolder.mSwitch, context);
            } else {
                ServicesData.getInstance().stopServiceforItem(position, itemViewHolder.mSwitch, context);
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
        return KaliServicesModelListFilter;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView nametextView;
        private Switch mSwitch;
        //private CheckBox runOnChrootStartCheckbox;
        private TextView statustextView;

        private ItemViewHolder(View view) {
            super(view);
            nametextView = view.findViewById(R.id.f_services_recyclerview_servicetitle_tv);
            //runOnChrootStartCheckbox = view.findViewById(R.id.f_services_recyclerview_runonchrootstart_checkbox);
            mSwitch = view.findViewById(R.id.f_services_recyclerview_switch_toggle);
            statustextView = view.findViewById(R.id.f_services_recyclerview_serviceresult_tv);
        }
    }
}