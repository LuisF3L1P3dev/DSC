package br.edu.ifrn;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
    public static void main(String[] args) {
        System.out.println("Hello World");

        Produto p1 = new Produto("Notebook", 3500.00, 10);

        System.out.println(p1);

        p1.vender(3);

        System.out.println(p1);
    }
}