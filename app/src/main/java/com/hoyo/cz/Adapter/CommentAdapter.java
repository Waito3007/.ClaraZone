package com.hoyo.cz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Comment;
import com.hoyo.cz.R;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private DatabaseReference userRef;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
        this.userRef = FirebaseDatabase.getInstance().getReference("account");
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Load user details based on uid
        userRef.child(comment.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Account account = snapshot.getValue(Account.class);
                    if (account != null) {
                        holder.nameUser.setText(account.getNameUser());

                        // Load user avatar using Glide or any other library
                        Glide.with(context)
                                .load(account.getAvatarUser() != null ? account.getAvatarUser() : R.drawable.avatar_macdinh)
                                .circleCrop()
                                .placeholder(R.drawable.anh_loading)
                                .error(R.drawable.anh_loading)
                                .into(holder.imageViewUserAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });

        // Load comment content
        holder.contentCm.setText(comment.getContentCm());

        // Load comment images if needed
        if (comment.getImageCmt() != null && !comment.getImageCmt().isEmpty()) {
            Glide.with(context)
                    .load(comment.getImageCmt().get(0)) // Load the first image URL (assuming there's only one image per comment for simplicity)
                    .placeholder(R.drawable.anh_loading)
                    .error(R.drawable.anh_loading)
                    .into(holder.imageViewComment);
            holder.imageViewComment.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewComment.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewUserAvatar,imageViewComment;
        TextView nameUser, contentCm;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            nameUser = itemView.findViewById(R.id.name_user);
            contentCm = itemView.findViewById(R.id.content_cm);
            imageViewComment = itemView.findViewById(R.id.imageViewComment);
        }
    }
}
