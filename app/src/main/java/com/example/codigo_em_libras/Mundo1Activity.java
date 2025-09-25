package com.example.codigo_em_libras;

import static android.app.PendingIntent.getActivity;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class Mundo1Activity extends AppCompatActivity {
    private FirebaseFirestore bancoDeDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mundo1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        ImageButton fase7ImageButton = findViewById(R.id.fase7ImageButton);
        botoesFases.add(fase7ImageButton);

        bancoDeDados = FirebaseFirestore.getInstance();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        String userId = usuarioAtual.getUid();

        for (int i = 0; i < botoesFases.size(); i++) {
            int finalI = i;
            botoesFases.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Dialog dialogPersonalizado = new Dialog(Mundo1Activity.this);
                    dialogPersonalizado.setContentView(R.layout.fase_iniciar);
                    dialogPersonalizado.getWindow().setLayout(dpToPx(Mundo1Activity.this,300),dpToPx(Mundo1Activity.this,400));

                    TextView faseAtualTextView = dialogPersonalizado.findViewById(R.id.faseAtualTextView);
                    faseAtualTextView.setText("Fase "+(finalI+1));
                    Log.d("FIREBASE", "Estrelas: " + 3 +
                            " | Fase: " + 3 +
                            " | Mundo: " + 3);

                    Button iniciarFaseButton = dialogPersonalizado.findViewById(R.id.iniciarFaseButton);
                    iniciarFaseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d("FIREBASE", "Estrelas: " + 3 +
                                    " | Fase: " + 2 +
                                    " | Mundo: " + 3);
                            // getActivity() retorna o contexto da Activity que hospeda o Fragment
                            bancoDeDados.collection("JogadorDados")
                                    .document(userId)
                                    .collection("Dados do Jogo")
                                    .document("Progresso")
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Long estrelasTotais = documentSnapshot.getLong("EstrelasTotais");
                                            Long faseAtual = documentSnapshot.getLong("FaseAtual");
                                            Long mundoAtual = documentSnapshot.getLong("MundoAtual");

                                            Log.d("FIREBASE", "Estrelas: " + estrelasTotais +
                                                    " | Fase: " + faseAtual +
                                                    " | Mundo: " + mundoAtual);

                                            if  (mundoAtual >= 1 && faseAtual >= (finalI + 1)){
                                                Intent intent = new Intent(Mundo1Activity.this, FaseActivity.class);
                                                intent.putExtra("conteudo", "alfabeto");
                                                startActivity(intent);
                                            }else{
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Você ainda não pode acessar essa fase! Complete a fase "+finalI+" antes de acessá-la!",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }


                                        } else {
                                            Log.d("FIREBASE", "Documento não encontrado!");
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao buscar dados", e));
                        }
                    });

                    dialogPersonalizado.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogPersonalizado.show();


                }
            });
        }


    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}