package edmt.dev.androidecommerceapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import edmt.dev.androidecommerceapp.Common.Common;
import edmt.dev.androidecommerceapp.Model.User;

public class SignUp extends AppCompatActivity {

    MaterialEditText edtPhone,edtName,edtPassword,edtSecureCode;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = (MaterialEditText)findViewById(R.id.edtName);
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        edtSecureCode = (MaterialEditText)findViewById(R.id.edtSecureCode);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);


        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.isConnectedToInterner(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Please wait......");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //check if already user phone
//                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
//                                mDialog.dismiss();
//                                Toast.makeText(SignUp.this, "Phone number is already registered", Toast.LENGTH_SHORT).show();
//
//                            } else {

                                mDialog.dismiss();
                                User user = new User(edtName.getText().toString(),
                                        edtPassword.getText().toString(),
                                        edtSecureCode.getText().toString()
                                );
                                table_user.child(edtPhone.getText().toString()).setValue(user);
                                Toast.makeText(SignUp.this, "Sign up successfully done!!", Toast.LENGTH_SHORT).show();
                                finish();
                            //}

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                else
                {
                    Toast.makeText(SignUp.this, "Please Check Your Connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });


    }
}
