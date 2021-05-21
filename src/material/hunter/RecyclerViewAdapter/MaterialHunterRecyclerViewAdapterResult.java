package material.hunter.RecyclerViewAdapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import material.hunter.R;
import material.hunter.utils.NhPaths;

public class MaterialHunterRecyclerViewAdapterResult extends RecyclerView.Adapter<MaterialHunterRecyclerViewAdapterResult.ItemViewHolder> {

    private final String[] resultStrings;
    private final Context context;

    public MaterialHunterRecyclerViewAdapterResult(Context context, String[] resultStrings) {
        this.context = context;
        this.resultStrings = resultStrings;
    }

    @NonNull
    @Override
    public MaterialHunterRecyclerViewAdapterResult.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.materialhunter_recyclerview_result, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialHunterRecyclerViewAdapterResult.ItemViewHolder holder, int position) {
        holder.resultTextView.setText(resultStrings[position]);
        holder.resultTextView.setOnLongClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData cData = ClipData.newPlainText("text", holder.resultTextView.getText());
            cm.setPrimaryClip(cData);
            NhPaths.showMessage(context, "Copied to clipboard: " + holder.resultTextView.getText());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return resultStrings.length;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView resultTextView;

        private ItemViewHolder(View view) {
            super(view);
            resultTextView = view.findViewById(R.id.f_materialhunter_item_result_tv);
        }
    }
}
