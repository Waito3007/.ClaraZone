package com.hoyo.cz.Adapter;

import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.Model.Share;
import com.hoyo.cz.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SharePostAdapter extends RecyclerView.Adapter<SharePostAdapter.SharePostViewHolder> {

    private List<Share> shareList;
    private Context context;
    private DatabaseReference mDatabase;

    public SharePostAdapter(List<Share> shareList, Context context) {
        this.shareList = shareList;
        this.context = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public SharePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_postshare, parent, false);
        return new SharePostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharePostViewHolder holder, int position) {
        Share share = shareList.get(position);

        //load
        holder.shareDate.setText(share.getTimestamp());

        // Lấy thông tin người chia sẻ
        mDatabase.child("account").child(share.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Account account = task.getResult().getValue(Account.class);
                if (account != null) {
                    holder.shareUserName.setText(account.getNameUser());
                    Glide.with(context)
                            .load(account.getAvatarUser())
                            .apply(RequestOptions.circleCropTransform()) // Bo tròn ảnh đại diện
                            .into(holder.shareUserAvatar);
                }
            }
        });

        // Lấy thông tin bài viết được chia sẻ
        mDatabase.child("posts").child(share.getPid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Post post = task.getResult().getValue(Post.class);
                if (post != null) {
                    holder.postTitle.setText(post.getTitleP());
                    holder.postDate.setText(post.getDayupP());

                    String mediaUrl = post.getContentP();
                    if (mediaUrl != null && !mediaUrl.isEmpty()) {
                        holder.postImage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(mediaUrl)
                                .apply(new RequestOptions().placeholder(R.drawable.placeholder_image).error(R.drawable.error_image))
                                .into(holder.postImage);
                    } else {
                        holder.postContent.setVisibility(View.GONE);
                    }

                    // Lấy thông tin người đăng bài
                    mDatabase.child("account").child(post.getUid()).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                            Account postUser = userTask.getResult().getValue(Account.class);
                            if (postUser != null) {
                                holder.postUserName.setText(postUser.getNameUser());
                                Glide.with(context)
                                        .load(postUser.getAvatarUser())
                                        .apply(RequestOptions.circleCropTransform()) // Bo tròn ảnh đại diện
                                        .into(holder.postUserAvatar);
                            }
                        }
                    });
                }
            }
        });

        // Thiết lập các nút like và comment (có thể thêm logic xử lý sự kiện nếu cần)
        holder.btnLike.setOnClickListener(v -> {
            // Xử lý sự kiện nhấn nút like
        });

        holder.btnComment.setOnClickListener(v -> {
            // Xử lý sự kiện nhấn nút comment
        });
    }

    @Override
    public int getItemCount() {
        return shareList.size();
    }

    public static class SharePostViewHolder extends RecyclerView.ViewHolder {
        ImageView shareUserAvatar, postUserAvatar, postImage;
        TextView shareUserName, postUserName, postTitle, postContent, postDate, shareDate;
        Button btnLike, btnComment;

        public SharePostViewHolder(@NonNull View itemView) {
            super(itemView);
            shareUserAvatar = itemView.findViewById(R.id.shareUserAvatar);
            shareUserName = itemView.findViewById(R.id.shareUserName);
            postUserAvatar = itemView.findViewById(R.id.postUserAvatar);
            postUserName = itemView.findViewById(R.id.postUserName);
            postTitle = itemView.findViewById(R.id.postTitle);
            shareDate = itemView.findViewById(R.id.day_share);
            postDate = itemView.findViewById(R.id.day_post);
            postImage = itemView.findViewById(R.id.postImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }
}
