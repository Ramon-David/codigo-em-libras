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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            String respostaUsuario = editTextResposta.getText().toString().trim().toUpperCase();
            if (!respostaUsuario.isEmpty()) {
                verificarResposta(questao, respostaUsuario, context,layoutFilho,3);
            } else {
                Toast.makeText(context, "Digite uma resposta antes de enviar.", Toast.LENGTH_SHORT).show();
            }
        });

        return layoutFilho;
    }

    // ---------------- TIPO 4 ----------------
    public View criarFaseTipo4(LayoutInflater inflater, FrameLayout rootLayout, Questao questao, Context context) {
        View layoutFilho = inflater.inflate(R.layout.questao_tipo4, rootLayout, false);

        TextView pergunta = layoutFilho.findViewById(R.id.perguntaTipo4TextView);
        pergunta.setText(questao.pergunta);

        ImageView slot1 = layoutFilho.findViewById(R.id.slot1Tipo4);
        ImageView slot2 = layoutFilho.findViewById(R.id.slot2Tipo4);
        ImageView slot3 = layoutFilho.findViewById(R.id.slot3Tipo4);

        Button buttonEnviar = layoutFilho.findViewById(R.id.enviarTipo4Button);

        ArrayList<ImageView> slots = new ArrayList<>();
        slots.add(slot1);
        slots.add(slot2);
        slots.add(slot3);

        // 6 botões
        ImageButton[] botoes = {
                layoutFilho.findViewById(R.id.alternativa1Tipo4ImageButton),
                layoutFilho.findViewById(R.id.alternativa2Tipo4ImageButton),
                layoutFilho.findViewById(R.id.alternativa3Tipo4ImageButton),
                layoutFilho.findViewById(R.id.alternativa4Tipo4ImageButton),
                layoutFilho.findViewById(R.id.alternativa5Tipo4ImageButton),
                layoutFilho.findViewById(R.id.alternativa6Tipo4ImageButton)
        };

        // preencher imagens e setar apenas o comportamento de colocar nos slots
        for (int i = 0; i < botoes.length; i++) {
            final String url = (questao.alternativasTipo4Array.size() > i) ? questao.alternativasTipo4Array.get(i) : null;
            if (url != null) {
                Glide.with(context).load(url).into(botoes[i]);
                botoes[i].setTag(url);
            } else {
                botoes[i].setEnabled(false);
                botoes[i].setTag(null);
            }

            botoes[i].setOnClickListener(v -> {
                // apenas preencher o primeiro slot vazio
                for (ImageView s : slots) {
                    if (s.getTag() == null) {
                        s.setTag(v.getTag());
                        Glide.with(context).load(String.valueOf(v.getTag())).into(s);
                        break;
                    }
                }
            });
        }

        // Permitir remover uma imagem do slot ao clicar nele
        for (ImageView s : slots) {
            s.setOnClickListener(v -> {
                // se tiver algo, limpa
                if (v.getTag() != null) {
                    v.setTag(null);
                    ((ImageView) v).setImageDrawable(null); // remove imagem
                    Toast.makeText(context, "Resposta removida.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonEnviar.setOnClickListener(view -> {
            if (slots.get(0).getTag() != null &&
                    slots.get(1).getTag() != null &&
                    slots.get(2).getTag() != null) {

                String userResposta =
                        slots.get(0).getTag().toString().trim() + "|" +
                                slots.get(1).getTag().toString().trim() + "|" +
                                slots.get(2).getTag().toString().trim();

                verificarResposta(questao, userResposta, context, layoutFilho, 4);
            } else {
                Toast.makeText(context, "Preencha todos os campos antes de verificar.", Toast.LENGTH_SHORT).show();
            }
        });

        return layoutFilho;
    }

    //------------------ Mostrar resposta correta --------------------
    public void exibirRespostaCorreta(View layout, String alternativaCorreta, int tipo) {
        switch (tipo) {
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
                                1f, 1.5f, // escala X
                                1f, 1.5f, // escala Y
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
                Toast.makeText(layout.getContext(), "Resposta correta: " + alternativaCorreta, Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(layout.getContext(), "Resposta incorreta! Ainda vamos ver um jeito de mostrar a correta nesse caso", Toast.LENGTH_SHORT).show();
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
        // Desativa cliques extras
        desativarBotoes(layout, tipo);

        boolean acertou = false;

        // --- VERIFICAÇÃO DOS TIPOS 1 A 3 ---
        if (questao.respostaCorreta != null && respostaSelecionada.equalsIgnoreCase(questao.respostaCorreta)) {
            acertou = true;
        }

        // --- VERIFICAÇÃO DO TIPO 4 ---
        if (tipo == 4 && questao.respostaCorretaArray != null) {

            // respostaSelecionada vem no formato: "url1|url4|url6"
            String[] urlsEscolhidas = respostaSelecionada.split("\\|");

            List<String> esperado = questao.respostaCorretaArray;
            ArrayList<String> escolhidoIndices = new ArrayList<>();

            // Para cada URL escolhida, descobrir qual índice ela é dentro das alternativas do tipo 4
            for (String url : urlsEscolhidas) {
                int index = questao.alternativasTipo4Array.indexOf(url);
                if (index != -1) {
                    // Somamos +1 porque o Firebase usa 1 a 6, não 0 a 5
                    escolhidoIndices.add(String.valueOf(index + 1));
                }
            }

            // Compara a lista de índices obtidos com a lista correta
            if (escolhidoIndices.equals(esperado)) {
                acertou = true;
            }
        }

        if (acertou) {
            Toast.makeText(context, "✅ Acertou!", Toast.LENGTH_SHORT).show();
            acertos++;

            layout.postDelayed(() -> callback.proximaQuestao(), 1200); // Serve para agendar uma ação para rodar mais tarde, na UI Thread (a thread da interface)

        } else {
            Toast.makeText(context, "❌ Errou!", Toast.LENGTH_SHORT).show();
            erros++;
            exibirRespostaCorreta(layout, questao.respostaCorreta, tipo); // já chama proximaQuestao
        }
    }

    // ----------------- DESATIVA BOTÕES -----------------
    // Função para "bloquear" temporariamente os botões ao clicar pela primeira vez, evitando pular várias missões de uma vez
    private void desativarBotoes(View layout, int tipo) {
        switch (tipo) {
            case 1:
                Button[] botoes = new Button[4];
                botoes[0] = layout.findViewById(R.id.alternativa1Tipo1Button);
                botoes[1] = layout.findViewById(R.id.alternativa2Tipo1Button);
                botoes[2] = layout.findViewById(R.id.alternativa3Tipo1Button);
                botoes[3] = layout.findViewById(R.id.alternativa4Tipo1Button);
                for (Button b : botoes) b.setEnabled(false);
                break;
            case 2:
                ImageView[] imagens = new ImageView[4];
                imagens[0] = layout.findViewById(R.id.alternativa1Tipo2ImageView);
                imagens[1] = layout.findViewById(R.id.alternativa2Tipo2ImageView);
                imagens[2] = layout.findViewById(R.id.alternativa3Tipo2ImageView);
                imagens[3] = layout.findViewById(R.id.alternativa4Tipo2ImageView);
                for (ImageView img : imagens) img.setEnabled(false);
                break;
            case 3:
                Button enviar = layout.findViewById(R.id.enviarTipo3Button);
                enviar.setEnabled(false);
                break;
            case 4:
                ImageButton[] botoesTipo4 = {
                        layout.findViewById(R.id.alternativa1Tipo4ImageButton),
                        layout.findViewById(R.id.alternativa2Tipo4ImageButton),
                        layout.findViewById(R.id.alternativa3Tipo4ImageButton),
                        layout.findViewById(R.id.alternativa4Tipo4ImageButton),
                        layout.findViewById(R.id.alternativa5Tipo4ImageButton),
                        layout.findViewById(R.id.alternativa6Tipo4ImageButton)
                };
                for (ImageButton b : botoesTipo4) b.setEnabled(false);
                break;

                /*
                // A desabilitação de tipo 4 vai funcionar assim quando o botão "Enviar" for corrigido.
                Button enviar = layout.findViewById(R.id.enviarTipo4Button);
                enviar.setEnabled(false);
                break;
                */

                // Quando testar, provavelmente vai precisar desabilitar somente o botão de enviar.
            /*
            case 5:
                break;
            */
        }
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