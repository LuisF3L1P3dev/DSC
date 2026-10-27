package br.edu.ifrn.service;

import org.springframework.stereotype.Service;

@Service
public class EntregaService {

    public double calcularTaxa(String localEntrega) {
        if ("RN".equalsIgnoreCase(localEntrega)) {
            return 10.0;
        }
        return 20.0;
    }
}
