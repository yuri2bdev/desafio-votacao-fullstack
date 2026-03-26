package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.AbrirSessaoRequest;
import br.com.yuri.votacao.dto.SessaoResponse;
import br.com.yuri.votacao.service.SessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class SessaoController {

    private final SessaoService sessaoService;

    @PostMapping("/{pautaId}/sessao")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse abrirSessao(
            @PathVariable UUID pautaId,
            @Valid @RequestBody(required = false) AbrirSessaoRequest request
    ) {
        Integer duracao = request == null ? null : request.duracaoSegundos();
        return sessaoService.abrirSessao(pautaId, duracao);
    }
}

