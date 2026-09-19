package br.edu.ifrn.biblioteca.web.error;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> tratarViolacaoDeParametro(ConstraintViolationException exception) {
        String mensagem = exception.getConstraintViolations().stream()
                .map(violacao -> violacao.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mensagem);
        problema.setTitle("Parâmetro inválido");

        return ResponseEntity.badRequest().body(problema);
    }
}
