package material.hunter.RecyclerViewAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import material.hunter.models.AuthorsModel;
import material.hunter.R;

import java.util.List;

public class AuthorsRecyclerViewAdapter
        extends RecyclerView.Adapter<AuthorsRecyclerViewAdapter.ViewHolder> {

    private Activity activity;
    private Context context;
    private List<AuthorsModel> list;

    public AuthorsRecyclerViewAdapter(
            Activity activity, Context context, List<AuthorsModel> list) {
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
                                R.layout.authors_item,
                                parent,
                                false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        AuthorsModel model = list.get(position);
        holder.nickname.setText(model.getNickname());
        holder.nicknamedesc.setText(model.getNicknameDesc());
        holder.description.setText(
            Html.fromHtml(
                model.getDescription(),
                Html.FROM_HTML_MODE_LEGACY));
        holder.description.setMovementMethod(LinkMovementMethod.getInstance());
        String url = model.getUrl();
        if (!url.isEmpty()) {
            holder.card.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            });
        }
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

        public MaterialCardView card;
        public TextView nickname;
        public TextView nicknamedesc;
        public TextView description;

        public ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            nickname = v.findViewById(R.id.nickname);
            nicknamedesc = v.findViewById(R.id.nicknamedesc);
            description = v.findViewById(R.id.description);
        }
    }
}