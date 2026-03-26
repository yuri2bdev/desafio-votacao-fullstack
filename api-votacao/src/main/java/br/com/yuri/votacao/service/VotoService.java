package br.com.yuri.votacao.service;

import br.com.yuri.votacao.client.CpfValidatorClient;
import br.com.yuri.votacao.domain.EscolhaVoto;
import br.com.yuri.votacao.domain.ResultadoStatus;
import br.com.yuri.votacao.dto.ResultadoVotacaoResponse;
import br.com.yuri.votacao.dto.VoteEventDto;
import br.com.yuri.votacao.dto.VotoRecebidoResponse;
import br.com.yuri.votacao.dto.VotoRequest;
import br.com.yuri.votacao.exception.BusinessConflictException;
import br.com.yuri.votacao.exception.IntegracaoException;
import br.com.yuri.votacao.messaging.VoteProducer;
import br.com.yuri.votacao.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VotoService {

    private final PautaService pautaService;
    private final SessaoService sessaoService;
    private final CpfValidatorClient cpfValidatorClient;
    private final VotoRepository votoRepository;
    private final VoteProducer voteProducer;
    private final Clock appClock;

    public VotoRecebidoResponse receberVoto(UUID pautaId, VotoRequest request) {
        pautaService.buscarOuFalhar(pautaId);
        sessaoService.validarSessaoAberta(pautaId);
        cpfValidatorClient.validarCpf(request.associadoId());

        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())) {
            throw new BusinessConflictException("Associado ja votou nesta pauta");
        }

        try {
            voteProducer.publicar(new VoteEventDto(
                    pautaId,
                    request.associadoId(),
                    request.escolha(),
                    LocalDateTime.now(appClock).toString()
            ));
        } catch (Exception ex) {
            throw new IntegracaoException("Falha ao publicar voto no Kafka", ex);
        }

        return new VotoRecebidoResponse(
                pautaId,
                request.associadoId(),
                "Voto recebido e enviado para processamento"
        );
    }

    public ResultadoVotacaoResponse obterResultado(UUID pautaId) {
        pautaService.buscarOuFalhar(pautaId);

        long totalSim = votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.SIM);
        long totalNao = votoRepository.countByPautaIdAndEscolha(pautaId, EscolhaVoto.NAO);

        return new ResultadoVotacaoResponse(pautaId, totalSim, totalNao, calcularStatus(totalSim, totalNao));
    }

    private ResultadoStatus calcularStatus(long totalSim, long totalNao) {
        if (totalSim == 0 && totalNao == 0) {
            return ResultadoStatus.SEM_VOTOS;
        }
        if (totalSim > totalNao) {
            return ResultadoStatus.APROVADA;
        }
        if (totalNao > totalSim) {
            return ResultadoStatus.REPROVADA;
        }
        return ResultadoStatus.EMPATE;
    }
}

