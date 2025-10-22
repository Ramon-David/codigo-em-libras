package com.example.codigo_em_libras;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class Fases {
    int erros;
    int acertos;
    FirebaseFirestore bd;
    // Interface de callback para informar a Activity que a questão terminou
    public interface QuestaoCallback {
        void proximaQuestao();
    }

    private final QuestaoCallback callback;

    public Fases(QuestaoCallback callback) {
        this.callback = callback;
    }

    // ----------------- TIPO 1 -----------------
    public View criarFaseTipo1(LayoutInflater inflater, FrameLayout rootLayout, Questao questao, Context context) {
        View layoutFilho = inflater.inflate(R.layout.questao_tipo1, rootLayout, false);

        TextView perguntaTextView = layoutFilho.findViewById(R.id.perguntaTipo1TextView);
        perguntaTextView.setText(questao.pergunta);

        // Carregar sinal (GIF ou imagem)
        ImageView sinalImageView = layoutFilho.findViewById(R.id.sinalTipo1ImageView);
        if (!questao.sinalUrl.isEmpty() && questao.sinalUrl.get(0) != null) {
            Log.d("DEBUG_IMAGE", "Tipo1 URL: " + questao.sinalUrl.get(0));
            Glide.with(context)
                    .load(questao.sinalUrl.get(0))
                    .into(sinalImageView);
        } else {
            Log.w("DEBUG_IMAGE", "sinalUrl vazio para Tipo1: " + questao.pergunta);
        }

        // Popular alternativas
        Button[] botoes = new Button[4];
        botoes[0] = layoutFilho.findViewById(R.id.alternativa1Tipo1Button);
        botoes[1] = layoutFilho.findViewById(R.id.alternativa2Tipo1Button);
        botoes[2] = layoutFilho.findViewById(R.id.alternativa3Tipo1Button);
        botoes[3] = layoutFilho.findViewById(R.id.alternativa4Tipo1Button);

        for (int i = 0; i < 4; i++) {
            botoes[i].setText(questao.alternativas.get(i));
            final String respostaEscolhida = questao.alternativas.get(i);
            botoes[i].setOnClickListener(v -> verificarResposta(questao, respostaEscolhida, context,layoutFilho,1));
        }

        return layoutFilho;
    }

    // ----------------- TIPO 2 -----------------
    public View criarFaseTipo2(LayoutInflater inflater, FrameLayout rootLayout, Questao questao, Context context) {
        View layoutFilho = inflater.inflate(R.layout.questao_tipo2, rootLayout, false);

        TextView perguntaTextView = layoutFilho.findViewById(R.id.perguntaTipo2TextView);
        perguntaTextView.setText(questao.pergunta);

        // Cada alternativa é um sinal (GIF ou imagem)
        ImageView[] botoesSinal = new ImageView[4];
        botoesSinal[0] = layoutFilho.findViewById(R.id.alternativa1Tipo2ImageView);
        botoesSinal[1] = layoutFilho.findViewById(R.id.alternativa2Tipo2ImageView);
        botoesSinal[2] = layoutFilho.findViewById(R.id.alternativa3Tipo2ImageView);
        botoesSinal[3] = layoutFilho.findViewById(R.id.alternativa4Tipo2ImageView);

        for (int i = 0; i < 4; i++) {
            if (!questao.alternativas.get(i).isEmpty()) {
                Log.d("DEBUG_IMAGE", "Tipo2 URL alternativa " + (i+1) + ": " + questao.alternativas.get(i));
                Glide.with(context)
                        .load(questao.alternativas.get(i))
                        .into(botoesSinal[i]);
                botoesSinal[i].setTag(questao.alternativas.get(i));
            } else {
                Log.w("DEBUG_IMAGE", "URL alternativa vazia Tipo2: " + questao.pergunta + " botão " + (i+1));
            }
            final String respostaEscolhida = questao.alternativas.get(i);
            botoesSinal[i].setOnClickListener(v -> verificarResposta(questao, respostaEscolhida, context,layoutFilho,2));
        }

        return layoutFilho;
    }

    // ----------------- TIPO 3 -----------------
    public View criarFaseTipo3(LayoutInflater inflater, FrameLayout rootLayout, Questao questao, Context context) {
        View layoutFilho = inflater.inflate(R.layout.questao_tipo3, rootLayout, false);

        TextView perguntaTextView = layoutFilho.findViewById(R.id.perguntaTipo3TextView);
        perguntaTextView.setText(questao.pergunta);

        ImageView sinalImageView = layoutFilho.findViewById(R.id.sinalTipo3ImageView);
        if (!questao.sinalUrl.isEmpty() && questao.sinalUrl.get(0) != null) {
            Log.d("DEBUG_IMAGE", "Tipo3 URL: " + questao.sinalUrl.get(0));
            Glide.with(context)
                    .load(questao.sinalUrl.get(0))
                    .into(sinalImageView);
        } else {
            Log.w("DEBUG_IMAGE", "sinalUrl vazio para Tipo3: " + questao.pergunta);
        }

        EditText editTextResposta = layoutFilho.findViewById(R.id.respostaTipo3EditText);
        Button buttonEnviar = layoutFilho.findViewById(R.id.enviarTipo3Button);

        buttonEnviar.setOnClickListener(v -> {
            String respostaUsuario = editTextResposta.getText().toString().toUpperCase();
            verificarResposta(questao, respostaUsuario, context,layoutFilho,3);
        });

        return layoutFilho;
    }

    // ----------------- TIPO 4 -----------------

    // ----------------- TIPO 5 -----------------


    //Mostra a resposta certa
    public void exibirRespostaCorreta(View layout, String alternativaCorreta, int tipo){
        switch (tipo){
            case 1:
                Button[] botoes = new Button[4];
                botoes[0] = layout.findViewById(R.id.alternativa1Tipo1Button);
                botoes[1] = layout.findViewById(R.id.alternativa2Tipo1Button);
                botoes[2] = layout.findViewById(R.id.alternativa3Tipo1Button);
                botoes[3] = layout.findViewById(R.id.alternativa4Tipo1Button);

                for (Button botao : botoes) {
                    if (botao.getText().toString().equals(alternativaCorreta)) {
                        // ✨ Animação de “pulsar” (cresce e volta)
                        ScaleAnimation anim = new ScaleAnimation(
                                1f, 1.1f, // escala X
                                1f, 1.1f, // escala Y
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                        );
                        anim.setDuration(300);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        anim.setInterpolator(new AccelerateDecelerateInterpolator());

                        botao.startAnimation(anim);
                    }
                }
                break;
            case 2:
                ImageView[] botoesImageView = new ImageView[4];
                botoesImageView[0] = layout.findViewById(R.id.alternativa1Tipo2ImageView);
                botoesImageView[1] = layout.findViewById(R.id.alternativa2Tipo2ImageView);
                botoesImageView[2] = layout.findViewById(R.id.alternativa3Tipo2ImageView);
                botoesImageView[3] = layout.findViewById(R.id.alternativa4Tipo2ImageView);


                for (ImageView botao : botoesImageView) {

                    Object tag = botao.getTag();

                    if (tag != null && tag.toString().equals(alternativaCorreta)) {
                        botao.setBackgroundColor(Color.parseColor("#105D0B"));
                    }
                }

                break;
            case 3:
                Toast.makeText(layout.getContext(), "Resposta correta: "+alternativaCorreta, Toast.LENGTH_SHORT).show();
                break;
            case 4:
                break;
            case 5:
                break;
        }

        if (callback != null) {
            layout.postDelayed(() -> callback.proximaQuestao(), 1200); // espera 800ms
        }

    }


    // ----------------- FUNÇÃO DE VERIFICAÇÃO -----------------
    private void verificarResposta(Questao questao, String respostaSelecionada, Context context, View layout, int tipo) {
        boolean acertou = false;

        if (questao.respostaCorreta != null && respostaSelecionada.equalsIgnoreCase(questao.respostaCorreta)) {
            acertou = true;
        }

        if (questao.respostaCorretaArray != null &&
                questao.respostaCorretaArray.equals(Arrays.asList(respostaSelecionada.split("")))) {
            acertou = true;
        }




        if (acertou) {
            Toast.makeText(context, "✅ Acertou!", Toast.LENGTH_SHORT).show();

            acertos++;
        } else {
            Toast.makeText(context, "❌ Errou!", Toast.LENGTH_SHORT).show();
            erros++;
        }


        exibirRespostaCorreta(layout,questao.respostaCorreta, tipo);
    }

    public int contarEstrelas() {
        int total = acertos + erros;
        int estrelasCalculadas = (int) Math.floor(acertos * 3.0 / total);

        return estrelasCalculadas;
/*
        if (usuarioAtual != null) {
            String userId = usuarioAtual.getUid();

            // Referência ao documento do progresso
            DocumentReference progressoRef = bd.collection("JogadorDados")
                    .document(userId)
                    .collection("Dados do Jogo")
                    .document("Progresso");

            progressoRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long faseAtual = documentSnapshot.getLong("FaseAtual");
                    Long estrelas = documentSnapshot.getLong("EstrelasTotais");

                    if (faseAtualValor > faseAtual) {
                        // Calcula estrelas (convertendo para long)

                        Toast.makeText(
                                context,"Estrelas Totais: " + estrelasCalculadas, Toast.LENGTH_SHORT
                        ).show();

                        // Atualiza o Firestore
                        progressoRef.update("EstrelasTotais", estrelasCalculadas)
                                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Progresso atualizado com sucesso!"))
                                .addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao atualizar progresso", e));
                    }
                }
            }).addOnFailureListener(e -> Log.e("FIREBASE", "Erro ao buscar progresso", e));
        }*/
    }

}