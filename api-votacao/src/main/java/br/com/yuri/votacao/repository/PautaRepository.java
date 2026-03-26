package br.com.yuri.votacao.repository;

import br.com.yuri.votacao.domain.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PautaRepository extends JpaRepository<Pauta, UUID> {
}

