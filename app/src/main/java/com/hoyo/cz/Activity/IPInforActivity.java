package com.hoyo.cz.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class IPInforActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivAvatar;
    private EditText edUserName;
    private Button btChooseAvatar, btSave;
    private Uri avatarUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipinfor);

        ivAvatar = findViewById(R.id.ivAvatar);
        edUserName = findViewById(R.id.edUserName);
        btChooseAvatar = findViewById(R.id.btChooseAvatar);
        btSave = findViewById(R.id.btSave);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("account");
        storageReference = FirebaseStorage.getInstance().getReference("avatars");

        btChooseAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAvatar();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void chooseAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            ivAvatar.setImageURI(avatarUri);
        }
    }

    private void saveUserInfo() {
        final String userName = edUserName.getText().toString().trim();
        if (userName.isEmpty()) {
            Toast.makeText(this, "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userId = user.getUid();
        showLoading();

        if (avatarUri != null) {
            uploadAvatar(userId, new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String avatarUrl = task.getResult().toString();
                        saveToDatabase(userId, userName, avatarUrl);
                    } else {
                        hideLoading();
                        Toast.makeText(IPInforActivity.this, "Lỗi khi tải lên avatar", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            saveToDatabase(userId, userName, null);
        }
    }

    private void uploadAvatar(String userId, OnCompleteListener<Uri> onCompleteListener) {
        StorageReference avatarRef = storageReference.child(userId + ".jpg");
        avatarRef.putFile(avatarUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return avatarRef.getDownloadUrl();
                })
                .addOnCompleteListener(onCompleteListener);
    }

    private void saveToDatabase(String userId, String userName, String avatarUrl) {
        Account account = new Account();
        account.setUid(userId);
        account.setNameUser(userName);
        if (avatarUrl != null) {
            account.setAvatarUser(avatarUrl);
        }
        databaseReference.child(userId).setValue(account)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoading();
                        if (task.isSuccessful()) {
                            Toast.makeText(IPInforActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(IPInforActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(IPInforActivity.this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
