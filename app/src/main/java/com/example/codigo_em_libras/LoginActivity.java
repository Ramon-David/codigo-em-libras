package com.example.codigo_em_libras;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
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
import java.util.Random;

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

        ImageView imageView = findViewById(R.id.logoImageView);

        ObjectAnimator zoomX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.05f);
        ObjectAnimator zoomY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.05f);
        zoomX.setRepeatMode(ValueAnimator.REVERSE);
        zoomY.setRepeatMode(ValueAnimator.REVERSE);
        zoomX.setRepeatCount(ValueAnimator.INFINITE);
        zoomY.setRepeatCount(ValueAnimator.INFINITE);
        zoomX.setDuration(10000);
        zoomY.setDuration(9000);
        zoomX.start();
        zoomY.start();

// piscar com alpha
        Handler handler = new Handler();
        Random random = new Random();
        Runnable blinkRunnable = new Runnable() {
            @Override
            public void run() {
                ObjectAnimator blink = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0.3f);
                blink.setDuration(200);
                blink.setRepeatMode(ValueAnimator.REVERSE);
                blink.setRepeatCount(1);
                blink.start();

                handler.postDelayed(this, 3000 + random.nextInt(5000));
            }
        };
        handler.post(blinkRunnable);




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

                        criarDocumentoInicial(userId,nome,email);

                        startActivity(new Intent(this, MainActivity.class));
                        Toast.makeText(getApplicationContext(), "Login bem sucedido!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha no login", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void criarDocumentoInicial(String userId,String nome, String email) {
        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados do Jogo")
                .document("Progresso")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        criarDocEstrelas(userId);
                    } else {
                        Log.d("FIREBASE", "Dados de estrelas já existem.");
                    }
                });

        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados da Conta")
                .document("DadosDaConta")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        criarDadosIniciais(userId, nome, email);
                    } else {
                        Log.d("FIREBASE", "Dados da conta ja existem.");
                    }
                });
    }

    private void criarDocEstrelas(String userId){
        Map<String, Object> dadosIniciais = new HashMap<>();
        dadosIniciais.put("EstrelasTotais", 0);
        dadosIniciais.put("FaseAtual", 1);
        dadosIniciais.put("MundoAtual", 1);

        for (int m = 1; m < 4;m++){
            String nomeMundo = "mundo"+m;

            for (int i = 1; i < 8; i++) {
                Map<String, Object> faseData = new HashMap<>();
                faseData.put("estrelas", 0);

                String nomeFase = "fase" + i;

                bd.collection("JogadorDados")
                        .document(userId)
                        .collection("Dados do Jogo")
                        .document("Progresso")
                        .collection(nomeMundo)
                        .document(nomeFase)
                        .set(faseData);
            }
        }

        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados do Jogo")
                .document("Progresso")
                .set(dadosIniciais);

        Log.d("FIREBASE", "Coleção EstrelasPorFase criada com sucesso!");
    }

    private void criarDadosIniciais(String userId, String nome, String email){
        Map<String, Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("Nome", nome);
        dadosUsuario.put("Email", email);
        dadosUsuario.put("ModoNoturno", false);

        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados da Conta")
                .document("DadosDaConta")
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Dados da conta criados com sucesso!"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao criar documento", e));
    }

}


