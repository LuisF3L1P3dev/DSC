package br.edu.ifrn.service;

import org.springframework.stereotype.Service;

@Service
public class ImpostoService {

    public double calcularImpostos(double custo) {
        return custo * 0.10;
    }
}
