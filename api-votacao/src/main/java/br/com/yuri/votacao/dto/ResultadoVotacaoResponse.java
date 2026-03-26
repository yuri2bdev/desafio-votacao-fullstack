package br.com.yuri.votacao.dto;

import br.com.yuri.votacao.domain.ResultadoStatus;

import java.util.UUID;

public record ResultadoVotacaoResponse(
        UUID pautaId,
        long totalSim,
        long totalNao,
        ResultadoStatus status
) {
}

