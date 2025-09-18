package com.example.codigo_em_libras;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Fases {
    public View criarFaseTipo1(LayoutInflater inflater, FrameLayout rootLayout){
        View layoutFilho = inflater.inflate(R.layout.fase_tipo1,rootLayout, false);

        // Adicionando o layout filho ao pai
        TextView perguntaTextView = layoutFilho.findViewById(R.id.perguntaTipo1TextView);
        perguntaTextView.setText("Olá! Como vai?");

        Button alternativa1Button = layoutFilho.findViewById(R.id.alternativa1Tipo1Button);
        Button alternativa2Button = layoutFilho.findViewById(R.id.alternativa2Tipo1Button);
        Button alternativa3Button = layoutFilho.findViewById(R.id.alternativa3Tipo1Button);
        Button alternativa4Button = layoutFilho.findViewById(R.id.alternativa4Tipo1Button);

        alternativa1Button.setText("Bem");
        alternativa2Button.setText("Uma bosta");
        alternativa3Button.setText("Vivendo");
        alternativa4Button.setText("Melhor? Impossível!");

        return layoutFilho;
    }
}
