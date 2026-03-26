package br.com.yuri.votacao.service;

import br.com.yuri.votacao.domain.Pauta;
import br.com.yuri.votacao.domain.SessaoVotacao;
import br.com.yuri.votacao.dto.SessaoResponse;
import br.com.yuri.votacao.exception.BusinessConflictException;
import br.com.yuri.votacao.exception.ResourceNotFoundException;
import br.com.yuri.votacao.exception.SessaoFechadaException;
import br.com.yuri.votacao.repository.SessaoVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private static final int DURACAO_PADRAO_SEGUNDOS = 60;

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final PautaService pautaService;
    private final Clock appClock;

    public SessaoResponse abrirSessao(UUID pautaId, Integer duracaoSegundos) {
        if (sessaoVotacaoRepository.existsByPautaId(pautaId)) {
            throw new BusinessConflictException("Ja existe sessao aberta para esta pauta");
        }

        Pauta pauta = pautaService.buscarOuFalhar(pautaId);
        LocalDateTime dataAbertura = LocalDateTime.now(appClock);
        int duracaoEfetiva = duracaoSegundos == null ? DURACAO_PADRAO_SEGUNDOS : duracaoSegundos;

        SessaoVotacao sessao = SessaoVotacao.builder()
                .id(UUID.randomUUID())
                .pauta(pauta)
                .dataAbertura(dataAbertura)
                .dataFechamento(dataAbertura.plusSeconds(duracaoEfetiva))
                .build();

        SessaoVotacao saved = sessaoVotacaoRepository.save(sessao);

        return new SessaoResponse(
                saved.getId(),
                pautaId,
                saved.getDataAbertura(),
                saved.getDataFechamento()
        );
    }

    public void validarSessaoAberta(UUID pautaId) {
        SessaoVotacao sessao = sessaoVotacaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessao de votacao nao encontrada para pauta: " + pautaId));

        LocalDateTime agora = LocalDateTime.now(appClock);
        if (agora.isBefore(sessao.getDataAbertura()) || !agora.isBefore(sessao.getDataFechamento())) {
            throw new SessaoFechadaException("Sessao de votacao esta fechada para a pauta: " + pautaId);
        }
    }
}

