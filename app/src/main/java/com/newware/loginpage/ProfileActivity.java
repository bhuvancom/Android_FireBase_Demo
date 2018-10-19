package com.newware.loginpage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{
    private ImageView profilePic;
    private TextView profileName, profileEmail, profileMobile;
    private Button btnEdit;


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    private ProgressDialog progressDialog;

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        referencing();

        btnEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                startActivity(new Intent(ProfileActivity.this, UpdateProfileActivity.class));
            }
        });
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        StorageReference storageReference = firebaseStorage.getReference();
        storageReference.child(firebaseAuth.getUid()).child("images/profile_pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                Picasso.get().load(uri).fit().centerCrop().into(profilePic);
            }
        });

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid()); // only auth users id
        //Toast.makeText(this, "user : "+firebaseUser, Toast.LENGTH_SHORT).show();


        if (firebaseUser != null)
        {//data changing

            progressDialog.setCancelable(false);
            progressDialog.setMessage("Fetching Profile Please Wait");
            progressDialog.show();
            databaseReference.addValueEventListener(new ValueEventListener()
            {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    User currentUser = dataSnapshot.getValue(User.class);//get value

                    if (currentUser == null)
                    {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Null Hai", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    profileEmail.setText(currentUser.getEmail());
                    profileName.setText(currentUser.getName());
                    profileMobile.setText(currentUser.getMobile());
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Toast.makeText(ProfileActivity.this, "Database error : " + databaseError.getCode(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void referencing()
    {
        profileEmail = (TextView) findViewById(R.id.tv_user_email);
        profileMobile = (TextView) findViewById(R.id.tv_user_mobile);
        profileName = (TextView) findViewById(R.id.tv_user_name);

        profilePic = (ImageView) findViewById(R.id.iv_user_img);

        btnEdit = (Button) findViewById(R.id.btn_update);
    }
}
