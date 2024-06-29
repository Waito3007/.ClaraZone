package com.hoyo.cz.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private ImageView imagePreview;
    private VideoView videoPreview;
    private Button buttonChooseMedia;
    private Button buttonPost;
    private Button btnBack;
    private CheckBox checkboxPrivacy;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri mediaUri;
    private FirebaseAuth firebaseAuth;

    private static final int PICK_MEDIA_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        editTextTitle = findViewById(R.id.editTextTitle);
        imagePreview = findViewById(R.id.image_preview);
        videoPreview = findViewById(R.id.video_preview);
        buttonChooseMedia = findViewById(R.id.choosemedia_btn);
        buttonPost = findViewById(R.id.post_btn);
        checkboxPrivacy = findViewById(R.id.checkbox_privacy);
        btnBack = findViewById(R.id.btback);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        buttonChooseMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/* video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture or Video"), PICK_MEDIA_REQUEST);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng PostActivity và trở về màn hình trước đó
            }
        });

        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString().trim();
                String content = mediaUri != null ? mediaUri.toString() : null;
                boolean status = checkboxPrivacy.isChecked();
                String timestamp = getCurrentTimestamp();

                // Kiểm tra điều kiện: ít nhất một trong hai trường không được rỗng
                if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
                    createPost(title, content, timestamp, status);
                } else {
                    Toast.makeText(PostActivity.this, "Please provide a title or select media", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Hàm tạo bài viết mới và lưu vào Firebase Database
    private void createPost(String title, String content, String timestamp, boolean status) {
        String postId = databaseReference.push().getKey();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Post post = new Post(postId, userId, title, content, timestamp, status, 0);

            // Lưu post vào Firebase Database
            databaseReference.child(postId).setValue(post)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PostActivity.this, "Post successful", Toast.LENGTH_SHORT).show();
                                finish(); // Đóng activity sau khi đăng bài
                            } else {
                                Toast.makeText(PostActivity.this, "Post failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(PostActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm lấy timestamp hiện tại dưới định dạng yyyyMMdd_HHmmss
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h'g' m'p' 'ngày' d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }


    // Xử lý kết quả chọn media từ Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mediaUri = data.getData();

            if (mediaUri.toString().contains("image")) {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(mediaUri);
                videoPreview.setVisibility(View.GONE);
            } else if (mediaUri.toString().contains("video")) {
                videoPreview.setVisibility(View.VISIBLE);
                videoPreview.setVideoURI(mediaUri);
                videoPreview.start();
                imagePreview.setVisibility(View.GONE);
            }
        }
    }
}
