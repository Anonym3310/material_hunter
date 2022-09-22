package material.hunter.RecyclerViewAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import material.hunter.models.LicenseModel;
import material.hunter.R;

import java.util.List;

public class LicensesRecyclerViewAdapter
        extends RecyclerView.Adapter<LicensesRecyclerViewAdapter.ViewHolder> {

    private Activity activity;
    private Context context;
    private List<LicenseModel> list;

    public LicensesRecyclerViewAdapter(
            Activity activity, Context context, List<LicenseModel> list) {
        this.activity = activity;
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.licenses_item,
                                parent,
                                false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        LicenseModel model = list.get(position);
        holder.title.setText(model.getTitle());
        holder.license.setText(model.getLicense());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView license;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            license = v.findViewById(R.id.license);
        }
    }
}