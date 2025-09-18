package com.example.codigo_em_libras;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class Missao {//Aqui é onde as fases serão criadas
    ArrayList<Fases> listaDeFases = new ArrayList<Fases>();
    Fases fase = new Fases();
    public void criarMissao(LayoutInflater inflater, FrameLayout rootLayout){
        View novaFase = fase.criarFaseTipo1(inflater, rootLayout);
    }

    public void avancarFase(){

    }
}
