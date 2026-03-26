package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.CpfValidationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeCpfValidatorControllerTest {

    @Test
    void deveRetornarApenasContratosPermitidos() {
        FakeCpfValidatorController controller = new FakeCpfValidatorController();

        for (int i = 0; i < 25; i++) {
            ResponseEntity<CpfValidationResponse> response = controller.validarCpf("12345678901");

            if (response.getStatusCode() == HttpStatus.OK) {
                CpfValidationResponse body = response.getBody();
                assertNotNull(body);
                assertTrue(
                        "ABLE_TO_VOTE".equals(body.status()) || "UNABLE_TO_VOTE".equals(body.status()),
                        "Quando 200, o status deve ser ABLE_TO_VOTE ou UNABLE_TO_VOTE"
                );
            } else {
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            }
        }
    }
}

