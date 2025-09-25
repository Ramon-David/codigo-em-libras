package com.example.codigo_em_libras;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FaseActivity extends AppCompatActivity implements Fases.QuestaoCallback {

    private static final String TAG = "FIRESTORE_DEBUG";

    private List<Questao> questoesList;
    private int indexAtual = 0;
    private FrameLayout rootLayout;
    private Fases fases;
    private FirebaseFirestore bancoDeDados;

    private String conteudoAtual;
    private int faseAtual;
    private static final int MAX_QUESTOES = 4;

    // Lista de conteúdos (na ordem das fases)
    private final String[] listaConteudos = {
            "alfabeto",
            "numeros",
            "saudacoes",
            "cores",
            "animais",
            "profissoes",
            "frases"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fase);

        rootLayout = findViewById(R.id.rootLayout);
        fases = new Fases(this);
        bancoDeDados = FirebaseFirestore.getInstance();

        // Pega a fase enviada pela Mundo1Activity (padrão = 1)
        faseAtual = getIntent().getIntExtra("fase", 1);

        // Define o conteúdo correto de acordo com a fase
        if (faseAtual >= 1 && faseAtual <= listaConteudos.length) {
            conteudoAtual = listaConteudos[faseAtual - 1];
        } else {
            conteudoAtual = listaConteudos[0]; // fallback se algo vier errado
        }

        // Sempre começa do zero quando reabre
        indexAtual = 0;

        carregarQuestoes(conteudoAtual);
    }

    private void carregarQuestoes(String conteudoAtual) {
        bancoDeDados.collection("Mundos")
                .document("mundo1")
                .collection("conteudos")
                .document(conteudoAtual)
                .collection("questoes")
                .orderBy(FieldPath.documentId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    questoesList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        try {
                            // Debug dos campos crus
                            Object rawSinal = doc.get("sinalUrl");
                            Object rawResposta = doc.get("respostaCorreta");
                            Log.d(TAG, "Doc: " + doc.getId() +
                                    " sinalUrl class=" + (rawSinal == null ? "null" : rawSinal.getClass().getName()) +
                                    " respostaCorreta class=" + (rawResposta == null ? "null" : rawResposta.getClass().getName()));

                            // Normalizar dados
                            Map<String, Object> data = doc.getData();
                            Questao questao = new Questao();

                            questao.pergunta = safeString(data.get("pergunta"));
                            questao.sinalUrl = toStringList(data.get("sinalUrl"));
                            questao.alternativas = toStringList(data.get("alternativas"));

                            // respostaCorreta -> String (para tipos 1–3)
                            Object respostaCorreta = data.get("respostaCorreta");
                            if (respostaCorreta instanceof List) {
                                List<?> rcList = (List<?>) respostaCorreta;
                                questao.respostaCorreta = rcList.size() > 0 ? String.valueOf(rcList.get(0)) : null;
                                Log.w(TAG, "Doc " + doc.getId() + " tinha respostaCorreta como List — usando primeiro elemento: " + questao.respostaCorreta);
                            } else {
                                questao.respostaCorreta = safeString(respostaCorreta);
                            }

                            // respostaCorretaArray -> apenas para tipo 4
                            questao.respostaCorretaArray = toStringList(data.get("respostaCorretaArray"));

                            questao.tipo = parseIntSafe(data.get("tipo"), 1);
                            questao.nivel = parseIntSafe(data.get("nivel"), 1);
                            questao.conteudo = conteudoAtual;

                            questoesList.add(questao);

                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao processar doc " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "Total questoes carregadas: " + (questoesList == null ? 0 : questoesList.size()));
                    mostrarQuestao();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar questões!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro get questoes", e);
                });
    }

    private void mostrarQuestao() {
        if (indexAtual >= questoesList.size() || indexAtual >= MAX_QUESTOES) {
            Toast.makeText(this, "Conteúdo concluído!", Toast.LENGTH_SHORT).show();
            finish();

            // 🔹 Atualiza o progresso do jogador no Firestore
            int proximaFase = faseAtual + 1;
            FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

            if (usuarioAtual != null) {
                String userId = usuarioAtual.getUid();

                bancoDeDados.collection("JogadorDados")
                        .document(userId)
                        .collection("Dados do Jogo")
                        .document("Progresso")
                        .get()
                        .addOnSuccessListener(doc -> {
                            long faseSalva = doc.contains("FaseAtual") ? doc.getLong("FaseAtual") : 1;
                            if (proximaFase > faseSalva) {
                                doc.getReference().update("FaseAtual", proximaFase);
                            }
                        });
            }
            return;
        }

        Questao questao = questoesList.get(indexAtual);
        rootLayout.removeAllViews();

        View viewQuestao;
        switch (questao.tipo) {
            case 1:
                viewQuestao = fases.criarFaseTipo1(getLayoutInflater(), rootLayout, questao, this);
                break;
            case 2:
                viewQuestao = fases.criarFaseTipo2(getLayoutInflater(), rootLayout, questao, this);
                break;
            case 3:
                viewQuestao = fases.criarFaseTipo3(getLayoutInflater(), rootLayout, questao, this);
                break;
            /*
            case 4:
                viewQuestao = fases.criarFaseTipo4(getLayoutInflater(), rootLayout, questao, this);
                break;
            case 5:
                viewQuestao = fases.criarFaseTipo5(getLayoutInflater(), rootLayout, questao, this);
                break;
             */
            default:
                return;
        }

        rootLayout.addView(viewQuestao);
    }

    @Override
    public void proximaQuestao() {
        indexAtual++;
        mostrarQuestao();
    }

    // --------- Helpers ---------
    private String safeString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object o) {
        List<String> out = new ArrayList<>();
        if (o == null) return out;
        if (o instanceof String) {
            out.add((String) o);
            return out;
        }
        if (o instanceof List) {
            for (Object item : (List<?>) o) {
                out.add(item == null ? "" : String.valueOf(item));
            }
            return out;
        }
        out.add(String.valueOf(o)); // fallback
        return out;
    }

    private int parseIntSafe(Object o, int defaultValue) {
        if (o == null) return defaultValue;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
