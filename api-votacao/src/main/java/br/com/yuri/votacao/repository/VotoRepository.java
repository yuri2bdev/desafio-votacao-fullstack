package br.com.yuri.votacao.repository;

import br.com.yuri.votacao.domain.EscolhaVoto;
import br.com.yuri.votacao.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {

    boolean existsByPautaIdAndAssociadoId(UUID pautaId, String associadoId);

    long countByPautaIdAndEscolha(UUID pautaId, EscolhaVoto escolha);
}

