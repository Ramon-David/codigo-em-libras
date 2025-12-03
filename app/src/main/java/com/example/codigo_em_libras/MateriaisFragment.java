package com.example.codigo_em_libras;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class MateriaisFragment extends Fragment {

    private ImageView mundo1, mundo2, mundo3;

    public MateriaisFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materiais, container, false);
        FrameLayout rootLayout = view.findViewById(R.id.fragmentMateriais);

        View layoutFilho = inflater.inflate(R.layout.materiais_tipo, rootLayout, false);
        rootLayout.addView(layoutFilho);

        mundo1 = layoutFilho.findViewById(R.id.mundo1);
        mundo2 = layoutFilho.findViewById(R.id.mundo2);
        mundo3 = layoutFilho.findViewById(R.id.mundo3);

        mundo1.setOnClickListener(v -> abrirMateriais("mundo1"));
        mundo2.setOnClickListener(v -> abrirMateriais("mundo2"));
        mundo3.setOnClickListener(v -> abrirMateriais("mundo3"));



        return view;
    }

    private void abrirMateriais(String mundo) {
        Intent intent = new Intent(getActivity(), MateriaisActivity.class);
        intent.putExtra("mundo", mundo);
        startActivity(intent);
    }
}
