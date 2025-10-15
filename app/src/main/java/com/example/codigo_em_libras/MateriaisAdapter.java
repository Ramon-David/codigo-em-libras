package com.example.codigo_em_libras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MateriaisAdapter extends RecyclerView.Adapter<MateriaisAdapter.MaterialViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Material material);
    }

    private final List<Material> materiais;
    private final OnItemClickListener listener;

    public MateriaisAdapter(List<Material> materiais, OnItemClickListener listener) {
        this.materiais = materiais;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materiais.get(position);
        holder.titulo.setText(material.getTitulo());
        holder.descricao.setText(material.getDescricao());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(material));
    }

    @Override
    public int getItemCount() {
        return materiais.size();
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, descricao;

        MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textViewTitulo);
            descricao = itemView.findViewById(R.id.textViewDescricao);
        }
    }
}
