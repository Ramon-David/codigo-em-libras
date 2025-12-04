package com.example.codigo_em_libras;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
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
    private static final int MAX_QUESTOES = 5;

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

                            // As 6 imagens do tipo 4
                            questao.alternativasTipo4Array = toStringList(data.get("alternativasArray"));

                            // Ordem correta dos 3 slots
                            questao.respostaCorretaArray = toStringList(data.get("respostaCorretaArray"));

                            questao.tipo = parseIntSafe(data.get("tipo"), 1);
                            questao.nivel = parseIntSafe(data.get("nivel"), 1);
                            questao.conteudo = conteudoAtual;

                            questoesList.add(questao);

                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao processar doc " + doc.getId() + ": " + e.getMessage(), e);
                        }
                    }

                    // Randomização por tipo (mantendo a ordem 1 -> 2 -> 3 -> 2 -> 4)
                    questoesList = organizarPorTipo(questoesList);
                    mostrarQuestao();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar questões!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro get questoes", e);
                });
    }

    private List<Questao> organizarPorTipo(List<Questao> originais) {
        List<Questao> tipo1 = new ArrayList<>();
        List<Questao> tipo2 = new ArrayList<>();
        List<Questao> tipo3 = new ArrayList<>();
        List<Questao> tipo4 = new ArrayList<>();

        for (Questao q : originais) {
            switch (q.tipo) {
                case 1:
                    tipo1.add(q);
                    break;
                case 2:
                    tipo2.add(q);
                    break;
                case 3:
                    tipo3.add(q);
                    break;
                case 4:
                    tipo4.add(q);
                    break;
            }
        }

        // Embaralha cada grupo (que está armazenado em listas separadas)
        Collections.shuffle(tipo1);
        Collections.shuffle(tipo2);
        Collections.shuffle(tipo3);
        Collections.shuffle(tipo4);

        // Reconstrói na ordem desejada dos tipos: 1 → 2 → 3 → 2 → 4
        List<Questao> novaLista = new ArrayList<>();

        if (!tipo1.isEmpty()) novaLista.add(tipo1.remove(0)); // Obs.: esse modelo de condicional é mais compacto, ideal
        if (!tipo2.isEmpty()) novaLista.add(tipo2.remove(0)); // para quando só existe uma condição dentro do IF.
        if (!tipo3.isEmpty()) novaLista.add(tipo3.remove(0)); // O modelo convencional também funcionaria, mas optamos por
        if (!tipo2.isEmpty()) novaLista.add(tipo2.remove(0)); // esse para diminuir o código em si, que está muito grande.
        if (!tipo4.isEmpty()) novaLista.add(tipo4.remove(0));

        return novaLista;
    }

    private void mostrarQuestao() {
        if (indexAtual >= questoesList.size() || indexAtual >= MAX_QUESTOES) {
            Toast.makeText(this, "Conteúdo concluído!", Toast.LENGTH_SHORT).show();

            // Atualiza o progresso do jogador no Firestore
            int proximaFase = faseAtual + 1;
            FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

            PrefsHelper prefs = new PrefsHelper(FaseActivity.this);
            String progressoJogador = prefs.getString("prefs_progresso");

            String[] infoProgresso = progressoJogador.split("\\|");

            String mundoAtualString = infoProgresso[2].replace("MundoAtual=","");
            String faseRegistradaString = infoProgresso[1].replace("FaseAtual=","");

            int mundoAtual = parseIntSafe(mundoAtualString,1);
            int faseRegistrada = parseIntSafe(faseRegistradaString,1);

            if (faseRegistrada < proximaFase){
                if (proximaFase > 7) {
                    infoProgresso[1] = "FaseAtual="+1;
                    infoProgresso[2] = "MundoAtual="+(mundoAtual+1);
                }else{
                    infoProgresso[1] = "FaseAtual="+proximaFase;
                }
            }

            String chaveEstrela = "mundo"+mundoAtual+"_fase"+faseAtual+"=";

            int estrelaPorFase = 0;
            int numeroEstrelasAtual = fases.contarEstrelas();

            for (int i = 0; i < infoProgresso.length; i++){
                if (infoProgresso[i].contains(chaveEstrela)){
                    String estrelaPorFaseString = infoProgresso[i].replace(chaveEstrela,"");
                    estrelaPorFase = parseIntSafe(estrelaPorFaseString, 0);

                    if (numeroEstrelasAtual > estrelaPorFase) {
                        infoProgresso[i] = chaveEstrela+numeroEstrelasAtual;
                    }
                    break;
                }
            }

            StringBuilder dadosAtualizados = new StringBuilder();

            for (String item : infoProgresso) {
                dadosAtualizados.append(item).append("|");
            }

            prefs.putString("prefs_progresso",dadosAtualizados.toString());
            Log.d("SOCORRO cache", dadosAtualizados.toString());

            new SalvamentoDados().salvarDadosConta(FaseActivity.this);
            Toast.makeText(getApplicationContext(),"Você recebeu "+numeroEstrelasAtual+" estrelas!",Toast.LENGTH_SHORT).show();

            finish();
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
            case 4:
                viewQuestao = fases.criarFaseTipo4(getLayoutInflater(), rootLayout, questao, this);
                break;
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
