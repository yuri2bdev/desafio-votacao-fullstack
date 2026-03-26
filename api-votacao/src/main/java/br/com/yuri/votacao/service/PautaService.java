package br.com.yuri.votacao.service;

import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.dto.CreatePautaRequest;
import br.com.yuri.votacao.dto.PautaResponse;
import br.com.yuri.votacao.exception.ResourceNotFoundException;
import br.com.yuri.votacao.repository.PautaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PautaService {

    private final PautaRepository pautaRepository;
    private final Clock appClock;

    public PautaResponse criar(CreatePautaRequest request) {
        Pauta pauta = Pauta.builder()
                .id(UUID.randomUUID())
                .descricao(request.descricao())
                .dataCriacao(LocalDateTime.now(appClock))
                .build();

        Pauta saved = pautaRepository.save(pauta);
        return new PautaResponse(saved.getId(), saved.getDescricao(), saved.getDataCriacao());
    }

    public Pauta buscarOuFalhar(UUID pautaId) {
        return pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pauta nao encontrada: " + pautaId));
    }
}

