package br.com.yuri.votacao.controller;

import br.com.yuri.votacao.dto.CpfValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class FakeCpfValidatorController {

    @GetMapping("/users/{cpf}")
    public ResponseEntity<CpfValidationResponse> validarCpf(@PathVariable String cpf) {
        int sorteio = ThreadLocalRandom.current().nextInt(3);

        return switch (sorteio) {
            case 0 -> ResponseEntity.ok(new CpfValidationResponse("ABLE_TO_VOTE"));
            case 1 -> ResponseEntity.ok(new CpfValidationResponse("UNABLE_TO_VOTE"));
            default -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        };
    }
}