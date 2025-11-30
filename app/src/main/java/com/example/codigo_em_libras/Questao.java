package com.example.codigo_em_libras;

import java.util.List;

public class Questao {
    // Campos do Firestore
    public String pergunta;
    public List<String> sinalUrl;
    public List<String> alternativas;
    public String respostaCorreta;            // Para os tipos 1–3, 5
    public List<String> respostaCorretaArray; // Para o tipo 4
    public List<String> alternativasTipo4Array;
    public int tipo;
    public int nivel;
    public String conteudo;

    // Construtor vazio (Firebase exige)
    public Questao() {}

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public List<String> getSinalUrl() {
        return sinalUrl;
    }

    public void setSinalUrl(List<String> sinalUrl) {
        this.sinalUrl = sinalUrl;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public List<String> getAlternativasTipo4Array() {
        return alternativasTipo4Array;
    }

    public void setAlternativasTipo4Array(List<String> alternativasTipo4Array) {
        this.alternativasTipo4Array = alternativasTipo4Array;
    }

    public List<String> getRespostaCorretaArray() {
        return respostaCorretaArray;
    }

    public void setRespostaCorretaArray(List<String> respostaCorretaArray) {
        this.respostaCorretaArray = respostaCorretaArray;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
