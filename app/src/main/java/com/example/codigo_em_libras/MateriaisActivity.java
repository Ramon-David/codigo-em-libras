package com.example.codigo_em_libras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class MateriaisActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MateriaisAdapter adapter;
    private ArrayList<Material> materiaisList;

    private String mundoSelecionado;

    private ImageButton voltarImageButton;
    private TextView tituloToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materiais);

        // Inicializa os componentes da tela
        recyclerView = findViewById(R.id.recyclerViewMateriais);
        progressBar = findViewById(R.id.progressBarMateriais);
        voltarImageButton = findViewById(R.id.voltarImageButton);
        tituloToolBar = findViewById(R.id.tituloToolBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        materiaisList = new ArrayList<>();

        adapter = new MateriaisAdapter(materiaisList, this::abrirMaterial);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER); // Remove o efeito visual das bordas do RecyclerView
        recyclerView.setAdapter(adapter);

        // Recebe qual mundo foi selecionado
        mundoSelecionado = getIntent().getStringExtra("mundo");

        // Define o título conforme o mundo recebido
        if (mundoSelecionado != null) {
            switch (mundoSelecionado) {
                case "mundo1": tituloToolBar.setText("Mundo 1"); break;
                case "mundo2": tituloToolBar.setText("Mundo 2"); break;
                case "mundo3": tituloToolBar.setText("Mundo 3"); break;
                default: tituloToolBar.setText("Materiais");
            }
        } else {
            tituloToolBar.setText("Materiais");
        }

        // Configura o botão de voltar para fechar a tela
        voltarImageButton.setOnClickListener(v -> finish());

        carregarMateriais();
    }

    private void carregarMateriais() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        String userId = usuarioAtual.getUid();

        // Carregamos o progresso do jogador
        db.collection("JogadorDados")
                .document(userId)
                .collection("Dados do Jogo")
                .document("Progresso")
                .get()
                .addOnSuccessListener(progressoSnap -> {

                    Long faseAtualValor = progressoSnap.getLong("FaseAtual");
                    Long mundoAtualValor = progressoSnap.getLong("MundoAtual");

                    long faseAtual = (faseAtualValor != null) ? faseAtualValor : 0;
                    long mundoAtual = (mundoAtualValor != null) ? mundoAtualValor : 1;

                    db.collection("Materiais")
                            .document(mundoSelecionado)
                            .collection("itens")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                materiaisList.clear();

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                                        Material material = doc.toObject(Material.class);

                                        // Lógica de desbloqueio de fase
                                        String faseStr = material.getFaseVinculada();
                                        int numeroFaseMaterial = Integer.parseInt(faseStr.replace("fase", ""));

                                        boolean desbloquear;

                                        // DESBLOQUEIA SOMENTE SE O MUNDO DO MATERIAL == O MUNDO ATUAL DO JOGADOR
                                        if (
                                                (mundoSelecionado.equals("mundo1") && mundoAtual == 1) ||
                                                        (mundoSelecionado.equals("mundo2") && mundoAtual == 2) ||
                                                        (mundoSelecionado.equals("mundo3") && mundoAtual == 3)
                                        ) {
                                            // desbloqueio normal
                                            desbloquear = faseAtual >= numeroFaseMaterial;
                                        } else {
                                            // impede desbloquear mundos futuros
                                            desbloquear = false;
                                        }

                                        material.setDesbloqueado(desbloquear);

                                        materiaisList.add(material);
                                    }

                                    Collections.sort(materiaisList, (m1, m2) ->
                                            Integer.compare(m1.getOrdem(), m2.getOrdem()));

                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(this, "Nenhum material encontrado.", Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao carregar materiais.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar progresso!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void abrirMaterial(Material material) {

        // BLOQUEIO: material vinculado a uma fase que o usuário não completou
        if (!material.isDesbloqueado()) {
            Toast.makeText(this,
                    "Material bloqueado. Complete a " + material.getFaseVinculada() + ".",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ABRIR PDF
        if (material.getUrl() != null && !material.getUrl().isEmpty()) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(material.getUrl())));
        } else {
            Toast.makeText(this, "URL do PDF não encontrada.", Toast.LENGTH_SHORT).show();
        }
    }
}
