package com.hoyo.cz.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Fragment.OptionsFragment;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private DatabaseReference accountsRef;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.accountsRef = FirebaseDatabase.getInstance().getReference("account");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Load post details
        holder.dayPost.setText(post.getDayupP());
        holder.title.setText(post.getTitleP());

        // Load post content (image)
        String mediaUrl = post.getContentP();
        if (mediaUrl != null && !mediaUrl.isEmpty()) {
            holder.content.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(mediaUrl)
                    .apply(new RequestOptions().placeholder(R.drawable.placeholder_image).error(R.drawable.error_image))
                    .into(holder.content);
        } else {
            holder.content.setVisibility(View.GONE);
        }

        // Load user details
        accountsRef.child(post.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Account account = snapshot.getValue(Account.class);
                if (account != null) {
                    holder.nameUser.setText(account.getNameUser());

                    Glide.with(context)
                            .load(account.getAvatarUser() != null ? account.getAvatarUser() : R.drawable.avatar_macdinh)
                            .circleCrop()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(holder.imageViewUserAvatar);

                    // Hiển thị menuOptions cho người dùng đăng bài
                    if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                            FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getUid())) {
                        holder.menuOptions.setVisibility(View.VISIBLE);
                    } else {
                        holder.menuOptions.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Error loading user data", error.toException());
            }
        });

        holder.btnLike.setOnClickListener(v -> {
            // Xử lý sự kiện khi nhấn nút Thích
        });

        holder.btnComment.setOnClickListener(v -> {
            // Xử lý sự kiện khi nhấn nút Bình luận
        });

        holder.menuOptions.setOnClickListener(v -> {
            OptionsFragment optionsFragment = new OptionsFragment(post.getPid());
            optionsFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "OptionsFragment");
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewUserAvatar;
        TextView nameUser, dayPost, title;
        ImageView content,menuOptions;
        Button btnLike, btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            nameUser = itemView.findViewById(R.id.name_user);
            dayPost = itemView.findViewById(R.id.day_post);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            menuOptions = itemView.findViewById(R.id.menuOptions);
        }
    }
}
