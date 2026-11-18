package app;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Bets {

    // Resultados possíveis de um jogo
    public static final int CASA = 0;
    public static final int EMPATE = 1;
    public static final int FORA = 2;

    // Seções (campeonatos). A ordem aqui é a mesma de TIMES e FORCA.
    private static final String[] CAMPEONATOS = {
            "Brasileirão Série A",
            "Brasileirão Série B",
            "La Liga",
            "Premier League",
            "Bundesliga",
            "O Maior do Mundo"
    };

    private static final String[][] TIMES = {
            // Brasileirão Série A
            {"Flamengo", "Palmeiras", "Botafogo", "Cruzeiro", "Fluminense",
                    "Internacional", "São Paulo", "Atlético-MG", "Corinthians", "Bahia",
                    "Grêmio", "Vasco", "Red Bull Bragantino", "Mirassol", "Santos",
                    "Athletico-PR", "Coritiba", "Vitória", "Chapecoense", "Remo"},
            // Brasileirão Série B
            {"Fortaleza", "Ceará", "Sport", "Juventude", "Goiás",
                    "Cuiabá", "Novorizontino", "Criciúma", "Atlético-GO", "América-MG",
                    "Avaí", "CRB", "Vila Nova", "Operário-PR", "Ponte Preta",
                    "Náutico", "Londrina", "Botafogo-SP", "Athletic", "São Bernardo"},
            // La Liga
            {"Real Madrid", "Barcelona", "Atlético de Madrid", "Athletic Club", "Villarreal",
                    "Real Betis", "Real Sociedad", "Celta de Vigo", "Sevilla", "Girona",
                    "Osasuna", "Valencia", "Rayo Vallecano", "Getafe", "Mallorca",
                    "Espanyol", "Alavés", "Elche", "Levante", "Real Oviedo"},
            // Premier League
            {"Liverpool", "Manchester City", "Arsenal", "Chelsea", "Newcastle",
                    "Tottenham", "Aston Villa", "Manchester United", "Brighton", "Nottingham Forest",
                    "Crystal Palace", "Bournemouth", "Brentford", "Fulham", "Everton",
                    "West Ham", "Wolverhampton", "Leeds United", "Sunderland", "Burnley"},
            // Bundesliga
            {"Bayern de Munique", "Bayer Leverkusen", "Borussia Dortmund", "RB Leipzig", "Eintracht Frankfurt",
                    "Stuttgart", "Freiburg", "Mainz", "Hoffenheim", "Werder Bremen",
                    "Borussia M'gladbach", "Wolfsburg", "Union Berlin", "Augsburg", "Köln",
                    "Hamburgo", "St. Pauli", "Heidenheim"},
            // O Maior do Mundo
            {"Sampaio Corrêa"}
    };

    // Força de cada time (mesma posição de TIMES). Quanto maior, mais chance de ganhar.
    private static final int[][] FORCA = {
            // Série A
            {84, 84, 79, 79, 78, 77, 77, 77, 76, 76, 75, 75, 74, 74, 74, 72, 71, 71, 69, 69},
            // Série B
            {70, 69, 68, 67, 66, 66, 66, 65, 65, 65, 63, 63, 63, 62, 62, 61, 60, 60, 59, 59},
            // La Liga
            {92, 92, 88, 83, 83, 82, 81, 79, 79, 78, 78, 78, 77, 77, 77, 77, 76, 75, 74, 73},
            // Premier League
            {92, 92, 92, 89, 86, 85, 85, 84, 83, 82, 82, 81, 81, 80, 80, 80, 78, 78, 77, 76},
            // Bundesliga
            {93, 86, 86, 84, 83, 82, 80, 79, 79, 78, 78, 78, 77, 77, 76, 76, 75, 74},
            // O Maior do Mundo (obviamente)
            {100}
    };

    private static final double CHANCE_EMPATE = 0.27;
    private static final double VANTAGEM_CASA = 1.25; // jogar em casa ajuda
    private static final double MARGEM_CASA_DE_APOSTA = 1.07;

    // Nome do time -> força (para achar a força rápido pelo nome)
    private static final Map<String, Integer> FORCA_POR_TIME = new HashMap<>();

    static {
        for (int c = 0; c < TIMES.length; c++) {
            for (int t = 0; t < TIMES[c].length; t++) {
                FORCA_POR_TIME.put(TIMES[c][t], FORCA[c][t]);
            }
        }
    }

    private Random gerador = new Random();

    private String ultimoPlacar = "";
    private int golsCasa = 0;
    private int golsFora = 0;
    private double totalApostado = 0;
    private double totalGanho = 0;

    public static String[] getCampeonatos() {
        return CAMPEONATOS;
    }

    public static String[] getTimes(String campeonato) {
        for (int i = 0; i < CAMPEONATOS.length; i++) {
            if (CAMPEONATOS[i].equals(campeonato)) {
                return TIMES[i];
            }
        }
        return TIMES[0];
    }

    // Chance real de cada resultado, calculada pela força dos times.
    // A força é elevada a 4 para que a diferença entre times fortes e fracos pese bastante.
    private double[] calcularChances(String casa, String fora) {
        double pesoCasa = Math.pow(FORCA_POR_TIME.get(casa), 4) * VANTAGEM_CASA;
        double pesoFora = Math.pow(FORCA_POR_TIME.get(fora), 4);
        double chanceCasa = (1 - CHANCE_EMPATE) * pesoCasa / (pesoCasa + pesoFora);
        double chanceFora = (1 - CHANCE_EMPATE) - chanceCasa;
        return new double[]{chanceCasa, CHANCE_EMPATE, chanceFora};
    }

    // Odd = 1 / chance, com uma margem para a casa de apostas (arredondada em 2 casas)
    public double getOdd(String casa, String fora, int escolha) {
        double chance = calcularChances(casa, fora)[escolha];
        double odd = 1.0 / (chance * MARGEM_CASA_DE_APOSTA);
        return Math.round(odd * 100) / 100.0;
    }

    // Sorteia o resultado usando a chance real de cada um
    public int sortearResultado(String casa, String fora) {
        double[] chance = calcularChances(casa, fora);
        double sorteio = gerador.nextDouble();

        int resultado = FORA;
        if (sorteio < chance[CASA]) {
            resultado = CASA;
        } else if (sorteio < chance[CASA] + chance[EMPATE]) {
            resultado = EMPATE;
        }

        ultimoPlacar = gerarPlacar(casa, fora, resultado);
        return resultado;
    }

    // Cria um placar que combina com o resultado sorteado
    private String gerarPlacar(String casa, String fora, int resultado) {
        if (resultado == EMPATE) {
            golsCasa = gerador.nextInt(4);
            golsFora = golsCasa;
        } else {
            int vencedor = 1 + gerador.nextInt(4);   // 1 a 4 gols
            int perdedor = gerador.nextInt(vencedor); // sempre menos que o vencedor
            golsCasa = (resultado == CASA) ? vencedor : perdedor;
            golsFora = (resultado == CASA) ? perdedor : vencedor;
        }
        return casa + " " + golsCasa + " x " + golsFora + " " + fora;
    }

    public String getUltimoPlacar() {
        return ultimoPlacar;
    }

    public int getGolsCasa() {
        return golsCasa;
    }

    public int getGolsFora() {
        return golsFora;
    }

    // Registra a aposta e devolve quanto o usuário ganhou (0 se perdeu)
    public double apostar(String casa, String fora, int escolha, double valor) {
        totalApostado += valor;
        int resultado = sortearResultado(casa, fora);
        if (resultado == escolha) {
            double premio = valor * getOdd(casa, fora, escolha);
            totalGanho += premio;
            return premio;
        }
        return 0;
    }

    // Lucro (ou prejuízo, se negativo) de todas as apostas
    public double getLucro() {
        return totalGanho - totalApostado;
    }
}
