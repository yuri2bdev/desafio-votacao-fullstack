package br.com.yuri.votacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePautaRequest(
        @NotBlank(message = "Descricao da pauta e obrigatoria")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String descricao
) {
}

