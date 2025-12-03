package com.example.codigo_em_libras;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore bd;
    private static final int RC_SIGN_IN = 100;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    PrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#008744"));
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        prefs = new PrefsHelper(LoginActivity.this);

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
        zoomX.setDuration(5000);
        zoomY.setDuration(4500);
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

            atualizarDadosCache();
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

                        criarDocumentoInicial(userId, nome, email);

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
                    if (!documentSnapshot.exists() ||
                            !documentSnapshot.contains("EstrelasTotais") ||
                            !documentSnapshot.contains("FaseAtual") ||
                            !documentSnapshot.contains("MundoAtual")) {

                        criarDocEstrelas(userId);
                        Log.d("FIREBASE", "Dados recriados novamente.");
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

        atualizarDadosCache();
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
        dadosUsuario.put("ImgConta","");

        bd.collection("JogadorDados")
                .document(userId)
                .collection("Dados da Conta")
                .document("DadosDaConta")
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Dados da conta criados com sucesso!"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao criar documento", e));
    }

    private void atualizarDadosCache(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        bd.collection("JogadorDados")//Informações da conta
                .document(uid)
                .collection("Dados da Conta")
                .document("DadosDaConta")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String email = document.getString("Email");
                        String nome = document.getString("Nome");
                        String img = document.getString("ImgConta");
                        Boolean modoNoturno = document.getBoolean("ModoNoturno");

                        String dados = email + ";" + nome + ";" + img + ";" + modoNoturno;

                        prefs.putString("prefs_conta", dados);
                        Log.d("CACHE", dados);
                    }
                });



        bd.collection("JogadorDados")//Dados de cada fase(pontuação)
                .document(uid)
                .collection("Dados do Jogo")
                .document("Progresso")
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) return;

                    Integer estrelasTotais = document.getLong("EstrelasTotais").intValue();
                    Integer faseAtual = document.getLong("FaseAtual").intValue();
                    Integer mundoAtual = document.getLong("MundoAtual").intValue();

                    StringBuilder sb = new StringBuilder();
                    sb.append("EstrelasTotais=").append(estrelasTotais).append("|");
                    sb.append("FaseAtual=").append(faseAtual).append("|");
                    sb.append("MundoAtual=").append(mundoAtual).append("|");

                    prefs.putInt("estrelas", estrelasTotais);

                    // ler mundos dentro do documento "Progresso"
                    document.getReference().collection("mundo1").get()
                            .addOnSuccessListener(m1 -> processarMundo(sb, "mundo1", m1, prefs));

                    document.getReference().collection("mundo2").get()
                            .addOnSuccessListener(m2 -> processarMundo(sb, "mundo2", m2, prefs));

                    document.getReference().collection("mundo3").get()
                            .addOnSuccessListener(m3 -> processarMundo(sb, "mundo3", m3, prefs));
                });

        bd.collection("Materiais")
                .get()
                .addOnSuccessListener(snap -> {

                    StringBuilder sb = new StringBuilder();

                    for (DocumentSnapshot mundo : snap.getDocuments()) {

                        mundo.getReference().collection("itens").get()
                                .addOnSuccessListener(itens -> {

                                    for (DocumentSnapshot item : itens.getDocuments()) {

                                        String descricao = item.getString("descricao");
                                        Long ordem = item.getLong("ordem");
                                        String titulo = item.getString("titulo");
                                        String url = item.getString("url");

                                        sb.append(mundo.getId()).append("/")
                                                .append(item.getId()).append(";")
                                                .append(descricao).append(";")
                                                .append(ordem).append(";")
                                                .append(titulo).append(";")
                                                .append(url).append("|");
                                    }

                                    Log.d("CACHE", sb.toString());
                                    prefs.putString("prefs_materiais", sb.toString());
                                });
                    }
                });

            bd.collection("Mundos").get()
                    .addOnSuccessListener(mundos -> {

                        for (DocumentSnapshot mundo : mundos) {

                            String mundoId = mundo.getId();
                            StringBuilder sbMundo = new StringBuilder();

                            mundo.getReference().collection("conteudos").get()
                                    .addOnSuccessListener(conteudos -> {

                                        AtomicInteger restanteConteudos = new AtomicInteger(conteudos.size());

                                        for (DocumentSnapshot conteudo : conteudos.getDocuments()) {

                                            StringBuilder sbConteudo = new StringBuilder();

                                            String conteudoId = conteudo.getId();
                                            String descricao = conteudo.getString("descricao");
                                            String imagem = conteudo.getString("imagemReferente");

                                            // Começa o bloco do conteúdo
                                            sbConteudo.append(conteudoId)
                                                    .append(";")
                                                    .append(descricao)
                                                    .append(";")
                                                    .append(imagem)
                                                    .append(";");

                                            conteudo.getReference().collection("questoes").get()
                                                    .addOnSuccessListener(questoes -> {

                                                        for (DocumentSnapshot questao : questoes.getDocuments()) {

                                                            // Adiciona dados da questão
                                                            sbConteudo.append(questao.getId())
                                                                    .append("=")
                                                                    .append(questao.getData().toString())
                                                                    .append(";");
                                                        }

                                                        // Quando terminar todas as questões do conteúdo:
                                                        sbConteudo.append("|"); // separa conteúdos

                                                        sbMundo.append(sbConteudo);

                                                        // Se finalizou todos os conteúdos -> salva prefs
                                                        if (restanteConteudos.decrementAndGet() == 0) {
                                                            salvarPrefsMundo(LoginActivity.this, mundoId, sbMundo.toString());
                                                        }

                                                    });
                                        }
                                    });
                        }
                    });

            new SalvamentoDados().salvarDadosConta(LoginActivity.this);
    }



    public void salvarPontuacaoJogador(){

    }

    private void processarMundo(StringBuilder sb, String nomeMundo, QuerySnapshot mundoSnap, PrefsHelper prefs) {

        for (DocumentSnapshot fase : mundoSnap.getDocuments()) {
            Integer estrelas = fase.getLong("estrelas").intValue();

            sb.append(nomeMundo).append("_")
                    .append(fase.getId()) // ex: fase1
                    .append("=")
                    .append(estrelas)
                    .append("|");
        }

        Log.d("CACHE definitivo", sb.toString());
        prefs.putString("prefs_progresso", sb.toString());
    }

    private static void salvarPrefsMundo(Context ctx, String mundoId, String dados) {
        String chavePrefs = "prefs_" + mundoId.toLowerCase();
        PrefsHelper prefs = new PrefsHelper(ctx);

        prefs.putString(chavePrefs, dados);
        Log.d("CACHE supremo", "Salvo em " + chavePrefs + ": " + dados);
    }

}


