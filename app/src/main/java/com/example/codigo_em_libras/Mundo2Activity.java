package com.example.codigo_em_libras;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Mundo2Activity extends AppCompatActivity {
    private FirebaseFirestore bancoDeDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mundo2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        String[] listaConteudos = {
                "alfabeto",
                "numeros",
                "saudacoes",
                "cores",
                "animais",
                "profissoes",
                "frases"
        };


        ArrayList<ImageButton> botoesFases = new ArrayList<>();
        ImageButton fase1ImageButton = findViewById(R.id.fase1ImageButton);
        botoesFases.add(fase1ImageButton);

        ImageButton fase2ImageButton = findViewById(R.id.fase2ImageButton);
        botoesFases.add(fase2ImageButton);

        ImageButton fase3ImageButton = findViewById(R.id.fase3ImageButton);
        botoesFases.add(fase3ImageButton);

        ImageButton fase4ImageButton = findViewById(R.id.fase4ImageButton);
        botoesFases.add(fase4ImageButton);

        ImageButton fase5ImageButton = findViewById(R.id.fase5ImageButton);
        botoesFases.add(fase5ImageButton);

        ImageButton fase6ImageButton = findViewById(R.id.fase6ImageButton);
        botoesFases.add(fase6ImageButton);


        ImageButton voltarButton = findViewById(R.id.voltarImageButton);
        voltarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bancoDeDados = FirebaseFirestore.getInstance();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        String userId = usuarioAtual.getUid();

        ImageView spyImageView = findViewById(R.id.spyImageView);

        int marginInDp = 35;
        float density = getResources().getDisplayMetrics().density;
        int marginInPx = (int) (marginInDp * density);

        for (int i = 0; i < botoesFases.size(); i++) {
            int finalI = i;
            botoesFases.get(i).setOnClickListener(new View.OnClickListener() {
                ImageButton faseAtual = botoesFases.get(finalI);
                @Override
                public void onClick(View v) {
                    spyImageView.animate()
                            .scaleX(0f)
                            .setDuration(500)
                            .withEndAction(() -> {
                                // Depois que a animação termina, atualiza a constraint
                                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) spyImageView.getLayoutParams();
                                params.bottomMargin = marginInPx;
                                params.bottomToBottom = faseAtual.getId();
                                params.startToStart = faseAtual.getId();
                                params.endToEnd = faseAtual.getId();
                                spyImageView.setLayoutParams(params);

                                spyImageView.animate()
                                        .scaleX(1f)
                                        .setDuration(500).start();

                            })
                            .start();

                    Dialog dialogPersonalizado = new Dialog(Mundo2Activity.this);
                    dialogPersonalizado.setContentView(R.layout.fase_iniciar);
                    dialogPersonalizado.getWindow().setLayout(dpToPx(Mundo2Activity.this, 350), dpToPx(Mundo2Activity.this, 450));

                    TextView faseAtualTextView = dialogPersonalizado.findViewById(R.id.faseAtualTextView);
                    faseAtualTextView.setText("Fase " + (finalI + 1));

                    TextView mundoAtualTextView = dialogPersonalizado.findViewById(R.id.mundoAtualTextView);
                    mundoAtualTextView.setText("Mundo 2");

                    TextView conteudosTextView = dialogPersonalizado.findViewById(R.id.conteudoTextView);
                    TextView descricao = dialogPersonalizado.findViewById(R.id.descricaoTextView);
                    ImageView temaImageView = dialogPersonalizado.findViewById(R.id.capaFaseImageView);

                    PrefsHelper prefs = new PrefsHelper(Mundo2Activity.this);
                    String dadosMundo = prefs.getString("prefs_mundo2");

                    if (dadosMundo != null && dadosMundo.contains("|")) {
                        String[] conteudos = dadosMundo.split("\\|");
                        String infoConteudoEscolhido = "";

                        for (String conteudo:conteudos){
                            String[] contInfo = conteudo.split(";");
                            if (contInfo[0].equals(listaConteudos[finalI])){
                                infoConteudoEscolhido = conteudo;
                                break;
                            }
                        }

                        String[] dadosConteudo = infoConteudoEscolhido.split(";");
                        String nomeConteudo = dadosConteudo[0];
                        String descricaoConteudo = dadosConteudo[1];
                        String imagemReferente = dadosConteudo[2];
                        String questoesString = dadosConteudo[3];

                        conteudosTextView.setText("Conteúdos: "+nomeConteudo.toUpperCase());
                        descricao.setText(descricaoConteudo);

                        Glide.with(getApplicationContext())
                                .load(imagemReferente)
                                .into(temaImageView);

                        String progressoJogador = prefs.getString("prefs_progresso");
                        String[] infoProgresso = progressoJogador.split("\\|");

                        int mundoAtual = Integer.parseInt(infoProgresso[2].replace("MundoAtual=",""));
                        int faseAtual = Integer.parseInt(infoProgresso[1].replace("FaseAtual=",""));

                        Button iniciarFaseButton = dialogPersonalizado.findViewById(R.id.iniciarFaseButton);
                        iniciarFaseButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mundoAtual >= 1 && faseAtual >= (finalI + 1)) {
                                    Intent intent = new Intent(Mundo2Activity.this, FaseActivity.class);
                                    intent.putExtra("fase", finalI + 1); // só passa o número da fase

                                    startActivity(intent);
                                    dialogPersonalizado.cancel();

                                }else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Você ainda não pode acessar essa fase! Complete a fase " + (faseAtual) + " antes de acessá-la!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        });

                        dialogPersonalizado.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialogPersonalizado.show();

                        Log.d("CACHE SUPREME",questoesString);

                    }
                }
            });
        }
    }

    public void mostrarEstrelas(){

    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}