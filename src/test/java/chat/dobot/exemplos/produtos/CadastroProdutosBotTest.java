package chat.dobot.exemplos.produtos;

import chat.dobot.bot.Contexto;
import chat.dobot.bot.DoBotConfig;
import chat.dobot.bot.service.DoBotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CadastroProdutosBotTest {

    private CadastroProdutosBot bot;
    private StubProdutoService servico;
    private Map<String, DoBotService<Record>> servicos;

    @BeforeEach
    void setUp() {
        bot = new CadastroProdutosBot();
        servico = new StubProdutoService();
        servicos = new HashMap<>();
        @SuppressWarnings("unchecked")
        DoBotService<Record> servicoRecord = (DoBotService<Record>) (DoBotService<?>) servico;
        servicos.put("Produto", servicoRecord);
    }

    @Test
    @DisplayName("aloMundo deve exibir boas-vindas, menu e alterar estado para opcao")
    void aloMundoDeveExibirMenuEEntrarNoEstadoOpcao() {
        Contexto contexto = new Contexto("", "main", servicos);

        bot.aloMundo(contexto);

        assertEquals(
                List.of(
                        "Bem-vindo ao exemplo de cadastro de produtos!",
                        "Menu: \n1 - Cadastrar produto\n2 - Listar produtos\n3 - Sair"
                ),
                contexto.getRespostas()
        );
        assertEquals("opcao", contexto.getEstado());
    }

    @Test
    @DisplayName("menu deve exibir menu e alterar estado para opcao")
    void menuDeveExibirMenuEEntrarNoEstadoOpcao() {
        Contexto contexto = new Contexto("", "menu", servicos);

        bot.menu(contexto);

        assertEquals(
                List.of("Menu: \n1 - Cadastrar produto\n2 - Listar produtos\n3 - Sair"),
                contexto.getRespostas()
        );
        assertEquals("opcao", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao 1 deve solicitar dados do produto e alterar estado para cadastrar")
    void opcaoUmDevePedirFormatoDoProduto() {
        Contexto contexto = new Contexto("1", "opcao", servicos);

        bot.opcao(contexto);

        assertEquals(List.of("Envie o produto no formato: nome;preco (ex: Caneca;12.50)"), contexto.getRespostas());
        assertEquals("cadastrar", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao 2 deve exibir mensagem quando não houver produtos cadastrados")
    void opcaoDoisDeveExibirMensagemQuandoNaoHouverProdutos() {
        Contexto contexto = new Contexto("2", "opcao", servicos);

        bot.opcao(contexto);

        assertEquals(List.of("Nenhum produto cadastrado."), contexto.getRespostas());
        assertEquals("main", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao 2 deve listar produtos quando existirem cadastrados")
    void opcaoDoisDeveListarProdutosQuandoExistirem() {
        servico.salvar(new Produto(1, "Caneca", 12.50));
        Contexto contexto = new Contexto("2", "opcao", servicos);

        bot.opcao(contexto);

        String respostaEsperada = "Produtos cadastrados:\nProduto[id=1, nome=Caneca, preco=12.5]\n";
        assertEquals(List.of(respostaEsperada), contexto.getRespostas());
        assertEquals("main", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao 2 deve exibir mensagem de erro se o serviço lançar exceção")
    void opcaoDoisDeveTratarExcecaoAoListar() {
        servico.setLancarExcecaoAoBuscar(true);
        Contexto contexto = new Contexto("2", "opcao", servicos);

        bot.opcao(contexto);

        assertEquals(List.of("Falha ao listar produtos: Erro de banco de dados"), contexto.getRespostas());
        assertEquals("main", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao 3 deve encerrar conversação e retornar ao estado main")
    void opcaoTresDeveEncerrarConversacao() {
        Contexto contexto = new Contexto("3", "opcao", servicos);

        bot.opcao(contexto);

        assertEquals(List.of("Até logo!"), contexto.getRespostas());
        assertEquals("main", contexto.getEstado());
    }

    @Test
    @DisplayName("opcao inválida deve informar o usuário e manter o estado")
    void opcaoInvalidaDeveExibirMensagemDeAlerta() {
        Contexto contexto = new Contexto("99", "opcao", servicos);

        bot.opcao(contexto);

        assertEquals(List.of("Opção inválida. Digite 1, 2 ou 3."), contexto.getRespostas());
        assertEquals("opcao", contexto.getEstado());
    }

    @Test
    @DisplayName("cadastrar deve salvar produto e retornar ao estado main quando formato for válido")
    void cadastrarDeveSalvarProdutoQuandoFormatoForValido() {
        Contexto contexto = new Contexto("Caneca;12.50", "cadastrar", servicos);

        bot.cadastrar(contexto);

        assertEquals(List.of("Produto cadastrado: Produto[id=0, nome=Caneca, preco=12.5]"), contexto.getRespostas());
        assertEquals("main", contexto.getEstado());
        assertEquals(List.of(new Produto(0, "Caneca", 12.50)), servico.itensSalvos);
    }

    @Test
    @DisplayName("cadastrar deve exibir erro quando o formato for inválido")
    void cadastrarDeveExibirErroQuandoFormatoInvalido() {
        Contexto contexto = new Contexto("CanecaSemPontoEVirgula", "cadastrar", servicos);

        bot.cadastrar(contexto);

        assertEquals(List.of("Formato inválido. Use: nome;preco (ex: Caneca;12.50)"), contexto.getRespostas());
        assertEquals("cadastrar", contexto.getEstado());
    }

    @Test
    @DisplayName("cadastrar deve exibir erro quando o preço for um número inválido")
    void cadastrarDeveExibirErroQuandoPrecoInvalido() {
        Contexto contexto = new Contexto("Caneca;abc", "cadastrar", servicos);

        bot.cadastrar(contexto);

        assertEquals(List.of("Preço inválido. Use um número (ex: 12.50)"), contexto.getRespostas());
        assertEquals("cadastrar", contexto.getEstado());
    }

    @Test
    @DisplayName("cadastrar deve tratar exceção do serviço ao salvar produto")
    void cadastrarDeveTratarExcecaoDoServico() {
        servico.setLancarExcecaoAoSalvar(true);
        Contexto contexto = new Contexto("Caneca;12.50", "cadastrar", servicos);

        bot.cadastrar(contexto);

        assertEquals(List.of("Falha ao salvar produto: Erro ao salvar"), contexto.getRespostas());
        assertEquals("cadastrar", contexto.getEstado());
    }

    @Test
    @DisplayName("config deve definir mensagem inicial")
    void configDeveDefinirMensagemInicial() {
        DoBotConfig config = new DoBotConfig();

        bot.config(config);

        assertEquals("👋 Olá! Eu sou o chatbot De Cadastro de produtos!", config.getMensagemInicial());
    }

    private static class StubProdutoService extends DoBotService<Produto> {
        private final List<Produto> itensSalvos = new ArrayList<>();
        private boolean lancarExcecaoAoBuscar = false;
        private boolean lancarExcecaoAoSalvar = false;

        private StubProdutoService() {
            super(Produto.class, null);
        }

        public void setLancarExcecaoAoBuscar(boolean valor) {
            this.lancarExcecaoAoBuscar = valor;
        }

        public void setLancarExcecaoAoSalvar(boolean valor) {
            this.lancarExcecaoAoSalvar = valor;
        }

        @Override
        public void salvar(Produto obj) {
            if (lancarExcecaoAoSalvar) {
                throw new RuntimeException("Erro ao salvar");
            }
            itensSalvos.add(obj);
        }

        @Override
        public List<Produto> buscarTodos() {
            if (lancarExcecaoAoBuscar) {
                throw new RuntimeException("Erro de banco de dados");
            }
            return new ArrayList<>(itensSalvos);
        }
    }
}

