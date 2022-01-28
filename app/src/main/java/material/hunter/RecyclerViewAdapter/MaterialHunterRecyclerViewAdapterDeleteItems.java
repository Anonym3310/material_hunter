package material.hunter.RecyclerViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import material.hunter.R;
import material.hunter.models.MaterialHunterModel;

public class MaterialHunterRecyclerViewAdapterDeleteItems extends RecyclerView.Adapter<MaterialHunterRecyclerViewAdapterDeleteItems.ItemViewHolder> {

    private final Context context;
    private final List<MaterialHunterModel> materialhunterModelList;

    public MaterialHunterRecyclerViewAdapterDeleteItems(Context context, List<MaterialHunterModel> materialhunterModelList) {
        this.context = context;
        this.materialhunterModelList = materialhunterModelList;
    }

    @NonNull
    @Override
    public MaterialHunterRecyclerViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.materialhunter_recyclerview_dialog_delete, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.checkBox.setText(materialhunterModelList.get(i).getTitle());
    }

    @Override
    public int getItemCount() {
        return materialhunterModelList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        private ItemViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.f_materialhunter_recyclerview_dialog_chkbox);
        }
    }
}