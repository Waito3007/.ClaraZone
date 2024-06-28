package com.hoyo.cz.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hoyo.cz.R;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edEmail;
    Button btBack;
    private Button btResetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        edEmail = findViewById(R.id.edEmail);
        btResetPassword = findViewById(R.id.btResetPassword);
        btBack = findViewById(R.id.btback);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng SignInActivity và trở về fragment_profile
            }
        });
        btResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    resetPassword(email);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập địa chỉ email của bạn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void resetPassword(String email) {
        // Hiển thị vòng loading
        showLoading();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Đã gửi email reset mật khẩu, vui lòng kiểm tra hộp thư đến của bạn", Toast.LENGTH_SHORT).show();
                            // Ẩn vòng loading nếu gửi email thành công
                            hideLoading();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Gửi email reset mật khẩu thất bại, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                            // Ẩn vòng loading nếu gửi email thất bại
                            hideLoading();
                        }
                    }
                });
    }

    private void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi email...");
        progressDialog.setCancelable(false); // Không cho phép hủy bỏ
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
