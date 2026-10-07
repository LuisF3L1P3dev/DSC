package br.edu.ifrn;


class Produto{
    private String nome;
    private double preco;
    private int quant;

    public Produto(String nome, double preco, int quant){
        this.nome = nome;
        this.preco = preco;
        this.quant = quant;

    }

    public String getNome(){
        return nome;
    }

    public double getPreco(){
        return preco;
    }

    public int getQuant(){
        return quant;
    }

    public boolean vender(int quant ){
        if(quant > 0 || quant <= this.quant){
            this.quant -= quant;
            return true;
        }
        return false;
    }

    public String toString(){
        return String.format("Produto: %s | Preço: R$ %.2f | Estoque: %d un", nome, preco, quant);
    }

}

