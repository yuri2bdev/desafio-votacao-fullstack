package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.ResultadoVotacaoResponse;
import br.com.yuri.votacao.dto.VotoRecebidoResponse;
import br.com.yuri.votacao.dto.VotoRequest;
import br.com.yuri.votacao.service.VotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class VotoController {

    private final VotoService votoService;

    @PostMapping("/{pautaId}/votos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VotoRecebidoResponse votar(@PathVariable UUID pautaId, @Valid @RequestBody VotoRequest request) {
        return votoService.receberVoto(pautaId, request);
    }

    @GetMapping("/{pautaId}/resultado")
    public ResultadoVotacaoResponse resultado(@PathVariable UUID pautaId) {
        return votoService.obterResultado(pautaId);
    }
}

