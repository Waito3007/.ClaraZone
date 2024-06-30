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
import com.google.firebase.auth.FirebaseUser;
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
                boolean status = checkboxPrivacy.isChecked();
                String timestamp = getCurrentTimestamp();

                // Kiểm tra điều kiện: ít nhất một trong hai trường không được rỗng
                if (!TextUtils.isEmpty(title) || mediaUri != null) {
                    uploadMediaToFirebase(title, timestamp, status);
                } else {
                    Toast.makeText(PostActivity.this, "Please provide a title or select media", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Hàm tải media lên Firebase Storage và lưu thông tin bài viết vào Firebase Database
    private void uploadMediaToFirebase(String title, String timestamp, boolean status) {
        if (mediaUri != null) {
            // Tạo tên tệp tin dựa trên thời gian hiện tại
            String fileName = System.currentTimeMillis() + "." + getFileExtension(mediaUri);
            StorageReference fileReference = storageReference.child("posts/" + fileName);

            fileReference.putFile(mediaUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String mediaUrl = task.getResult().toString();
                                            createPost(title, mediaUrl, timestamp, status);
                                        } else {
                                            Toast.makeText(PostActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(PostActivity.this, "Media upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            createPost(title, null, timestamp, status);
        }
    }

    // Hàm tạo bài viết mới và lưu vào Firebase Database
    private void createPost(String title, String mediaUrl, String timestamp, boolean status) {
        String postId = databaseReference.push().getKey();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Post post = new Post(postId, userId, title, mediaUrl, timestamp, status, 0);

            // Lưu post vào Firebase Database
            databaseReference.child(postId).setValue(post)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
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

    // Hàm lấy timestamp hiện tại dưới định dạng h'p' m' ngày' d/M/yyyy
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Hàm lấy phần mở rộng của tệp tin
    private String getFileExtension(Uri uri) {
        String extension;
        // Lấy phần mở rộng của tệp tin
        extension = uri.getPath().substring(uri.getPath().lastIndexOf(".") + 1);
        return extension;
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
