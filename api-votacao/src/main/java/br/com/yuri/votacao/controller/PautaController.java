package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.CreatePautaRequest;
import br.com.yuri.votacao.dto.PautaResponse;
import br.com.yuri.votacao.service.PautaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
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

    @GetMapping
    public List<PautaResponse> listar() { return pautaService.listarTodas(); }

    @GetMapping("/{id}")
    public PautaResponse buscarPorId(@PathVariable UUID id) {
        return pautaService.buscarPorId(id);
    }
}

