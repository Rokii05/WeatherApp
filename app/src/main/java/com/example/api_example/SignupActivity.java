package com.example.api_example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGllgeSignInClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        //Button Listener
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnectButton).setOnClickListener(this);


        //configure Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                Log.w(TAG,"Google sign in failed");
                updateUI(null);
            }
        }
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new onCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@nonNull Task<AuthResult> task){
                        if (task.isSuccessful()){
                            Log.d(TAG, "signInWithCredential: success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }else{
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.activity_signup), "Authentication failed", Snackbar.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void signIn(){
        Intent signinIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signinIntent, RC_SIGN_IN);
    }

    private void signOut{
        mAuth.signOut();

        mGooleSignInClient.signout().addOnCompleteListener(this, new OnCompleteListener<void>(){
            @Override
            public void onComplete(@NonNull Task<void> task){
                updateUI(null);
            }
        });
    }

    private void revokeAccess(){
        mAuth.signOut();

        mGoogleSignInClient.revokeAccess().addOnCompliteListener(this,new OnCompleteListener<void>(){
           @Override
           public void onComplete(@NonNull Task<void> task){
               updateUI(null);
           }
        });
    }


    private void uodateUI(FirebaseUser user){
        hideprogressDialog();
        if(user != null){
            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.signOutAndDisconnec).setVisibility(View.VISIBLE);
        }else{
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.signOutAndDisconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v){
        int i = v.getId();

        if(i == R.id.sign_in_button){
            signIn();
        }else if (i == R.id.signOutButton){
            signOut();
        }else if (i == R.id.disconnectButton){
            revokeAccess();
        }
    }
}