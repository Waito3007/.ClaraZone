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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Like;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // tải thông tin của user
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Error loading user data", error.toException());
            }
        });

        // Handle like button status
        handleLikeStatus(holder, post);
    }

    private void handleLikeStatus(PostViewHolder holder, Post post) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // User is not authenticated
        }

        String postId = post.getPid();
        String userId = currentUser.getUid();
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("like").child(postId).child(userId);

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userLiked = snapshot.exists() && snapshot.child("status").getValue(Boolean.class) == Boolean.TRUE;

                // Update UI based on like status
                updateLikeButton(holder.btnLike, userLiked);

                // Set click listener for like button
                holder.btnLike.setOnClickListener(v -> {
                    toggleLikeStatus(likesRef, holder.btnLike, userLiked);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Error loading like status", error.toException());
            }
        });
    }

    private void updateLikeButton(Button btnLike, boolean userLiked) {
        if (userLiked) {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_black, 0, 0, 0);
            btnLike.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_white, 0, 0, 0);
            btnLike.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    private void toggleLikeStatus(DatabaseReference likesRef, Button btnLike, boolean userLiked) {
        if (userLiked) {
            // Unlike
            likesRef.removeValue();
            updateLikeButton(btnLike, false);
        } else {
            // Like
            String likedAt = getCurrentTime();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String postId = likesRef.getParent().getKey();
                Like like = new Like(postId, currentUser.getUid(), likedAt, true);
                likesRef.setValue(like);
                updateLikeButton(btnLike, true);
            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewUserAvatar;
        TextView nameUser, dayPost, title;
        ImageView content;
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
        }
    }
}
