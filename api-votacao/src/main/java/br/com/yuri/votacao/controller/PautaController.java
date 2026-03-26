package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.CreatePautaRequest;
import br.com.yuri.votacao.dto.PautaResponse;
import br.com.yuri.votacao.service.PautaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class PautaController {

    private final PautaService pautaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PautaResponse criar(@Valid @RequestBody CreatePautaRequest request) {
        return pautaService.criar(request);
    }
}

