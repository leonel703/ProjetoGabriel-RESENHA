package app;

import com.google.gson.Gson;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class App extends Application {

    // Todas as telas usam o mesmo tamanho (o da tela de login)
    private static final double LARGURA = 420;
    private static final double ALTURA = 320;

    @Override
    public void start(Stage primaryStage) {
        Label lblTitulo = new Label("Login - Investimentos");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Usuário");
        txtUsuario.setMaxWidth(260);

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Senha");
        txtSenha.setMaxWidth(260);

        Button btnEntrar = new Button("Entrar");
        btnEntrar.setPrefWidth(120);

        Label lblMensagem = new Label();

        VBox root = new VBox(12, lblTitulo, txtUsuario, txtSenha, btnEntrar, lblMensagem);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        final Scene cenaLogin = new Scene(root, LARGURA, ALTURA);

        Validacaologin validacao = new Validacaologin();

        btnEntrar.setOnAction(event -> {
            // Ignora os espaços: "Leonel Soares" vira "LeonelSoares"
            String usuarioDigitado = txtUsuario.getText().replace(" ", "");
            String senhaDigitada = txtSenha.getText();

            if (usuarioDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                lblMensagem.setText("Preencha usuário e senha.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
                return;
            }

            Path caminhoJson = Path.of("DadosBancarios.json");
            if (!Files.exists(caminhoJson)) {
                caminhoJson = Path.of("../DadosBancarios.json");
            }
            if (!Files.exists(caminhoJson)) {
                caminhoJson = Path.of("Projeto_Investimentos_FX", "DadosBancarios.json");
            }

            try (Reader reader = Files.newBufferedReader(caminhoJson, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                CarregarDados dadosCarregados = gson.fromJson(reader, CarregarDados.class);

                UsuarioJson usuarioEncontrado = null;
                if (dadosCarregados != null && dadosCarregados.getUsuarios() != null) {
                    for (UsuarioJson u : dadosCarregados.getUsuarios()) {
                        if (usuarioDigitado.equals(u.getUsuario())) {
                            usuarioEncontrado = u;
                            break;
                        }
                    }
                }

                if (usuarioEncontrado == null) {
                    lblMensagem.setText("Usuário ou senha incorretos.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                    return;
                }

                Login contaLogin = new Login(usuarioEncontrado.getUsuario(), usuarioEncontrado.getSenha());
                boolean autenticado = validacao.Entrar(contaLogin, usuarioDigitado, senhaDigitada);

                if (autenticado) {
                    txtSenha.clear();
                    lblMensagem.setText("");

                    double saldoInicial = Double.parseDouble(String.valueOf(usuarioEncontrado.getSaldo()));
                    Pagamentos pagamentos = new Pagamentos(saldoInicial);
                    // Cada login tem sua própria carteira de investimentos
                    Investimentos investimentos = new Investimentos();
                    // E também seu próprio histórico de apostas
                    Bets bets = new Bets();
                    Aviator aviator = new Aviator();
                    CacaNiquel cacaNiquel = new CacaNiquel();

                    Scene cenaPrincipal = criarCenaPrincipal(
                            primaryStage,
                            usuarioEncontrado,
                            cenaLogin,
                            investimentos,
                            pagamentos,
                            bets,
                            aviator,
                            cacaNiquel
                    );
                    primaryStage.setTitle("Painel - Projeto Investimentos FX");
                    primaryStage.setScene(cenaPrincipal);
                } else {
                    lblMensagem.setText("Usuário ou senha incorretos.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                }
            } catch (Exception e) {
                lblMensagem.setText("Erro ao ler DadosBancarios.json: " + e.getMessage());
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            }
        });

        primaryStage.setTitle("Login - Projeto Investimentos FX");
        primaryStage.setScene(cenaLogin);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene criarCenaPrincipal(
            Stage stage,
            UsuarioJson usuario,
            Scene cenaLogin,
            Investimentos investimentos,
            Pagamentos pagamentos,
            Bets bets,
            Aviator aviator,
            CacaNiquel cacaNiquel
    ) {
        Label lblBoasVindas = new Label("Boas Vindas - " + usuario.getNome());
        lblBoasVindas.setStyle("-fx-font-size: 15px;");

        Label lblMenu = new Label("Menu Inicial");
        lblMenu.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnCarteira = new Button("Carteira");
        btnCarteira.setPrefWidth(180);

        Button btnInvestimentos = new Button("Investimentos");
        btnInvestimentos.setPrefWidth(180);

        Button btnPagamentos = new Button("Pagamentos");
        btnPagamentos.setPrefWidth(180);

        Button btnCassino = new Button("Cassino");
        btnCassino.setPrefWidth(180);

        Button btnSair = new Button("Sair");
        btnSair.setPrefWidth(180);

        Label lblStatusAcao = new Label();
        lblStatusAcao.setWrapText(true);

        VBox painel = new VBox(12, lblMenu, lblBoasVindas, btnCarteira, btnInvestimentos, btnPagamentos, btnCassino, btnSair, lblStatusAcao);
        painel.setAlignment(Pos.CENTER);
        painel.setPadding(new Insets(24));

        final Scene cenaPrincipal = new Scene(painel, LARGURA, ALTURA);

        btnCarteira.setOnAction(e -> {
            Scene cenaCarteira = criarCenaCarteira(stage, investimentos, pagamentos, cenaPrincipal);
            stage.setTitle("Carteira - Projeto Investimentos FX");
            stage.setScene(cenaCarteira);
        });

        btnInvestimentos.setOnAction(e -> {
            Scene cenaInvestimentos = criarCenaInvestimentos(
                    stage,
                    usuario,
                    investimentos,
                    pagamentos,
                    cenaPrincipal,
                    lblStatusAcao
            );
            stage.setTitle("Investimentos - Projeto Investimentos FX");
            stage.setScene(cenaInvestimentos);
        });

        btnPagamentos.setOnAction(e -> {
            Scene cenaPagamentos = criarCenaPagamentos(stage, usuario, pagamentos, cenaPrincipal);
            stage.setTitle("Pagamentos - Projeto Investimentos FX");
            stage.setScene(cenaPagamentos);
        });

        // Só pergunta a idade uma vez por login
        boolean[] maiorDeIdade = {false};

        Runnable abrirCassino = () -> {
            Scene cenaCassino = criarCenaCassino(stage, pagamentos, bets, aviator, cacaNiquel, cenaPrincipal);
            stage.setTitle("Cassino - Projeto Investimentos FX");
            stage.setScene(cenaCassino);
        };

        btnCassino.setOnAction(e -> {
            if (maiorDeIdade[0]) {
                abrirCassino.run();
            } else {
                Scene cenaIdade = criarCenaIdade(stage, cenaPrincipal, () -> {
                    maiorDeIdade[0] = true;
                    abrirCassino.run();
                });
                stage.setTitle("Cassino - Projeto Investimentos FX");
                stage.setScene(cenaIdade);
            }
        });

        btnSair.setOnAction(e -> {
            stage.setTitle("Login - Projeto Investimentos FX");
            stage.setScene(cenaLogin);
        });

        return cenaPrincipal;
    }

    private Scene criarCenaPagamentos(
            Stage stage,
            UsuarioJson usuario,
            Pagamentos pagamentos,
            Scene cenaPrincipal
    ) {
        Label lblTitulo = new Label("Pagamentos");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Painel - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        Label lblSaldo = new Label(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
        lblSaldo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Parte de depósito
        Label lblDepositar = new Label("Depositar");
        lblDepositar.setStyle("-fx-font-weight: bold;");
        TextField txtDeposito = new TextField();
        txtDeposito.setPromptText("Valor (R$)");
        txtDeposito.setPrefWidth(110);
        Button btnDepositar = new Button("Depositar");
        HBox linhaDeposito = new HBox(8, txtDeposito, btnDepositar);

        // Parte de retirada
        Label lblRetirar = new Label("Retirar");
        lblRetirar.setStyle("-fx-font-weight: bold;");
        TextField txtRetirada = new TextField();
        txtRetirada.setPromptText("Valor (R$)");
        txtRetirada.setPrefWidth(110);
        Button btnRetirar = new Button("Retirar");
        HBox linhaRetirada = new HBox(8, txtRetirada, btnRetirar);

        Label lblMensagem = new Label();

        btnDepositar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtDeposito.getText().trim().replace(",", "."));
                if (pagamentos.Deposito(valor, usuario.getNome())) {
                    lblMensagem.setText(String.format("Depósito de R$ %.2f feito.", valor));
                    txtDeposito.clear();
                } else {
                    lblMensagem.setText("Digite um valor maior que zero.");
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
            }
            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
        });

        btnRetirar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtRetirada.getText().trim().replace(",", "."));
                if (pagamentos.Saque(valor)) {
                    lblMensagem.setText(String.format("Retirada de R$ %.2f feita.", valor));
                    txtRetirada.clear();
                } else {
                    lblMensagem.setText("Valor inválido ou maior que o saldo.");
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
            }
            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
        });

        VBox layout = new VBox(8, topo, lblSaldo, lblDepositar, linhaDeposito, lblRetirar, linhaRetirada, lblMensagem);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    private Scene criarCenaCarteira(
            Stage stage,
            Investimentos investimentos,
            Pagamentos pagamentos,
            Scene cenaPrincipal
    ) {
        Label lblTitulo = new Label("Carteira");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Painel - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        double saldo = pagamentos.Conta();
        double investido = investimentos.getTotalInvestido();

        Label lblSaldo = new Label(String.format("Saldo: R$ %.2f", saldo));
        Label lblInvestido = new Label(String.format("Investido: R$ %.2f", investido));
        Label lblTotal = new Label(String.format("Total: R$ %.2f", saldo + investido));
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblAtivos = new Label("Seus investimentos:");
        lblAtivos.setStyle("-fx-font-weight: bold;");

        VBox lista = new VBox(4);
        String[] opcoes = Investimentos.getOpcoes();
        for (int i = 0; i < opcoes.length; i++) {
            double valor = investimentos.getValorInvestido(i);
            if (valor > 0) {
                lista.getChildren().add(new Label(String.format("%s: R$ %.2f", opcoes[i], valor)));
            }
        }
        if (lista.getChildren().isEmpty()) {
            lista.getChildren().add(new Label("Você ainda não tem investimentos."));
        }

        VBox layout = new VBox(8, topo, lblSaldo, lblInvestido, lblTotal, lblAtivos, lista);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    private Scene criarCenaInvestimentos(
            Stage stage,
            UsuarioJson usuario,
            Investimentos investimentos,
            Pagamentos pagamentos,
            Scene cenaPrincipal,
            Label lblStatusAcao
    ) {
        Label lblTitulo = new Label("Investimentos");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblSaldo = new Label();

        Button btnVoltar = new Button("Voltar");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, lblSaldo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cbAtivo = new ComboBox<>();
        cbAtivo.getItems().addAll(Investimentos.getOpcoes());
        cbAtivo.setValue("Dollar");

        Label lblPreco = new Label();
        lblPreco.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox linhaAtivo = new HBox(10, cbAtivo, lblPreco);
        linhaAtivo.setAlignment(Pos.CENTER_LEFT);

        // O gráfico começa sem barras toda vez que a tela abre
        investimentos.limparHistorico();

        // Gráfico de barras com os últimos 25 preços
        CategoryAxis eixoX = new CategoryAxis();
        eixoX.setTickLabelsVisible(false);
        eixoX.setTickMarkVisible(false);
        NumberAxis eixoY = new NumberAxis();
        eixoY.setForceZeroInRange(false);
        eixoY.setMinorTickVisible(false);
        eixoY.setTickMarkVisible(false);
        eixoY.setTickLabelsVisible(false);
        BarChart<String, Number> grafico = new BarChart<>(eixoX, eixoY);
        grafico.setAnimated(false);
        grafico.setLegendVisible(false);
        // Sem linhas de grade, só as barras
        grafico.setHorizontalGridLinesVisible(false);
        grafico.setVerticalGridLinesVisible(false);
        grafico.setAlternativeRowFillVisible(false);
        grafico.setBarGap(0);
        grafico.setCategoryGap(7); // espaço entre as barras (menor = barras mais grossas)
        grafico.setPrefHeight(150);
        grafico.setMinHeight(0);
        VBox.setVgrow(grafico, Priority.ALWAYS);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        grafico.getData().add(serie);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Valor (R$)");
        txtValor.setPrefWidth(110);

        Button btnInvestir = new Button("Investir");
        Button btnRetirar = new Button("Retirar");
        HBox linhaBotoes = new HBox(8, txtValor, btnInvestir, btnRetirar);
        linhaBotoes.setAlignment(Pos.CENTER_LEFT);

        Label lblInvestido = new Label();
        Label lblMensagem = new Label();

        // Atualiza os textos e o gráfico
        Runnable atualizarTela = () -> {
            int i = Investimentos.indiceDe(cbAtivo.getValue());

            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
            lblPreco.setText(String.format("R$ %.2f", investimentos.getPreco(i)));
            lblInvestido.setText(String.format("Investido em %s: R$ %.2f", cbAtivo.getValue(), investimentos.getValorInvestido(i)));

            double[] historico = investimentos.getHistorico(i);
            serie.getData().clear();
            // Sempre 25 barras; as que ainda não têm preço ficam invisíveis
            for (int j = 0; j < Investimentos.PONTOS; j++) {
                boolean temPreco = j < historico.length;
                double valor = temPreco ? historico[j] : investimentos.getPreco(i);
                XYChart.Data<String, Number> barra = new XYChart.Data<>(String.valueOf(j + 1), valor);
                serie.getData().add(barra);
                barra.getNode().setStyle("-fx-bar-fill: gray;");
                if (!temPreco) {
                    barra.getNode().setVisible(false);
                }
            }
        };

        // A cada 1 segundo os preços mudam
        Timeline relogio = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            investimentos.atualizarPrecos();
            atualizarTela.run();
        }));
        relogio.setCycleCount(Timeline.INDEFINITE);
        relogio.play();

        cbAtivo.setOnAction(e -> atualizarTela.run());

        btnInvestir.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    lblMensagem.setText("Digite um valor maior que zero.");
                } else if (valor > pagamentos.Conta()) {
                    lblMensagem.setText("Saldo insuficiente.");
                } else {
                    int i = Investimentos.indiceDe(cbAtivo.getValue());
                    pagamentos.SaqueInv(valor);
                    investimentos.investir(i, valor);
                    txtValor.clear();
                    lblMensagem.setText(String.format("Investido R$ %.2f em %s.", valor, cbAtivo.getValue()));
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
            }
            atualizarTela.run();
        });

        // O dinheiro só volta para o saldo quando clica em Retirar
        btnRetirar.setOnAction(e -> {
            int i = Investimentos.indiceDe(cbAtivo.getValue());
            double valor = investimentos.retirar(i);
            if (valor > 0) {
                pagamentos.DepositoInv(valor);
                lblMensagem.setText(String.format("Retirado R$ %.2f de %s.", valor, cbAtivo.getValue()));
            } else {
                lblMensagem.setText("Você não tem nada investido em " + cbAtivo.getValue() + ".");
            }
            atualizarTela.run();
        });

        btnVoltar.setOnAction(e -> {
            relogio.stop();
            lblStatusAcao.setText(
                    "Saldo: R$ " + String.format("%.2f", pagamentos.Conta())
                            + " | Investido: R$ " + String.format("%.2f", investimentos.getTotalInvestido())
            );
            stage.setTitle("Painel - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        atualizarTela.run();

        VBox layout = new VBox(6, topo, linhaAtivo, grafico, linhaBotoes, lblInvestido, lblMensagem);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    private Scene criarCenaBets(
            Stage stage,
            Bets bets,
            Pagamentos pagamentos,
            Scene cenaPrincipal
    ) {
        Label lblTitulo = new Label("Bets");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblSaldo = new Label();

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Cassino - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, lblSaldo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        // Cada lado escolhe primeiro o campeonato (seção) e depois o time.
        // Assim dá para montar jogos entre campeonatos diferentes.
        String campeonatoInicial = Bets.getCampeonatos()[0];

        ComboBox<String> cbLigaCasa = new ComboBox<>();
        cbLigaCasa.getItems().addAll(Bets.getCampeonatos());
        cbLigaCasa.setValue(campeonatoInicial);
        cbLigaCasa.setPrefWidth(165);

        ComboBox<String> cbCasa = new ComboBox<>();
        cbCasa.getItems().addAll(Bets.getTimes(campeonatoInicial));
        cbCasa.setValue(Bets.getTimes(campeonatoInicial)[0]);
        cbCasa.setPrefWidth(180);
        cbCasa.setVisibleRowCount(10);

        ComboBox<String> cbLigaFora = new ComboBox<>();
        cbLigaFora.getItems().addAll(Bets.getCampeonatos());
        cbLigaFora.setValue(campeonatoInicial);
        cbLigaFora.setPrefWidth(165);

        ComboBox<String> cbFora = new ComboBox<>();
        cbFora.getItems().addAll(Bets.getTimes(campeonatoInicial));
        cbFora.setValue(Bets.getTimes(campeonatoInicial)[1]);
        cbFora.setPrefWidth(180);
        cbFora.setVisibleRowCount(10);

        Label lblCasa = new Label("Casa:");
        lblCasa.setPrefWidth(35);
        lblCasa.setStyle("-fx-font-weight: bold;");
        HBox linhaCasa = new HBox(6, lblCasa, cbLigaCasa, cbCasa);
        linhaCasa.setAlignment(Pos.CENTER_LEFT);

        Label lblFora = new Label("Fora:");
        lblFora.setPrefWidth(35);
        lblFora.setStyle("-fx-font-weight: bold;");
        HBox linhaFora = new HBox(6, lblFora, cbLigaFora, cbFora);
        linhaFora.setAlignment(Pos.CENTER_LEFT);

        // Escolha do palpite: Casa, Empate ou Fora (só um pode ficar marcado)
        ToggleGroup grupoPalpite = new ToggleGroup();
        RadioButton rbCasa = new RadioButton();
        RadioButton rbEmpate = new RadioButton();
        RadioButton rbFora = new RadioButton();
        rbCasa.setToggleGroup(grupoPalpite);
        rbEmpate.setToggleGroup(grupoPalpite);
        rbFora.setToggleGroup(grupoPalpite);
        rbCasa.setUserData(Bets.CASA);
        rbEmpate.setUserData(Bets.EMPATE);
        rbFora.setUserData(Bets.FORA);
        rbCasa.setSelected(true);
        VBox colunaPalpites = new VBox(4, rbCasa, rbEmpate, rbFora);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Valor (R$)");
        txtValor.setPrefWidth(110);

        Button btnApostar = new Button("Apostar");
        Label lblRetorno = new Label();
        HBox linhaAposta = new HBox(8, txtValor, btnApostar, lblRetorno);
        linhaAposta.setAlignment(Pos.CENTER_LEFT);

        Label lblPlacar = new Label();
        lblPlacar.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblMensagem = new Label();
        Label lblLucro = new Label();

        // Atualiza saldo, odds, lucro e o retorno possível
        Runnable atualizarTela = () -> {
            String casa = cbCasa.getValue();
            String fora = cbFora.getValue();
            boolean mesmoTime = casa.equals(fora);

            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
            btnApostar.setDisable(mesmoTime);

            if (mesmoTime) {
                rbCasa.setText(cbCasa.getValue() + " vence");
                rbEmpate.setText("Empate");
                rbFora.setText(cbFora.getValue() + " vence");
                lblRetorno.setText("");
            } else {
                rbCasa.setText(String.format("%s vence (odd %.2f)", cbCasa.getValue(), bets.getOdd(casa, fora, Bets.CASA)));
                rbEmpate.setText(String.format("Empate (odd %.2f)", bets.getOdd(casa, fora, Bets.EMPATE)));
                rbFora.setText(String.format("%s vence (odd %.2f)", cbFora.getValue(), bets.getOdd(casa, fora, Bets.FORA)));

                // Mostra quanto a aposta pagaria se acertar
                try {
                    double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                    int escolha = (int) grupoPalpite.getSelectedToggle().getUserData();
                    lblRetorno.setText(String.format("Retorno: R$ %.2f", valor * bets.getOdd(casa, fora, escolha)));
                } catch (NumberFormatException ex) {
                    lblRetorno.setText("");
                }
            }

            double lucro = bets.getLucro();
            lblLucro.setText(String.format("Lucro nas apostas: R$ %.2f", lucro));
            if (lucro > 0) {
                lblLucro.setStyle("-fx-text-fill: #2e7d32;");
            } else if (lucro < 0) {
                lblLucro.setStyle("-fx-text-fill: #c62828;");
            } else {
                lblLucro.setStyle("");
            }
        };

        // Quando troca algum time, limpa o placar antigo
        Runnable trocouTime = () -> {
            lblPlacar.setText("");
            if (cbCasa.getValue().equals(cbFora.getValue())) {
                lblMensagem.setText("Escolha dois times diferentes.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            } else {
                lblMensagem.setText("");
            }
            atualizarTela.run();
        };
        cbCasa.setOnAction(e -> trocouTime.run());
        cbFora.setOnAction(e -> trocouTime.run());

        // Trocou o campeonato: a lista de times muda para os times daquela seção
        cbLigaCasa.setOnAction(e -> {
            String[] times = Bets.getTimes(cbLigaCasa.getValue());
            cbCasa.getItems().setAll(times);
            cbCasa.setValue(times[0]);
            trocouTime.run();
        });
        cbLigaFora.setOnAction(e -> {
            String[] times = Bets.getTimes(cbLigaFora.getValue());
            cbFora.getItems().setAll(times);
            // Se o primeiro time for o mesmo da casa, já escolhe o segundo
            if (times.length > 1 && times[0].equals(cbCasa.getValue())) {
                cbFora.setValue(times[1]);
            } else {
                cbFora.setValue(times[0]);
            }
            trocouTime.run();
        });
        grupoPalpite.selectedToggleProperty().addListener((obs, antigo, novo) -> atualizarTela.run());
        txtValor.textProperty().addListener((obs, antigo, novo) -> atualizarTela.run());

        btnApostar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    lblMensagem.setText("Digite um valor maior que zero.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else if (valor > pagamentos.Conta()) {
                    lblMensagem.setText("Saldo insuficiente.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else {
                    String casa = cbCasa.getValue();
                    String fora = cbFora.getValue();
                    int escolha = (int) grupoPalpite.getSelectedToggle().getUserData();

                    // O valor sai do saldo; se ganhar, o prêmio volta
                    pagamentos.SaqueInv(valor);
                    double premio = bets.apostar(casa, fora, escolha, valor);
                    lblPlacar.setText(bets.getUltimoPlacar());

                    if (premio > 0) {
                        pagamentos.DepositoInv(premio);
                        lblMensagem.setText(String.format("Você ganhou R$ %.2f!", premio));
                        lblMensagem.setStyle("-fx-text-fill: #2e7d32;");
                    } else {
                        lblMensagem.setText(String.format("Você perdeu R$ %.2f.", valor));
                        lblMensagem.setStyle("-fx-text-fill: #c62828;");
                    }
                    txtValor.clear();
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            }
            atualizarTela.run();
        });

        atualizarTela.run();

        VBox layout = new VBox(7, topo, linhaCasa, linhaFora, colunaPalpites, linhaAposta, lblPlacar, lblMensagem, lblLucro);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    // Pergunta se o usuário é maior de 18 antes de entrar no Cassino
    private Scene criarCenaIdade(Stage stage, Scene cenaPrincipal, Runnable aoConfirmar) {
        Label lblTitulo = new Label("Cassino");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblPergunta = new Label("Você tem 18 anos ou mais?");
        lblPergunta.setStyle("-fx-font-size: 14px;");

        Button btnSim = new Button("Sim, tenho 18 anos ou mais");
        btnSim.setPrefWidth(200);
        Button btnNao = new Button("Não");
        btnNao.setPrefWidth(200);

        Label lblAviso = new Label("Jogos de azar podem causar dependência. Jogue com responsabilidade.");
        lblAviso.setWrapText(true);
        lblAviso.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        Label lblMensagem = new Label();
        lblMensagem.setWrapText(true);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(200);
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Painel - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        btnSim.setOnAction(e -> aoConfirmar.run());

        btnNao.setOnAction(e -> {
            lblMensagem.setText("Acesso permitido apenas para maiores de 18 anos.");
            lblMensagem.setStyle("-fx-text-fill: #c62828;");
            btnSim.setDisable(true);
            btnNao.setDisable(true);
        });

        VBox layout = new VBox(12, lblTitulo, lblPergunta, btnSim, btnNao, btnVoltar, lblMensagem, lblAviso);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));

        return new Scene(layout, LARGURA, ALTURA);
    }

    // Menu do Cassino: Bets e os jogos de azar
    private Scene criarCenaCassino(
            Stage stage,
            Pagamentos pagamentos,
            Bets bets,
            Aviator aviator,
            CacaNiquel cacaNiquel,
            Scene cenaPrincipal
    ) {
        Label lblTitulo = new Label("Cassino");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnBets = new Button("Bets");
        btnBets.setPrefWidth(180);

        Button btnAviator = new Button("Aviator");
        btnAviator.setPrefWidth(180);

        Button btnBetSurpresa = new Button("Bet Surpresa");
        btnBetSurpresa.setPrefWidth(180);

        Button btnCacaNiquel = new Button("Caça-níquel");
        btnCacaNiquel.setPrefWidth(180);

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setPrefWidth(180);

        Label lblAviso = new Label("Jogue com responsabilidade.");
        lblAviso.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        VBox layout = new VBox(10, lblTitulo, btnBets, btnBetSurpresa, btnAviator, btnCacaNiquel, btnVoltar, lblAviso);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));

        final Scene cenaCassino = new Scene(layout, LARGURA, ALTURA);

        btnBets.setOnAction(e -> {
            stage.setTitle("Bets - Projeto Investimentos FX");
            stage.setScene(criarCenaBets(stage, bets, pagamentos, cenaCassino));
        });

        btnAviator.setOnAction(e -> {
            stage.setTitle("Aviator - Projeto Investimentos FX");
            stage.setScene(criarCenaAviator(stage, aviator, pagamentos, cenaCassino));
        });

        btnBetSurpresa.setOnAction(e -> {
            stage.setTitle("Bet Surpresa - Projeto Investimentos FX");
            stage.setScene(criarCenaBetSurpresa(stage, bets, pagamentos, cenaCassino));
        });

        btnCacaNiquel.setOnAction(e -> {
            stage.setTitle("Caça-níquel - Projeto Investimentos FX");
            stage.setScene(criarCenaCacaNiquel(stage, cacaNiquel, pagamentos, cenaCassino));
        });

        btnVoltar.setOnAction(e -> {
            stage.setTitle("Painel - Projeto Investimentos FX");
            stage.setScene(cenaPrincipal);
        });

        return cenaCassino;
    }

    private Scene criarCenaCacaNiquel(
            Stage stage,
            CacaNiquel cacaNiquel,
            Pagamentos pagamentos,
            Scene cenaCassino
    ) {
        Label lblTitulo = new Label("Caça-níquel");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblSaldo = new Label();

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Cassino - Projeto Investimentos FX");
            stage.setScene(cenaCassino);
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, lblSaldo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        // Os 3 rolos
        Label[] rolos = new Label[CacaNiquel.ROLOS];
        for (int i = 0; i < rolos.length; i++) {
            rolos[i] = new Label();
            rolos[i].setPrefSize(80, 70);
            rolos[i].setAlignment(Pos.CENTER);
            mostrarSimbolo(rolos[i], 5); // começa com 7 7 7
        }
        HBox linhaRolos = new HBox(10, rolos);
        linhaRolos.setAlignment(Pos.CENTER);

        // Tabela de prêmios
        StringBuilder tabela = new StringBuilder("3 iguais pagam:  ");
        for (int i = CacaNiquel.getQuantidadeSimbolos() - 1; i >= 0; i--) {
            tabela.append(CacaNiquel.getSimbolo(i)).append(" ").append(CacaNiquel.getPremio(i)).append("x   ");
        }
        Label lblTabela = new Label(tabela.toString().trim());
        lblTabela.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        TextField txtValor = new TextField();
        txtValor.setPromptText("Valor (R$)");
        txtValor.setPrefWidth(110);

        Button btnGirar = new Button("Girar");
        HBox linhaAposta = new HBox(8, txtValor, btnGirar);
        linhaAposta.setAlignment(Pos.CENTER_LEFT);

        Label lblMensagem = new Label();
        Label lblLucro = new Label();

        Runnable atualizarTela = () -> {
            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
            double lucro = cacaNiquel.getLucro();
            lblLucro.setText(String.format("Lucro no caça-níquel: R$ %.2f", lucro));
            if (lucro > 0) {
                lblLucro.setStyle("-fx-text-fill: #2e7d32;");
            } else if (lucro < 0) {
                lblLucro.setStyle("-fx-text-fill: #c62828;");
            } else {
                lblLucro.setStyle("");
            }
        };

        // Animação: os rolos giram e param um de cada vez, da esquerda pra direita,
        // com 3 segundos de pausa entre uma parada e outra (3 s, 6 s e 9 s).
        final int MS_POR_PASSO = 20; // a tela atualiza a cada 20 ms
        final int PAUSA_ENTRE_PARADAS = 3000;
        final int[] PARA_EM_MS = {PAUSA_ENTRE_PARADAS, PAUSA_ENTRE_PARADAS * 2, PAUSA_ENTRE_PARADAS * 3};
        final int MS_FREANDO = 1500; // no último 1,5 s antes de parar o rolo vai ficando mais lento

        int[] tempoPassado = {0};
        int[] proximaTroca = new int[rolos.length];   // quando o rolo troca de símbolo de novo
        int[] simboloNaTela = new int[rolos.length];
        boolean[] parado = new boolean[rolos.length];
        double[] valorDaRodada = {0};

        // Quando ganha, as bordas dos rolos piscam em verde
        int[] piscadas = {0};
        Timeline piscarVitoria = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            piscadas[0]++;
            String cor = (piscadas[0] % 2 == 0) ? "#2e7d32" : "white";
            for (int i = 0; i < rolos.length; i++) {
                mostrarSimbolo(rolos[i], cacaNiquel.getResultado(i), cor, 4);
            }
        }));
        piscarVitoria.setCycleCount(12);
        piscarVitoria.setOnFinished(e -> {
            for (int i = 0; i < rolos.length; i++) {
                mostrarSimbolo(rolos[i], cacaNiquel.getResultado(i), "#2e7d32", 4);
            }
        });

        Timeline giro = new Timeline();
        giro.getKeyFrames().add(new KeyFrame(Duration.millis(MS_POR_PASSO), e -> {
            tempoPassado[0] += MS_POR_PASSO;
            int t = tempoPassado[0];
            boolean doisPrimeirosIguais = parado[0] && parado[1]
                    && cacaNiquel.getResultado(0) == cacaNiquel.getResultado(1);

            for (int i = 0; i < rolos.length; i++) {
                if (parado[i]) {
                    continue;
                }

                // Chegou a hora desse rolo parar
                if (t >= PARA_EM_MS[i]) {
                    parado[i] = true;
                    mostrarSimbolo(rolos[i], cacaNiquel.getResultado(i), "#333333", 3);

                    // Os dois primeiros deram iguais: agora é tudo no último rolo!
                    if (i == 1 && cacaNiquel.getResultado(0) == cacaNiquel.getResultado(1)) {
                        lblMensagem.setText("Dois iguais... falta só um!");
                        lblMensagem.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                    }
                    continue;
                }

                // Ainda girando: troca de símbolo, cada vez mais devagar perto de parar
                if (t >= proximaTroca[i]) {
                    simboloNaTela[i] = cacaNiquel.simboloAleatorio();
                    int restante = PARA_EM_MS[i] - t;
                    int intervalo = 20; // girando rápido: troca de símbolo a cada 20 ms
                    if (restante < MS_FREANDO) {
                        intervalo = 20 + (MS_FREANDO - restante) * 340 / MS_FREANDO; // freia de 20 ms até 360 ms
                    }
                    proximaTroca[i] = t + intervalo;
                }

                // Se os dois primeiros bateram, o último rolo fica piscando em laranja
                String borda = "gray";
                int largura = 2;
                if (i == 2 && doisPrimeirosIguais && (t / 250) % 2 == 0) {
                    borda = "#ef6c00";
                    largura = 4;
                }
                mostrarSimbolo(rolos[i], simboloNaTela[i], borda, largura);
            }

            boolean todosParados = parado[0] && parado[1] && parado[2];
            if (todosParados) {
                giro.stop();
                double premio = cacaNiquel.getUltimoPremio();
                if (premio > 0) {
                    pagamentos.DepositoInv(premio);
                    lblMensagem.setText(String.format("3 iguais! Você ganhou R$ %.2f!", premio));
                    lblMensagem.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    piscadas[0] = 0;
                    piscarVitoria.playFromStart();
                } else {
                    boolean quase = cacaNiquel.getResultado(0) == cacaNiquel.getResultado(1)
                            || cacaNiquel.getResultado(1) == cacaNiquel.getResultado(2);
                    String inicio = quase ? "Quase! " : "Não foi dessa vez. ";
                    lblMensagem.setText(String.format("%sVocê perdeu R$ %.2f.", inicio, valorDaRodada[0]));
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                }
                btnGirar.setDisable(false);
                btnVoltar.setDisable(false);
                atualizarTela.run();
            }
        }));
        giro.setCycleCount(Timeline.INDEFINITE);

        btnGirar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    lblMensagem.setText("Digite um valor maior que zero.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else if (valor > pagamentos.Conta()) {
                    lblMensagem.setText("Saldo insuficiente.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else {
                    // O valor sai do saldo; o resultado já é sorteado aqui e a animação só mostra
                    pagamentos.SaqueInv(valor);
                    valorDaRodada[0] = valor;
                    cacaNiquel.girar(valor);

                    lblMensagem.setText("Girando...");
                    lblMensagem.setStyle("");
                    lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
                    btnGirar.setDisable(true);
                    btnVoltar.setDisable(true);
                    // Zera a animação para a rodada nova
                    piscarVitoria.stop();
                    tempoPassado[0] = 0;
                    for (int i = 0; i < rolos.length; i++) {
                        proximaTroca[i] = 0;
                        parado[i] = false;
                    }
                    giro.play();
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            }
        });

        atualizarTela.run();

        VBox layout = new VBox(8, topo, linhaRolos, lblTabela, linhaAposta, lblMensagem, lblLucro);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    // Mostra um símbolo num rolo, com borda cinza normal
    private void mostrarSimbolo(Label rolo, int simbolo) {
        mostrarSimbolo(rolo, simbolo, "gray", 2);
    }

    // Mostra um símbolo num rolo, com a cor dele e a borda escolhida
    private void mostrarSimbolo(Label rolo, int simbolo, String corBorda, int larguraBorda) {
        String[] cores = {"#2e7d32", "#c62828", "#1565c0", "#f9a825", "black", "#c62828"};
        String texto = CacaNiquel.getSimbolo(simbolo);
        String tamanho = texto.length() > 1 ? "22px" : "32px"; // "BAR" é mais largo
        rolo.setText(texto);
        rolo.setStyle("-fx-font-size: " + tamanho + "; -fx-font-weight: bold;"
                + " -fx-text-fill: " + cores[simbolo] + ";"
                + " -fx-background-color: white; -fx-border-color: " + corBorda + ";"
                + " -fx-border-width: " + larguraBorda + ";");
    }

    // Bet Surpresa: o app sorteia os times, as odds ficam escondidas até apostar
    // e depois o jogo é narrado minuto a minuto
    private Scene criarCenaBetSurpresa(
            Stage stage,
            Bets bets,
            Pagamentos pagamentos,
            Scene cenaCassino
    ) {
        Random gerador = new Random();
        Narrador narrador = new Narrador();
        String[] todosTimes = Bets.getTodosTimes();

        Label lblTitulo = new Label("Bet Surpresa");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblSaldo = new Label();

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setOnAction(e -> {
            stage.setTitle("Cassino - Projeto Investimentos FX");
            stage.setScene(cenaCassino);
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, lblSaldo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        // Jogo sorteado
        Label lblJogo = new Label();
        lblJogo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Button btnSortear = new Button("Sortear times");
        Region espaco2 = new Region();
        HBox.setHgrow(espaco2, Priority.ALWAYS);
        HBox linhaJogo = new HBox(8, lblJogo, espaco2, btnSortear);
        linhaJogo.setAlignment(Pos.CENTER_LEFT);

        // Palpite (as odds aparecem como "?" até apostar)
        ToggleGroup grupoPalpite = new ToggleGroup();
        RadioButton rbCasa = new RadioButton();
        RadioButton rbEmpate = new RadioButton();
        RadioButton rbFora = new RadioButton();
        rbCasa.setToggleGroup(grupoPalpite);
        rbEmpate.setToggleGroup(grupoPalpite);
        rbFora.setToggleGroup(grupoPalpite);
        rbCasa.setUserData(Bets.CASA);
        rbEmpate.setUserData(Bets.EMPATE);
        rbFora.setUserData(Bets.FORA);
        rbCasa.setSelected(true);
        HBox linhaPalpites = new HBox(12, rbCasa, rbEmpate, rbFora);
        linhaPalpites.setAlignment(Pos.CENTER_LEFT);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Valor (R$)");
        txtValor.setPrefWidth(110);
        Button btnApostar = new Button("Apostar");
        HBox linhaAposta = new HBox(8, txtValor, btnApostar);
        linhaAposta.setAlignment(Pos.CENTER_LEFT);

        // Placar ao vivo
        Label lblPlacar = new Label();
        lblPlacar.setStyle("-fx-font-weight: bold;");
        Label lblLucro = new Label();
        Region espaco3 = new Region();
        HBox.setHgrow(espaco3, Priority.ALWAYS);
        HBox linhaPlacar = new HBox(8, lblPlacar, espaco3, lblLucro);

        // Narração (o lance mais novo fica em cima)
        ListView<String> narracao = new ListView<>();
        narracao.setPrefHeight(100);
        VBox.setVgrow(narracao, Priority.ALWAYS);

        Label lblMensagem = new Label();

        String[] times = new String[2]; // [0] = casa, [1] = fora

        // Mostra as odds escondidas ("?") ou reveladas
        java.util.function.Consumer<Boolean> mostrarOdds = revelar -> {
            if (revelar) {
                rbCasa.setText(String.format("Casa (%.2f)", bets.getOdd(times[0], times[1], Bets.CASA)));
                rbEmpate.setText(String.format("Empate (%.2f)", bets.getOdd(times[0], times[1], Bets.EMPATE)));
                rbFora.setText(String.format("Fora (%.2f)", bets.getOdd(times[0], times[1], Bets.FORA)));
            } else {
                rbCasa.setText("Casa (?)");
                rbEmpate.setText("Empate (?)");
                rbFora.setText("Fora (?)");
            }
        };

        Runnable atualizarTela = () -> {
            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
            double lucro = bets.getLucro();
            lblLucro.setText(String.format("Lucro nas apostas: R$ %.2f", lucro));
            if (lucro > 0) {
                lblLucro.setStyle("-fx-text-fill: #2e7d32;");
            } else if (lucro < 0) {
                lblLucro.setStyle("-fx-text-fill: #c62828;");
            } else {
                lblLucro.setStyle("");
            }
        };

        Runnable travarControles = () -> {
            btnApostar.setDisable(true);
            btnSortear.setDisable(true);
            btnVoltar.setDisable(true);
            linhaPalpites.setDisable(true);
        };

        // ----- Roleta dos times: os nomes passam rápido e param num jogo -----
        int[] passoRoleta = {0};
        Timeline roleta = new Timeline(new KeyFrame(Duration.millis(60), e -> {
            passoRoleta[0]++;
            String a = todosTimes[gerador.nextInt(todosTimes.length)];
            String b = todosTimes[gerador.nextInt(todosTimes.length)];
            lblJogo.setText(a + " x " + b);
        }));
        roleta.setCycleCount(25); // ~1,5 segundo girando
        roleta.setOnFinished(e -> {
            lblJogo.setText(times[0] + " x " + times[1]);
            lblMensagem.setText("Escolha seu palpite. As odds só aparecem depois de apostar!");
            lblMensagem.setStyle("");
            btnApostar.setDisable(false);
            btnSortear.setDisable(false);
            btnVoltar.setDisable(false);
            linhaPalpites.setDisable(false);
        });

        Runnable sortearTimes = () -> {
            times[0] = todosTimes[gerador.nextInt(todosTimes.length)];
            do {
                times[1] = todosTimes[gerador.nextInt(todosTimes.length)];
            } while (times[1].equals(times[0]));

            mostrarOdds.accept(false);
            narracao.getItems().clear();
            lblPlacar.setText("");
            lblMensagem.setText("Sorteando times...");
            lblMensagem.setStyle("");
            travarControles.run();
            passoRoleta[0] = 0;
            roleta.playFromStart();
        };

        btnSortear.setOnAction(e -> sortearTimes.run());

        // ----- Jogo narrado: cada minuto dura 150 ms -----
        int[] minuto = {0};
        int[] fimDoJogo = {90};
        int[] gols = new int[2];
        int[] golNoMinuto = new int[100]; // -1 = sem gol, 0 = gol da casa, 1 = gol de fora
        double[] premioRodada = {0};
        double[] valorRodada = {0};

        Timeline jogo = new Timeline();
        jogo.getKeyFrames().add(new KeyFrame(Duration.millis(150), e -> {
            minuto[0]++;
            int m = minuto[0];
            String tempo = (m <= 90) ? m + "'" : "90+" + (m - 90) + "'";
            String placar = times[0] + " " + gols[0] + " x " + gols[1] + " " + times[1];

            if (golNoMinuto[m] >= 0) {
                int lado = golNoMinuto[m];
                gols[lado]++;
                placar = times[0] + " " + gols[0] + " x " + gols[1] + " " + times[1];
                narracao.getItems().add(0, tempo + " " + narrador.gol(times[lado]));
                narracao.getItems().add(0, "        " + placar);
            } else if (gerador.nextDouble() < 0.08) {
                narracao.getItems().add(0, tempo + " " + narrador.lance(times[gerador.nextInt(2)]));
            }

            if (m == 45) {
                narracao.getItems().add(0, "45' Fim do primeiro tempo! " + placar);
            } else if (m == 46) {
                narracao.getItems().add(0, "46' Bola rolando para o segundo tempo!");
            } else if (m == 90 && fimDoJogo[0] > 90) {
                narracao.getItems().add(0, "90' O quarto árbitro indica " + (fimDoJogo[0] - 90) + " minutos de acréscimo!");
            }

            lblPlacar.setText(tempo + "  " + placar);

            if (m >= fimDoJogo[0]) {
                jogo.stop();
                narracao.getItems().add(0, "FIM DE JOGO! " + placar);

                if (premioRodada[0] > 0) {
                    pagamentos.DepositoInv(premioRodada[0]);
                    lblMensagem.setText(String.format("Você acertou! Ganhou R$ %.2f.", premioRodada[0]));
                    lblMensagem.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                } else {
                    lblMensagem.setText(String.format("Você errou e perdeu R$ %.2f. Sorteie um novo jogo.", valorRodada[0]));
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                }
                // Para apostar de novo precisa sortear outro jogo (as odds desse já foram reveladas)
                btnSortear.setDisable(false);
                btnVoltar.setDisable(false);
                atualizarTela.run();
            }
        }));
        jogo.setCycleCount(Timeline.INDEFINITE);

        btnApostar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    lblMensagem.setText("Digite um valor maior que zero.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else if (valor > pagamentos.Conta()) {
                    lblMensagem.setText("Saldo insuficiente.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else {
                    int escolha = (int) grupoPalpite.getSelectedToggle().getUserData();

                    // O valor sai do saldo e o resultado já é sorteado aqui; a narração só mostra
                    pagamentos.SaqueInv(valor);
                    valorRodada[0] = valor;
                    premioRodada[0] = bets.apostar(times[0], times[1], escolha, valor);
                    mostrarOdds.accept(true);

                    // Monta em que minutos saem os gols
                    fimDoJogo[0] = 90 + 1 + gerador.nextInt(5); // 1 a 5 minutos de acréscimo
                    Arrays.fill(golNoMinuto, -1);
                    List<Integer> lados = new ArrayList<>();
                    for (int i = 0; i < bets.getGolsCasa(); i++) {
                        lados.add(0);
                    }
                    for (int i = 0; i < bets.getGolsFora(); i++) {
                        lados.add(1);
                    }
                    Collections.shuffle(lados);
                    List<Integer> minutosDosGols = new ArrayList<>();
                    while (minutosDosGols.size() < lados.size()) {
                        int min = 1 + gerador.nextInt(fimDoJogo[0]);
                        if (!minutosDosGols.contains(min)) {
                            minutosDosGols.add(min);
                        }
                    }
                    Collections.sort(minutosDosGols);
                    for (int i = 0; i < lados.size(); i++) {
                        golNoMinuto[minutosDosGols.get(i)] = lados.get(i);
                    }

                    gols[0] = 0;
                    gols[1] = 0;
                    minuto[0] = 0;
                    narracao.getItems().clear();
                    narracao.getItems().add(0, "0' Apita o árbitro! Começa o jogo: " + times[0] + " x " + times[1]);
                    lblPlacar.setText("0'  " + times[0] + " 0 x 0 " + times[1]);

                    String[] nomesPalpite = {times[0] + " vence", "Empate", times[1] + " vence"};
                    lblMensagem.setText(String.format("Apostou R$ %.2f em: %s (odd %.2f)",
                            valor, nomesPalpite[escolha], bets.getOdd(times[0], times[1], escolha)));
                    lblMensagem.setStyle("");
                    txtValor.clear();
                    travarControles.run();
                    jogo.play();
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            }
            atualizarTela.run();
        });

        atualizarTela.run();
        sortearTimes.run(); // já abre a tela sorteando um jogo

        VBox layout = new VBox(6, topo, linhaJogo, linhaPalpites, linhaAposta, linhaPlacar, narracao, lblMensagem);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    private Scene criarCenaAviator(
            Stage stage,
            Aviator aviator,
            Pagamentos pagamentos,
            Scene cenaCassino
    ) {
        Label lblTitulo = new Label("Aviator");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblSaldo = new Label();

        Button btnVoltar = new Button("Voltar");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(10, lblTitulo, lblSaldo, espaco, btnVoltar);
        topo.setAlignment(Pos.CENTER_LEFT);

        // Multiplicador atual e os últimos resultados
        Label lblMultiplicador = new Label();
        lblMultiplicador.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label lblUltimos = new Label();
        lblUltimos.setStyle("-fx-text-fill: gray;");
        HBox linhaInfo = new HBox(10, lblMultiplicador, lblUltimos);
        linhaInfo.setAlignment(Pos.CENTER_LEFT);

        // Gráfico do voo
        Canvas tela = new Canvas(LARGURA - 20, 122);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Valor (R$)");
        txtValor.setPrefWidth(110);

        Button btnApostar = new Button("Apostar");
        Button btnRetirar = new Button("Retirar");
        btnRetirar.setDisable(true);

        Label lblGanhoAtual = new Label();

        HBox linhaBotoes = new HBox(8, txtValor, btnApostar, btnRetirar, lblGanhoAtual);
        linhaBotoes.setAlignment(Pos.CENTER_LEFT);

        // Retirada automática: retira sozinho quando chegar no multiplicador escolhido
        CheckBox chkAuto = new CheckBox("Retirada automática em");
        TextField txtAuto = new TextField("2.00");
        txtAuto.setPrefWidth(60);
        HBox linhaAuto = new HBox(6, chkAuto, txtAuto, new Label("x"));
        linhaAuto.setAlignment(Pos.CENTER_LEFT);

        Label lblMensagem = new Label();
        Label lblLucro = new Label();

        Runnable atualizarTela = () -> {
            lblSaldo.setText(String.format("Saldo: R$ %.2f", pagamentos.Conta()));
            lblMultiplicador.setText(String.format("%.2fx", aviator.getMultiplicador()));
            lblMultiplicador.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"
                    + (aviator.isExplodiu() ? " -fx-text-fill: #c62828;" : ""));

            StringBuilder ultimos = new StringBuilder();
            for (double r : aviator.getUltimosResultados()) {
                ultimos.append(String.format("%.2fx  ", r));
            }
            lblUltimos.setText(ultimos.length() > 0 ? "Últimos: " + ultimos.toString().trim() : "");

            if (aviator.isApostaAtiva()) {
                lblGanhoAtual.setText(String.format("Retirar agora: R$ %.2f", aviator.getValorApostado() * aviator.getMultiplicador()));
            } else {
                lblGanhoAtual.setText("");
            }

            double lucro = aviator.getLucro();
            lblLucro.setText(String.format("Lucro no Aviator: R$ %.2f", lucro));
            if (lucro > 0) {
                lblLucro.setStyle("-fx-text-fill: #2e7d32;");
            } else if (lucro < 0) {
                lblLucro.setStyle("-fx-text-fill: #c62828;");
            } else {
                lblLucro.setStyle("");
            }

            desenharAviator(tela, aviator);
        };

        // Roda a cada quadro da tela e avança o voo pelo tempo real que passou
        AnimationTimer voo = new AnimationTimer() {
            private long ultimoQuadro = 0;

            @Override
            public void start() {
                ultimoQuadro = 0;
                super.start();
            }

            @Override
            public void handle(long agora) {
                if (ultimoQuadro > 0) {
                    double segundos = (agora - ultimoQuadro) / 1_000_000_000.0;
                    aviator.avancar(segundos);
                }
                ultimoQuadro = agora;

                // A retirada automática aconteceu nesse quadro: coloca o prêmio no saldo
                double premioAuto = aviator.pegarPremioAutomatico();
                if (premioAuto > 0) {
                    pagamentos.DepositoInv(premioAuto);
                    lblMensagem.setText(String.format("Retirada automática em %.2fx: R$ %.2f",
                            aviator.getMultiplicadorRetirada(), premioAuto));
                    lblMensagem.setStyle("-fx-text-fill: #2e7d32;");
                    btnRetirar.setDisable(true);
                }

                if (!aviator.isRodadaAtiva()) {
                    stop();
                    if (!aviator.isRetirou()) {
                        lblMensagem.setText(String.format("O avião voou em %.2fx! Você perdeu R$ %.2f.",
                                aviator.getMultiplicador(), aviator.getValorApostado()));
                        lblMensagem.setStyle("-fx-text-fill: #c62828;");
                    }
                    btnRetirar.setDisable(true);
                    btnApostar.setDisable(false);
                    btnVoltar.setDisable(false);
                    chkAuto.setDisable(false);
                    txtAuto.setDisable(false);
                }
                atualizarTela.run();
            }
        };

        btnApostar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
                if (valor <= 0) {
                    lblMensagem.setText("Digite um valor maior que zero.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else if (valor > pagamentos.Conta()) {
                    lblMensagem.setText("Saldo insuficiente.");
                    lblMensagem.setStyle("-fx-text-fill: #c62828;");
                } else {
                    // Confere o alvo da retirada automática (se estiver ligada)
                    double alvo = 0;
                    if (chkAuto.isSelected()) {
                        alvo = Double.parseDouble(txtAuto.getText().trim().replace(",", "."));
                        if (alvo <= 1.0) {
                            lblMensagem.setText("A retirada automática precisa ser maior que 1.00x.");
                            lblMensagem.setStyle("-fx-text-fill: #c62828;");
                            atualizarTela.run();
                            return;
                        }
                    }

                    // O valor sai do saldo e o avião decola
                    pagamentos.SaqueInv(valor);
                    aviator.iniciar(valor);
                    aviator.setRetiradaAutomatica(alvo);
                    if (alvo > 0) {
                        lblMensagem.setText(String.format("Decolou! Retirada automática em %.2fx.", alvo));
                    } else {
                        lblMensagem.setText("Decolou! Retire antes do avião sumir.");
                    }
                    lblMensagem.setStyle("");
                    btnApostar.setDisable(true);
                    btnRetirar.setDisable(false);
                    btnVoltar.setDisable(true); // não dá para sair no meio do voo
                    chkAuto.setDisable(true);   // o alvo fica travado durante o voo
                    txtAuto.setDisable(true);
                    voo.start();
                }
            } catch (NumberFormatException ex) {
                lblMensagem.setText("Digite um número válido.");
                lblMensagem.setStyle("-fx-text-fill: #c62828;");
            }
            atualizarTela.run();
        });

        // Retirar não para o avião: ele continua até sumir, só para mostrar até onde iria
        btnRetirar.setOnAction(e -> {
            double premio = aviator.retirar();
            if (premio > 0) {
                pagamentos.DepositoInv(premio);
                lblMensagem.setText(String.format("Retirou em %.2fx: R$ %.2f", aviator.getMultiplicadorRetirada(), premio));
                lblMensagem.setStyle("-fx-text-fill: #2e7d32;");
                btnRetirar.setDisable(true);
            }
            atualizarTela.run();
        });

        btnVoltar.setOnAction(e -> {
            voo.stop();
            stage.setTitle("Cassino - Projeto Investimentos FX");
            stage.setScene(cenaCassino);
        });

        atualizarTela.run();

        VBox layout = new VBox(6, topo, linhaInfo, tela, linhaBotoes, linhaAuto, lblMensagem, lblLucro);
        layout.setPadding(new Insets(10));

        return new Scene(layout, LARGURA, ALTURA);
    }

    // Desenha o gráfico do voo de forma simples, no mesmo estilo do resto do app
    private void desenharAviator(Canvas tela, Aviator aviator) {
        GraphicsContext gc = tela.getGraphicsContext2D();
        double w = tela.getWidth();
        double h = tela.getHeight();

        // Área do gráfico (sobra espaço à esquerda e embaixo para os números)
        double esq = 34;
        double dir = 10;
        double cima = 8;
        double baixo = 16;
        double larguraG = w - esq - dir;
        double alturaG = h - cima - baixo;
        double origemY = h - baixo;

        double tempo = aviator.getTempo();
        double mult = aviator.getMultiplicador();
        boolean jogou = aviator.isRodadaAtiva() || aviator.isExplodiu();

        // Fundo branco com borda cinza
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        gc.strokeRect(0.5, 0.5, w - 1, h - 1);

        // Escalas: crescem junto com o voo para a curva sempre caber
        double tempoMax = Math.max(8, tempo * 1.15);
        double multMax = Math.max(2, 1 + (mult - 1) * 1.25);

        // Eixos
        gc.setStroke(Color.GRAY);
        gc.strokeLine(esq, cima, esq, origemY);
        gc.strokeLine(esq, origemY, w - dir, origemY);

        // Valores nos eixos: só o começo e o fim
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("System", 10));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("1.0x", esq - 4, origemY + 3);
        gc.fillText(String.format("%.1fx", multMax), esq - 4, cima + 8);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("0s", esq, h - 3);
        gc.fillText(String.format("%.0fs", tempoMax), w - dir - 8, h - 3);

        if (!jogou) {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("System", 13));
            gc.fillText("Faça uma aposta para decolar", esq + larguraG / 2, cima + alturaG / 2);
            return;
        }

        // Curva: o multiplicador de cada instante do voo
        int pontos = 100;
        double[] xs = new double[pontos];
        double[] ys = new double[pontos];
        for (int i = 0; i < pontos; i++) {
            double t = tempo * i / (pontos - 1);
            double m = Math.min(Aviator.multiplicadorNoTempo(t), mult);
            xs[i] = esq + t / tempoMax * larguraG;
            ys[i] = origemY - (m - 1) / (multMax - 1) * alturaG;
        }
        gc.setStroke(aviator.isExplodiu() ? Color.web("#c62828") : Color.GRAY);
        gc.setLineWidth(2);
        gc.strokePolyline(xs, ys, pontos);

        // Marca onde o jogador retirou
        if (aviator.isRetirou()) {
            double tR = Math.log(aviator.getMultiplicadorRetirada()) / Aviator.VELOCIDADE;
            double xR = esq + tR / tempoMax * larguraG;
            double yR = origemY - (aviator.getMultiplicadorRetirada() - 1) / (multMax - 1) * alturaG;
            gc.setFill(Color.web("#2e7d32"));
            gc.fillOval(xR - 4, yR - 4, 8, 8);
        }

        // Avião na ponta da linha, inclinado igual à curva
        if (!aviator.isExplodiu()) {
            double inclinacaoX = larguraG / tempoMax;
            double inclinacaoY = Aviator.VELOCIDADE * mult / (multMax - 1) * alturaG;
            double angulo = -Math.toDegrees(Math.atan2(inclinacaoY, inclinacaoX));
            desenharAviao(gc, xs[pontos - 1], ys[pontos - 1], angulo);
        } else {
            gc.setFill(Color.web("#c62828"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            gc.fillText("Voou!", xs[pontos - 1] - 12, ys[pontos - 1] - 8);
        }
    }

    // Aviãozinho simples, com o nariz em (x, y)
    private void desenharAviao(GraphicsContext gc, double x, double y, double angulo) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angulo);
        gc.setFill(Color.DIMGRAY);
        gc.fillPolygon(new double[]{0, -18, -18}, new double[]{0, -3, 3}, 3);         // corpo
        gc.fillPolygon(new double[]{-8, -12, -14}, new double[]{0, -10, 0}, 3);       // asa
        gc.fillPolygon(new double[]{-15, -18, -19}, new double[]{-2, -7, -2}, 3);     // cauda
        gc.restore();
    }
}
