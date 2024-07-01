package com.hoyo.cz.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

public class EditPostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitle;
    private Button btnUpdate, btnSelectImage;
    private ImageView imageView;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private DatabaseReference postsRef;
    private StorageReference storageRef;
    private Uri imageUri;
    private String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        editTitle = findViewById(R.id.editTitle);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);

        // Lấy pid từ Intent
        pid = getIntent().getStringExtra("pid");

        // Tham chiếu đến node "posts" trong Firebase Realtime Database
        postsRef = FirebaseDatabase.getInstance().getReference("posts").child(pid);

        // Tham chiếu đến Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference("post_images");

        // Load post data
        loadPostData();

        btnSelectImage.setOnClickListener(v -> openFileChooser());

        btnUpdate.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                editTitle.setError("Tiêu đề không được bỏ trống");
                return;
            }

            if (imageUri != null) {
                uploadImageAndSavePost(title);
            } else {
                updatePost(title, null);
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageView);
        }
    }

    private void loadPostData() {
        progressBar.setVisibility(View.VISIBLE);
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    editTitle.setText(post.getTitleP());
                    if (post.getContentP() != null && !post.getContentP().isEmpty()) {
                        Glide.with(EditPostActivity.this)
                                .load(post.getContentP())
                                .into(imageView);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPostActivity.this, "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void uploadImageAndSavePost(String title) {
        progressDialog.setMessage("Đang tải ảnh lên...");
        progressDialog.show();

        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            updatePost(title, downloadUrl);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(EditPostActivity.this, "Lỗi khi lấy URL ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(EditPostActivity.this, "Lỗi khi tải ảnh lên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePost(String title, String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);

        postsRef.child("titleP").setValue(title);
        if (imageUrl != null) {
            postsRef.child("contentP").setValue(imageUrl);
        }

        postsRef.child("titleP").setValue(title)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditPostActivity.this, "Cập nhật bài viết thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditPostActivity.this, "Lỗi khi cập nhật bài viết", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    progressDialog.dismiss();
                });
    }
}
