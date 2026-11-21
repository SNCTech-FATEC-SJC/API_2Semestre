package br.snctech.autenticacao.modelo;

/** Usuário autenticado (nunca guarda a senha). */
public record UsuarioLogado(int id, String nome, String email, Perfil perfil) {
}
