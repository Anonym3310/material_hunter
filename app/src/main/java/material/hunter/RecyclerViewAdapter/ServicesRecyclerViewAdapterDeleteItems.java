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
import material.hunter.models.ServicesModel;

public class ServicesRecyclerViewAdapterDeleteItems
    extends RecyclerView.Adapter<ServicesRecyclerViewAdapterDeleteItems.ItemViewHolder> {

  private final Context context;
  private final List<ServicesModel> servicesModelList;

  public ServicesRecyclerViewAdapterDeleteItems(
      Context context, List<ServicesModel> servicesModelList) {
    this.context = context;
    this.servicesModelList = servicesModelList;
  }

  @NonNull
  @Override
  public ServicesRecyclerViewAdapterDeleteItems.ItemViewHolder onCreateViewHolder(
      @NonNull ViewGroup viewGroup, int i) {
    View view =
        LayoutInflater.from(context)
            .inflate(R.layout.recyclerview_dialog_delete, viewGroup, false);
    return new ItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
    itemViewHolder.runOnChrootStartCheckBox.setText(servicesModelList.get(i).getServiceName());
  }

  @Override
  public int getItemCount() {
    return servicesModelList.size();
  }

  static class ItemViewHolder extends RecyclerView.ViewHolder {
    private final CheckBox runOnChrootStartCheckBox;

    private ItemViewHolder(View view) {
      super(view);
      runOnChrootStartCheckBox = view.findViewById(R.id.checkbox);
    }
  }
}