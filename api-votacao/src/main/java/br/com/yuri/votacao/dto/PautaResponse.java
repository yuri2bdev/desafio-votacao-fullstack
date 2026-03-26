package br.com.yuri.votacao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PautaResponse(
        UUID id,
        String descricao,
        LocalDateTime dataCriacao
) {
}

