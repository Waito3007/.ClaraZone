package com.hoyo.cz.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Activity.PostDetailActivity;
import com.hoyo.cz.Fragment.OptionsFragment;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Like;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final DatabaseReference accountsRef;
    private final DatabaseReference likesRef;
    private final FirebaseUser currentUser;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.accountsRef = FirebaseDatabase.getInstance().getReference("account");
        this.likesRef = FirebaseDatabase.getInstance().getReference("like");
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        loadUserDetails(holder, post);

        //chuc nang like bai viet
        handleLikeStatus(holder, post);

        holder.btnComment.setOnClickListener(v -> {
            // Xử lý sự kiện khi nhấn nút Bình luận
        });

        holder.menuOptions.setOnClickListener(v -> {
            OptionsFragment optionsFragment = new OptionsFragment(post.getPid());
            optionsFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "OptionsFragment");
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getPid());
            context.startActivity(intent);
        });
    }

    private void loadUserDetails(PostViewHolder holder, Post post) {
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

                    if (currentUser != null && currentUser.getUid().equals(post.getUid())) {
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
    }

    public void handleLikeStatus(PostViewHolder holder, Post post) {
        if (currentUser == null) {
            return; // Người dùng chưa đăng nhập
        }

        String pid = post.getPid();
        String uid = currentUser.getUid();

        Query likeQuery = likesRef.orderByChild("pid").equalTo(pid);
        likeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean[] userLiked = {false};
                String[] likeId = {null};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Like like = ds.getValue(Like.class);
                    if (like != null && like.getUid().equals(uid)) {
                        userLiked[0] = like.isStatus();
                        likeId[0] = like.getLikeId();
                        break;
                    }
                }

                updateLikeButton(holder.btnLike, userLiked[0]);

                holder.btnLike.setOnClickListener(v -> {
                    if (userLiked[0]) {
                        // Bỏ like
                        if (likeId[0] != null) {
                            likesRef.child(likeId[0]).child("status").removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    updateLikeCount(pid, -1);
                                    userLiked[0] = false;
                                    updateLikeButton(holder.btnLike, userLiked[0]);
                                }
                            });
                        }
                    } else {
                        // Like
                        if (likeId[0] == null) {
                            likeId[0] = likesRef.push().getKey();
                        }
                        Like newLike = new Like(likeId[0], pid, uid, getCurrentTimestamp(), true);
                        likesRef.child(likeId[0]).setValue(newLike).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateLikeCount(pid, 1);
                                userLiked[0] = true;
                                updateLikeButton(holder.btnLike, userLiked[0]);
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Lỗi khi tải trạng thái like", error.toException());
            }
        });
    }

    public String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void updateLikeButton(Button btnLike, boolean isLiked) {
        if (isLiked) {
            btnLike.setText("Đã thích");
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_black, 0, 0, 0);
        } else {
            btnLike.setText("Thích");
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_white, 0, 0, 0);
        }
    }

    public void updateLikeCount(String pid, int countChange) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts").child(pid);
        postsRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Post post = mutableData.getValue(Post.class);
                if (post == null) {
                    return Transaction.success(mutableData);
                }

                post.setLikeP(post.getLikeP() + countChange);
                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("PostAdapter", "Lỗi khi cập nhật số lượng like", error.toException());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewUserAvatar;
        TextView nameUser;
        ImageView content;
        TextView title;
        TextView dayPost;
        Button btnLike;
        Button btnComment;
        ImageView menuOptions;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            nameUser = itemView.findViewById(R.id.name_user);
            content = itemView.findViewById(R.id.content);
            title = itemView.findViewById(R.id.title);
            dayPost = itemView.findViewById(R.id.day_post);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            menuOptions = itemView.findViewById(R.id.menuOptions);
        }
    }
}
