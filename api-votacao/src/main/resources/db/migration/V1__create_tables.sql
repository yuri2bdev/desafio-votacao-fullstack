CREATE TABLE pauta (
                       id UUID PRIMARY KEY,
                       descricao VARCHAR(255) NOT NULL,
                       data_criacao TIMESTAMP NOT NULL
);

CREATE TABLE sessao_votacao (
                                id UUID PRIMARY KEY,
                                pauta_id UUID NOT NULL UNIQUE,
                                data_abertura TIMESTAMP NOT NULL,
                                data_fechamento TIMESTAMP NOT NULL,
                                FOREIGN KEY (pauta_id) REFERENCES pauta(id)
);

CREATE TABLE voto (
                      id UUID PRIMARY KEY,
                      pauta_id UUID NOT NULL,
                      associado_id VARCHAR(11) NOT NULL,
                      escolha VARCHAR(3) NOT NULL CHECK (escolha IN ('SIM', 'NAO')),
                      data_voto TIMESTAMP NOT NULL,
                      FOREIGN KEY (pauta_id) REFERENCES pauta(id),
                      CONSTRAINT uk_voto_associado_pauta UNIQUE (pauta_id, associado_id)
);