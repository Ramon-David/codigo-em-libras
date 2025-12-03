package com.example.codigo_em_libras;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SalvamentoDados {
    private static FirebaseFirestore bd;
    public static void salvarDadosConta(Context context){
        bd = FirebaseFirestore.getInstance();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PrefsHelper prefs = new PrefsHelper(context);
        String dadosConta = prefs.getString("prefs_conta");

        if (dadosConta != null && dadosConta.contains(";")) {
            String[] partes = dadosConta.split(";");

            String email = partes[0];
            String nome = partes[1];
            String imagemBase64 = partes[2];
            Boolean modoEscuro = Boolean.parseBoolean(partes[3]);

            Map<String, Object> dados = new HashMap<>();
            dados.put("Email", email);
            dados.put("Nome", nome);
            dados.put("ImgConta",imagemBase64);
            dados.put("ModoNoturno",modoEscuro);

            bd.collection("JogadorDados")
                    .document(uid)
                    .collection("Dados da Conta")
                    .document("DadosDaConta")
                    .update(dados)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Dados atualizados com sucesso!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erro ao atualizar", e);
                    });

            String dadosJogo = prefs.getString("prefs_progresso");
            Map<String, Object> dados2 = new HashMap<>();

            String[] infos = dadosJogo.split("\\|");
            int totalEstrelas = 0;

            totalEstrelas = contarEstrelas(context);
            infos[0] = "EstrelasTotais="+totalEstrelas;

            Log.d("Cache Estrelas",infos[0]);
            prefs.putInt("estrelas",totalEstrelas);


            for (String info: infos) {
                String[] kv = info.split("=");

                String chave = kv[0].trim();
                String valor = kv[1].trim();


                // É um campo tipo: mundo1_fase3=2
                if (chave.startsWith("mundo")) {
                    salvarEstrelasDoMundo(bd, uid, chave, valor);
                }
                else {
                    dados2.put(chave, Integer.parseInt(valor));
                }
            }

            Log.d("CACHE INFO d",dados2.toString());


            bd.collection("JogadorDados")
                    .document(uid)
                    .collection("Dados do Jogo")
                    .document("Progresso")
                    .update(dados2)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Dados do progresso atualizados com sucesso!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Erro ao atualizar", e);
                    });


        }
    }

    public static int contarEstrelas(Context context){
        PrefsHelper prefs = new PrefsHelper(context);
        String dadosJogo = prefs.getString("prefs_progresso");
        String[] infos = dadosJogo.split("\\|");
        int totalEstrelas = 0;

        for (String info: infos) {
            String[] kv = info.split("=");

            if (kv.length > 1) {
                String chave = kv[0].trim();
                String valor = kv[1].trim();

                if (chave.startsWith("mundo")) {
                    totalEstrelas += Integer.parseInt(valor);
                }
            }

        }

        infos[0] = "EstrelasTotais="+totalEstrelas;
        prefs.putInt("estrelas",totalEstrelas);
        return totalEstrelas;
    }

    private static void salvarEstrelasDoMundo(FirebaseFirestore db, String uid,
                                              String chave, String valor) {

        try {
            Map<String, Object> dados = new HashMap<>();

            int estrelas = Integer.parseInt(valor);
            dados.put("estrelas", estrelas);

            // separa "mundo1_fase3"
            String[] partes = chave.split("_");
            String mundo = partes[0]; // mundo1
            String fase = partes[1];  // fase3

            db.collection("JogadorDados")
                    .document(uid)
                    .collection("Dados do Jogo")
                    .document("Progresso")
                    .collection(mundo)
                    .document(fase)
                    .set(dados)
                    .addOnSuccessListener(a -> Log.d("PROGRESSO", mundo + "/" + fase + " salvo"))
                    .addOnFailureListener(e -> Log.e("ERRO", "Falha ao salvar fase", e));

        } catch (Exception e) {
            Log.e("ERRO", "Erro ao salvar estrela: " + chave + "=" + valor, e);
        }
    }

}
