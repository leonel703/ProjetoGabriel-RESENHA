package app;

import java.util.Random;

// Frases do narrador da Bet Surpresa
public class Narrador {

    private static final String[] FRASES_GOL = {
            "GOOOOOOL! É do %s! A bola estufa a rede!",
            "GOL! GOL! GOL! O %s marca e a torcida explode!",
            "É GOL DO %s! Que golaço de fora da área!",
            "GOOOL! Cabeçada perfeita, gol do %s!",
            "Pênalti para o %s... bateu... É GOL!",
            "GOL DO %s! Contra-ataque fulminante!",
            "Olha o que ele fez! Driblou o goleiro e é GOL do %s!"
    };

    private static final String[] FRASES_LANCE = {
            "Chute perigoso do %s... passa raspando a trave!",
            "Que defesa! O goleiro segura o chute do %s!",
            "Cartão amarelo para um jogador do %s.",
            "Escanteio para o %s.",
            "NA TRAVE! O %s quase marca!",
            "Impedimento marcado contra o %s.",
            "O %s troca passes e tenta envolver a defesa.",
            "Falta perigosa a favor do %s na entrada da área.",
            "O %s pressiona, a torcida canta alto!",
            "Cruzamento do %s, a zaga afasta de cabeça."
    };

    private Random gerador = new Random();

    public String gol(String time) {
        return String.format(FRASES_GOL[gerador.nextInt(FRASES_GOL.length)], time);
    }

    public String lance(String time) {
        return String.format(FRASES_LANCE[gerador.nextInt(FRASES_LANCE.length)], time);
    }
}
