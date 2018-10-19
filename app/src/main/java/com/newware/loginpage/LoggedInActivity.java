package com.newware.loginpage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class LoggedInActivity extends AppCompatActivity
{

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        actionBar = getSupportActionBar();

        actionBar.setTitle("Bhuvaneshvar App");


        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid()); // only auth users id
        //Toast.makeText(this, "user : "+firebaseUser, Toast.LENGTH_SHORT).show();


        if (firebaseUser != null)
        {//data changing
            databaseReference.addValueEventListener(new ValueEventListener()
            {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    User currentUser = dataSnapshot.getValue(User.class);//get value

                    if (currentUser == null)
                    {

                        Toast.makeText(LoggedInActivity.this, "Null Hai", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    actionBar.setSubtitle(currentUser.getName());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Toast.makeText(LoggedInActivity.this, "Database error : " + databaseError.getCode(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        actionBar.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_item_for_logged_in_user, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_logout:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setMessage("Do You Want To Logout ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                ProgressDialog progressDialog = new ProgressDialog(LoggedInActivity.this);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage("Logging Out Please Wait");
                                progressDialog.show();
                                //todo on logout
                                firebaseAuth.signOut();
                                progressDialog.dismiss();
                                finish();
                                startActivity(new Intent(LoggedInActivity.this, MainActivity.class));

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            }
            case R.id.menu_profile:
            {
                startActivity(new Intent(LoggedInActivity.this, ProfileActivity.class));
                break;

            }
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
