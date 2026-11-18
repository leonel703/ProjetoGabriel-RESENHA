package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Aviator {

    // Quão rápido o multiplicador cresce com o tempo.
    // Com 0.15: 2x em ~4,6 s, 5x em ~10,7 s e 10x em ~15,4 s
    public static final double VELOCIDADE = 0.15;

    private static final double EXPLOSAO_MAXIMA = 100.0;
    private static final int RESULTADOS_GUARDADOS = 8;

    private Random gerador = new Random();

    private double tempo = 0;            // segundos desde a decolagem
    private double multiplicador = 1.0;
    private double pontoExplosao = 1.0;  // sorteado na decolagem, o jogador não sabe
    private double valorApostado = 0;
    private double multiplicadorRetirada = 0;

    private boolean rodadaAtiva = false; // avião voando
    private boolean apostaAtiva = false; // dinheiro do jogador ainda no avião
    private boolean explodiu = false;

    // Retirada automática: 0 = desligada. Se ligada, retira sozinho ao chegar nesse multiplicador.
    private double retiradaAutomatica = 0;
    private double premioAutomaticoPendente = 0; // prêmio que a tela ainda não colocou no saldo

    // Onde os últimos aviões sumiram (o mais novo primeiro)
    private List<Double> ultimosResultados = new ArrayList<>();

    private double totalApostado = 0;
    private double totalGanho = 0;

    // O multiplicador depende só do tempo de voo: cresce cada vez mais rápido
    public static double multiplicadorNoTempo(double segundos) {
        return Math.exp(VELOCIDADE * segundos);
    }

    // Começa uma rodada nova e sorteia em que multiplicador o avião vai sumir
    public void iniciar(double valor) {
        valorApostado = valor;
        totalApostado += valor;
        tempo = 0;
        multiplicador = 1.0;
        multiplicadorRetirada = 0;
        pontoExplosao = sortearPontoExplosao();
        rodadaAtiva = true;
        apostaAtiva = true;
        explodiu = false;
        retiradaAutomatica = 0;
        premioAutomaticoPendente = 0;
    }

    // Liga a retirada automática para essa rodada (0 desliga)
    public void setRetiradaAutomatica(double alvo) {
        retiradaAutomatica = alvo;
    }

    // Valores baixos são comuns e valores altos são raros:
    // ~50% de chance de passar de 2x, ~10% de passar de 10x
    private double sortearPontoExplosao() {
        double sorteio = gerador.nextDouble();
        double ponto = 0.97 / (1 - sorteio);
        ponto = Math.max(1.0, Math.min(ponto, EXPLOSAO_MAXIMA));
        return Math.floor(ponto * 100) / 100.0;
    }

    // Avança o voo em "segundos" e recalcula o multiplicador
    public void avancar(double segundos) {
        if (!rodadaAtiva) {
            return;
        }
        tempo += segundos;
        multiplicador = multiplicadorNoTempo(tempo);

        // Chegou no alvo da retirada automática antes do avião sumir: retira exatamente no alvo
        if (apostaAtiva && retiradaAutomatica > 1 && multiplicador >= retiradaAutomatica
                && retiradaAutomatica < pontoExplosao) {
            premioAutomaticoPendente = retirarEm(retiradaAutomatica);
        }

        if (multiplicador >= pontoExplosao) {
            multiplicador = pontoExplosao;
            tempo = Math.log(pontoExplosao) / VELOCIDADE;
            rodadaAtiva = false;
            apostaAtiva = false; // se ainda não tinha retirado, perdeu
            explodiu = true;

            ultimosResultados.add(0, pontoExplosao);
            if (ultimosResultados.size() > RESULTADOS_GUARDADOS) {
                ultimosResultados.remove(ultimosResultados.size() - 1);
            }
        }
    }

    // O jogador retira o dinheiro no multiplicador atual (o avião continua voando)
    public double retirar() {
        return retirarEm(multiplicador);
    }

    private double retirarEm(double mult) {
        if (!apostaAtiva) {
            return 0;
        }
        apostaAtiva = false;
        multiplicadorRetirada = mult;
        double premio = valorApostado * mult;
        totalGanho += premio;
        return premio;
    }

    // Devolve o prêmio da retirada automática (só uma vez) para a tela colocar no saldo
    public double pegarPremioAutomatico() {
        double premio = premioAutomaticoPendente;
        premioAutomaticoPendente = 0;
        return premio;
    }

    public boolean isRodadaAtiva() {
        return rodadaAtiva;
    }

    public boolean isApostaAtiva() {
        return apostaAtiva;
    }

    public boolean isExplodiu() {
        return explodiu;
    }

    // true se o jogador retirou nessa rodada
    public boolean isRetirou() {
        return multiplicadorRetirada > 0;
    }

    public double getTempo() {
        return tempo;
    }

    public double getMultiplicador() {
        return multiplicador;
    }

    public double getMultiplicadorRetirada() {
        return multiplicadorRetirada;
    }

    public double getValorApostado() {
        return valorApostado;
    }

    public List<Double> getUltimosResultados() {
        return ultimosResultados;
    }

    public double getLucro() {
        return totalGanho - totalApostado;
    }
}
