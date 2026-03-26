package br.com.yuri.votacao.dto;

import br.com.yuri.votacao.domain.EscolhaVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

public record VotoRequest(
        @NotBlank(message = "CPF do associado e obrigatorio")
        @CPF(message = "O CPF informado é matematicamente inválido")
        String associadoId,

        @NotNull(message = "Escolha do voto e obrigatoria")
        EscolhaVoto escolha
) {
}

