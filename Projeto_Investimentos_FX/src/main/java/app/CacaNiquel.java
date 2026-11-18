package app;

import java.util.Random;

public class CacaNiquel {

    // Símbolos dos rolos, do mais comum para o mais raro
    private static final String[] SIMBOLOS = {"♣", "♥", "♦", "★", "BAR", "7"};

    // Peso de cada símbolo no sorteio (quanto maior, mais aparece)
    private static final int[] PESOS = {6, 5, 4, 3, 2, 1};

    // Quantas vezes o valor apostado o jogador recebe ao tirar 3 iguais
    private static final int[] PREMIOS = {8, 15, 25, 60, 150, 500};

    public static final int ROLOS = 3;

    // Chance dos dois primeiros rolos darem iguais
    private static final double CHANCE_DOIS_PRIMEIROS_IGUAIS = 0.25;

    private Random gerador = new Random();

    private int[] ultimoResultado = new int[ROLOS];
    private double ultimoPremio = 0;
    private double totalApostado = 0;
    private double totalGanho = 0;

    public static int getQuantidadeSimbolos() {
        return SIMBOLOS.length;
    }

    public static String getSimbolo(int i) {
        return SIMBOLOS[i];
    }

    public static int getPremio(int i) {
        return PREMIOS[i];
    }

    // Sorteia um símbolo respeitando os pesos
    private int sortearSimbolo() {
        return sortearComPesos(PESOS);
    }

    // Sorteia o símbolo do par dos dois primeiros rolos.
    // Usa o peso ao quadrado para os símbolos raros (7, BAR) continuarem raros no par.
    private int sortearSimboloDoPar() {
        int[] pesosDoPar = new int[PESOS.length];
        for (int i = 0; i < PESOS.length; i++) {
            pesosDoPar[i] = PESOS[i] * PESOS[i];
        }
        return sortearComPesos(pesosDoPar);
    }

    private int sortearComPesos(int[] pesos) {
        int soma = 0;
        for (int peso : pesos) {
            soma += peso;
        }
        int sorteio = gerador.nextInt(soma);
        for (int i = 0; i < pesos.length; i++) {
            if (sorteio < pesos[i]) {
                return i;
            }
            sorteio -= pesos[i];
        }
        return 0;
    }

    // Símbolo qualquer, só para a animação dos rolos girando
    public int simboloAleatorio() {
        return gerador.nextInt(SIMBOLOS.length);
    }

    // Gira os 3 rolos. Devolve o prêmio (0 se não deu 3 iguais).
    public double girar(double valor) {
        totalApostado += valor;
        if (gerador.nextDouble() < CHANCE_DOIS_PRIMEIROS_IGUAIS) {
            // 25%: os dois primeiros saem iguais
            int simbolo = sortearSimboloDoPar();
            ultimoResultado[0] = simbolo;
            ultimoResultado[1] = simbolo;
        } else {
            // 75%: os dois primeiros saem diferentes
            ultimoResultado[0] = sortearSimbolo();
            do {
                ultimoResultado[1] = sortearSimbolo();
            } while (ultimoResultado[1] == ultimoResultado[0]);
        }
        // O terceiro é sorteado normalmente
        ultimoResultado[2] = sortearSimbolo();

        ultimoPremio = 0;
        if (ultimoResultado[0] == ultimoResultado[1] && ultimoResultado[1] == ultimoResultado[2]) {
            ultimoPremio = valor * PREMIOS[ultimoResultado[0]];
            totalGanho += ultimoPremio;
        }
        return ultimoPremio;
    }

    public int getResultado(int rolo) {
        return ultimoResultado[rolo];
    }

    public double getUltimoPremio() {
        return ultimoPremio;
    }

    public double getLucro() {
        return totalGanho - totalApostado;
    }
}
