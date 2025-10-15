package com.example.codigo_em_libras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class MateriaisActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MateriaisAdapter adapter;
    private ArrayList<Material> materiaisList;

    private String mundoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materiais);

        recyclerView = findViewById(R.id.recyclerViewMateriais);
        progressBar = findViewById(R.id.progressBarMateriais);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        materiaisList = new ArrayList<>();
        adapter = new MateriaisAdapter(materiaisList, this::abrirPDF);
        recyclerView.setAdapter(adapter);

        // Recebe qual mundo foi selecionado
        mundoSelecionado = getIntent().getStringExtra("mundo");

        carregarMateriais();
    }

    private void carregarMateriais() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Materiais")
                .document(mundoSelecionado)
                .collection("itens")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materiaisList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // Converte cada documento em um objeto Material
                            Material material = doc.toObject(Material.class);
                            materiaisList.add(material);
                        }
                        // Ordena os materiais pelo campo "ordem"
                        Collections.sort(materiaisList, (m1, m2) -> Integer.compare(m1.getOrdem(), m2.getOrdem()));
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
    }

    private void abrirPDF(Material material) {
        if (material.getUrl() != null && !material.getUrl().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(material.getUrl()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL do PDF não encontrada.", Toast.LENGTH_SHORT).show();
        }
    }
}
