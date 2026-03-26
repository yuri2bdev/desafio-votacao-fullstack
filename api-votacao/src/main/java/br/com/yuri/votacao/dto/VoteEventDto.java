package br.com.yuri.votacao.dto;

import br.com.yuri.votacao.domain.EscolhaVoto;

import java.util.UUID;

public record VoteEventDto(
        UUID pautaId,
        String associadoId,
        EscolhaVoto escolha,
        String dataVoto
) {
}

