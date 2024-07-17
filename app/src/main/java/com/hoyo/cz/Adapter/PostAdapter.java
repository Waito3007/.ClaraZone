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
import android.widget.Toast;

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
import com.hoyo.cz.Activity.UDPageActivity;
import com.hoyo.cz.Fragment.OptionsFragment;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Follow;
import com.hoyo.cz.Model.Like;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.Model.Share;
import com.hoyo.cz.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final DatabaseReference accountsRef;
    private final DatabaseReference likesRef;
    private final DatabaseReference followRef;
    private final FirebaseUser currentUser;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.accountsRef = FirebaseDatabase.getInstance().getReference("account");
        this.likesRef = FirebaseDatabase.getInstance().getReference("like");
        this.followRef = FirebaseDatabase.getInstance().getReference("follow");
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
        holder.likeCountTextView.setText(String.valueOf(post.getLikeP()));
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

        //chuc nang follow bai viet
        handleFollowStatus(holder, post);

        holder.btnComment.setOnClickListener(v -> {
            // Xử lý sự kiện khi nhấn nút Bình luận
        });

        holder.btnShare.setOnClickListener(v -> handleSharePost(post));

        // Xử lý sự kiện khi nhấn nút Menu Options
        holder.menuOptions.setVisibility(View.VISIBLE);
        holder.menuOptions.setOnClickListener(v -> {
            OptionsFragment optionsFragment = new OptionsFragment(post.getPid());
            optionsFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "OptionsFragment");
        });
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, PostDetailActivity.class);
//            intent.putExtra("postId", post.getPid());
//            context.startActivity(intent);
//        });
        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getPid());
            context.startActivity(intent);
        });
        //Xem trang cá nhân người dùng vừa ấn.
        holder.imageViewUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(context, UDPageActivity.class);
            intent.putExtra("userId", post.getUid());
            context.startActivity(intent);
        });

        holder.nameUser.setOnClickListener(v -> {
            Intent intent = new Intent(context, UDPageActivity.class);
            intent.putExtra("userId", post.getUid());
            context.startActivity(intent);
        });
    }
    public void loadUserDetails(PostViewHolder holder, Post post) {
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
    }
    public void sortByDateDescending() {
        Collections.sort(postList, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                SimpleDateFormat sdf = new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(post1.getDayupP());
                    Date date2 = sdf.parse(post2.getDayupP());
                    return date2.compareTo(date1); // Sắp xếp giảm dần theo ngày
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        notifyDataSetChanged(); // Cập nhật RecyclerView sau khi sắp xếp
    }



    private void handleLikeStatus(PostViewHolder holder, Post post) {
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
    private void handleFollowStatus(PostViewHolder holder, Post post) {
        if (currentUser == null) {
            return; // Người dùng chưa đăng nhập
        }

        String followingId = post.getUid(); // ID của người đang đăng bài
        String uid = currentUser.getUid(); // ID của người đang đăng nhập

        Query followQuery = followRef.orderByChild("followingId").equalTo(followingId);
        followQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean[] isFollowing = {false};
                String[] followId = {null};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Follow follow = ds.getValue(Follow.class);
                    if (follow != null && follow.getUid().equals(uid)) {
                        isFollowing[0] = follow.isStatusF();
                        followId[0] = follow.getFid();
                        break;
                    }
                }

                updateFollowButton(holder.btnFollow, isFollowing[0]);

                holder.btnFollow.setOnClickListener(v -> {
                    DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("follow");

                    if (isFollowing[0]) {
                        // Unfollow
                        if (followId[0] != null) {
                            followRef.child(followId[0]).removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    isFollowing[0] = false;
                                    updateFollowButton(holder.btnFollow, isFollowing[0]);
                                }
                            });
                        }
                    } else {
                        // Follow
                        if (followId[0] == null) {
                            followId[0] = followRef.push().getKey();
                        }
                        String currentDate = getCurrentTimestamp();
                        Follow newFollow = new Follow(followId[0], uid, followingId, true, currentDate);
                        followRef.child(followId[0]).setValue(newFollow).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                isFollowing[0] = true;
                                updateFollowButton(holder.btnFollow, isFollowing[0]);
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PostAdapter", "Lỗi khi tải trạng thái follow", error.toException());
            }
        });
    }

    private void handleSharePost(Post post) {
        if (currentUser == null) {
            return; // Người dùng chưa đăng nhập
        }

        String sid = FirebaseDatabase.getInstance().getReference("share").push().getKey();
        String pid = post.getPid();
        String timestamp = getCurrentTimestamp();

        String uid = currentUser.getUid();
        boolean statusShare = true; // Hoặc gán giá trị theo yêu cầu của bạn

        Share newShare = new Share(sid, timestamp, uid, pid);

        FirebaseDatabase.getInstance().getReference("share")
                .child(sid)
                .setValue(newShare)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Bài viết đã được chia sẻ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi khi chia sẻ bài viết", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void updateLikeButton(Button btnLike, boolean isLiked) {
        if (isLiked) {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_black, 0, 0, 0);
        } else {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_white, 0, 0, 0);
        }
    }

    private void updateFollowButton(Button btnFollow, boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wasfollow, 0, 0, 0);
        } else {
            btnFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow, 0, 0, 0);
        }
    }

    private void updateLikeCount(String pid, int countChange) {
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
         Button btnShare;
        ImageView imageViewUserAvatar;
        TextView nameUser,likeCountTextView;
        ImageView content;
        TextView title;
        TextView dayPost;
        Button btnLike;
        Button btnComment;
        Button btnFollow;
        ImageView menuOptions;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            btnShare = itemView.findViewById(R.id.btnShare);
            nameUser = itemView.findViewById(R.id.name_user);
            likeCountTextView = itemView.findViewById(R.id.like_count);
            content = itemView.findViewById(R.id.content);
            title = itemView.findViewById(R.id.title);
            dayPost = itemView.findViewById(R.id.day_post);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            menuOptions = itemView.findViewById(R.id.menuOptions);
        }
    }
}
