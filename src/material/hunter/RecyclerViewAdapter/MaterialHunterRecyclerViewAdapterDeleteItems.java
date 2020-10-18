package material.hunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import material.hunter.R;
import material.hunter.models.MaterialHunterModel;

import java.util.List;

public class MaterialHunterRecyclerViewAdapterDeleteItems extends RecyclerView.Adapter<MaterialHunterRecyclerViewAdapterDeleteItems.ItemViewHolder> {

    private static final String TAG = "MaterialHunterRecyclerView";
    private Context context;
    private List<MaterialHunterModel> nethunterModelList;

    public MaterialHunterRecyclerViewAdapterDeleteItems(Context context, List<MaterialHunterModel> nethunterModelList) {
        this.context = context;
        this.nethunterModelList = nethunterModelList;
    }

    @NonNull
    @Override
    public MaterialHunterRecyclerViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.nethunter_recyclerview_dialog_delete, viewGroup, false);
        return new MaterialHunterRecyclerViewAdapterDeleteItems.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.checkBox.setText(nethunterModelList.get(i).getTitle());
    }

    @Override
    public int getItemCount() {
        return nethunterModelList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;

        private ItemViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.f_nethunter_recyclerview_dialog_chkbox);
        }
    }

}