package br.com.yuri.votacao.dto;

import java.util.UUID;

public record VotoRecebidoResponse(
        UUID pautaId,
        String associadoId,
        String mensagem
) {
}

