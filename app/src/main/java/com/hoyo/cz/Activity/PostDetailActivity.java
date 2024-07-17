package com.hoyo.cz.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hoyo.cz.Adapter.CommentAdapter;
import com.hoyo.cz.Adapter.PostAdapter;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Comment;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private ImageView addImageComment, sendComment;
    private EditText editTextComment;
    private Button btBack;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        addImageComment = findViewById(R.id.addImageComment);
        sendComment = findViewById(R.id.sendComment);
        btBack = findViewById(R.id.btback);
        editTextComment = findViewById(R.id.editTextComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        postId = getIntent().getStringExtra("postId");

        setupPostAdapter();
        setupComments();
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay về trang trước đó
                finish();
            }
        });
        addImageComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
    }

    private void setupPostAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        Query query = postsRef.orderByChild("pid").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> postList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                PostAdapter postAdapter = new PostAdapter(PostDetailActivity.this, postList);
                recyclerView.setAdapter(postAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load post details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupComments() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments");
        Query query = commentsRef.orderByChild("pid").equalTo(postId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void postComment() {
        String commentText = editTextComment.getText().toString().trim();
        if (TextUtils.isEmpty(commentText) && imageUri == null) {
            Toast.makeText(this, "Please enter a comment or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String commentId = FirebaseDatabase.getInstance().getReference().push().getKey();
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        if (imageUri != null) {
            uploadImage(commentId, userId, commentText, currentDate);
        } else {
            saveComment(commentId, userId, commentText, currentDate, null);
        }
    }

    private void uploadImage(String commentId, String userId, String commentText, String currentDate) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("comment_images").child(commentId + ".jpg");
        storageReference.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    List<String> imageUrls = new ArrayList<>();
                    imageUrls.add(uri.toString());
                    saveComment(commentId, userId, commentText, currentDate, imageUrls);
                });
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveComment(String commentId, String userId, String commentText, String currentDate, List<String> imageUrls) {
        Comment comment = new Comment(commentId, userId, postId, currentDate, commentText, imageUrls);
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("comments").child(commentId);
        commentRef.setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                editTextComment.setText("");
                imageUri = null;
            } else {
                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
