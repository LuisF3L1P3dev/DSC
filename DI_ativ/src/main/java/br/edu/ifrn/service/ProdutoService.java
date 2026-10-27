package br.edu.ifrn.service;

import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final EntregaService entregaService;
    private final ImpostoService impostoService;

    public ProdutoService(EntregaService entregaService, ImpostoService impostoService) {
        this.entregaService = entregaService;
        this.impostoService = impostoService;
    }

    public double calcularPrecoFinal(double custo, String localEntrega) {
        double taxaEntrega = entregaService.calcularTaxa(localEntrega);
        double imposto = impostoService.calcularImpostos(custo);

        return custo + imposto + taxaEntrega;
    }
}
