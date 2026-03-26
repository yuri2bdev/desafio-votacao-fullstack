package br.com.yuri.votacao.repository;

import br.com.yuri.votacao.domain.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, UUID> {

    Optional<SessaoVotacao> findByPautaId(UUID pautaId);

    boolean existsByPautaId(UUID pautaId);
}

