package br.com.yuri.votacao.exception;

public class SessaoFechadaException extends RuntimeException {

    public SessaoFechadaException(String message) {
        super(message);
    }
}

