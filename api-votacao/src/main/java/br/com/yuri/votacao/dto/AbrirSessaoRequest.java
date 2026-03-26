package br.com.yuri.votacao.dto;

import jakarta.validation.constraints.Min;

public record AbrirSessaoRequest(
        @Min(value = 1, message = "Duracao deve ser no minimo 1 segundo")
        Integer duracaoSegundos
) {
}

