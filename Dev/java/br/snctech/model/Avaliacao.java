package br.snctech.model;

import br.snctech.Questao;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

/** Representa uma avaliação criada pelo professor. As questões são as mesmas
 * cadastradas no módulo de gerenciamento de questões (br.snctech.Questao). */
public class Avaliacao {
    private final int id;
    private String nome;
    private List<Questao> questoes;
    private boolean respondida;
    private String turma;
    private LocalDate data;
    private LocalDate prazo;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private boolean disponibilizada;

    public Avaliacao(int id, String nome, List<Questao> questoes) {
        this.id = id;
        this.nome = nome;
        this.questoes = new ArrayList<>(questoes);
        this.respondida = false;
        this.turma = "";
        this.data = null;
        this.prazo = null;
        this.horaInicio = LocalTime.MIDNIGHT;
        this.horaFim = LocalTime.MIDNIGHT;
        this.disponibilizada = false;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public List<Questao> getQuestoes() { return new ArrayList<>(questoes); }
    public boolean isRespondida() { return respondida; }
    public void setRespondida(boolean respondida) { this.respondida = respondida; }
    public void setNome(String nome) { this.nome = nome; }
    public void setQuestoes(List<Questao> questoes) { this.questoes = new ArrayList<>(questoes); }
    public String getTurma() { return turma; }
    public LocalDate getData() { return data; }
    public LocalDate getPrazo() { return prazo; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public boolean isDisponibilizada() { return disponibilizada; }
    public void setTurma(String turma) { this.turma = turma; }
    public void setData(LocalDate data) { this.data = data; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public void setDisponibilizada(boolean disponibilizada) { this.disponibilizada = disponibilizada; }
}
