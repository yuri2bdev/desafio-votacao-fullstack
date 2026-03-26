package br.com.yuri.votacao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID id,
        UUID pautaId,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento
) {
}

