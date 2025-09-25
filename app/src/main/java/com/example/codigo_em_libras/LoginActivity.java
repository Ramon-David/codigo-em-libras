package com.example.codigo_em_libras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#008744"));
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bd = FirebaseFirestore.getInstance();

        // Inicializa o firebase auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Configura o google sign-in
            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            // Inicializa o cliente do google
            mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

            SignInButton btnGoogle = findViewById(R.id.btnGoogle);
            btnGoogle.setOnClickListener(v -> signIn());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GOOGLE_SIGN_IN", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Salvo no sharedpreferences
                        String nome = user.getDisplayName();
                        String email = user.getEmail();
                        String userId = user.getUid();

                        SharedPreferences sharedPreferences = getSharedPreferences("app-config", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("nome", nome);
                        editor.putString("email", email);
                        editor.apply();

                        criarDocumentoInicial(userId);

                        startActivity(new Intent(this, MainActivity.class));
                        Toast.makeText(getApplicationContext(), "Login bem sucedido!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha no login", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void criarDocumentoInicial(String userId) {
        Map<String, Object> dadosIniciais = new HashMap<>();
        dadosIniciais.put("EstrelasTotais", 0);
        dadosIniciais.put("FaseAtual", 1);
        dadosIniciais.put("MundoAtual", 1);

        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados do Jogo")
                .document("Progresso")
                .set(dadosIniciais)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Dados iniciais criados com sucesso!"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao criar documento", e));
    }
}


