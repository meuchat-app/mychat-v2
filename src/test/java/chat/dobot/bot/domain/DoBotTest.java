package chat.dobot.bot.domain;

import chat.dobot.bot.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DoBotTest {

    private DoBot doBot;

    @BeforeEach
    void setUp() {
        doBot = new DoBot("bot1", "Bot de Teste", "Descrição do bot");
    }

    @Test
    @DisplayName("Deve inicializar DoBot com propriedades corretas")
    void deveInicializarDoBot() {
        assertEquals("bot1", doBot.getId());
        assertEquals("Bot de Teste", doBot.getNome());
        assertEquals("Descrição do bot", doBot.getDescricao());
        assertNotNull(doBot.getConfig());
        assertNotNull(doBot.getDoBotTema());
        assertTrue(doBot.getMensagens().isEmpty());
    }

    @Test
    @DisplayName("Deve definir estados e estado inicial como 'main'")
    void deveDefinirEstados() {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> contexto.responder("Resposta main"));

        doBot.setEstados(estados);

        assertEquals("main", doBot.getEstadoAtual());
        assertTrue(doBot.getEstados().contains("main"));
    }

    @Test
    @DisplayName("Deve lançar DoBotException se o estado 'main' não estiver presente")
    void deveLancarExcecaoSemEstadoMain() {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("outro", contexto -> {});

        assertThrows(DoBotException.class, () -> doBot.setEstados(estados));
    }

    @Test
    @DisplayName("Deve receber mensagem e executar o estado correspondente")
    void deveReceberMensagemEExecutarEstado() throws EstadoInvalidoException {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> {
            contexto.responder("Olá do bot!");
            contexto.mudarEstado("main");
        });
        doBot.setEstados(estados);

        Contexto contexto = new Contexto("Oi", "main", new HashMap<>());
        doBot.receberMensagem(contexto);

        List<Mensagem> mensagens = doBot.getMensagens();
        assertEquals(2, mensagens.size());
        assertEquals(Autor.USUARIO, mensagens.get(0).autor());
        assertEquals("Oi", mensagens.get(0).conteudo());
        assertEquals(Autor.BOT, mensagens.get(1).autor());
        assertEquals("Olá do bot!", mensagens.get(1).conteudo());
    }

    @Test
    @DisplayName("Deve lançar DoBotException ao receber mensagem com estado não cadastrado")
    void deveLancarExcecaoComEstadoNaoCadastrado() {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> {});
        doBot.setEstados(estados);

        Contexto contexto = new Contexto("Oi", "inexistente", new HashMap<>());

        assertThrows(DoBotException.class, () -> doBot.receberMensagem(contexto));
    }

    @Test
    @DisplayName("Deve persistir o estado alterado pelo handler")
    void devePersistirEstadoAlterado() throws EstadoInvalidoException {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> contexto.mudarEstado("proximo"));
        estados.put("proximo", contexto -> contexto.responder("ok"));
        doBot.setEstados(estados);

        doBot.receberMensagem(new Contexto("Oi", "main", new HashMap<>()));

        assertEquals("proximo", doBot.getEstadoAtual());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao tentar definir estado nulo")
    void deveLancarExcecaoComEstadoNulo() {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> {});
        doBot.setEstados(estados);

        assertThrows(IllegalArgumentException.class, () -> doBot.setEstadoAtual(null));
    }

    @Test
    @DisplayName("Deve lançar EstadoInvalidoException ao tentar definir estado inexistente")
    void deveLancarExcecaoComEstadoInexistente() {
        Map<String, BotStateMethod> estados = new HashMap<>();
        estados.put("main", contexto -> {});
        doBot.setEstados(estados);

        assertThrows(EstadoInvalidoException.class, () -> doBot.setEstadoAtual("nao_existe"));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException se o estado atual não foi inicializado")
    void deveLancarExcecaoSeEstadoAtualForNulo() {
        assertThrows(IllegalStateException.class, () -> doBot.getEstadoAtual());
    }

    @Test
    @DisplayName("Deve permitir adicionar mensagem de boas vindas inicial")
    void deveAdicionarMensagemInicial() {
        doBot.setMensagemInicial("Bem-vindo!");

        List<Mensagem> mensagens = doBot.getMensagens();
        assertEquals(1, mensagens.size());
        assertEquals(Autor.BOT, mensagens.get(0).autor());
        assertEquals("Bem-vindo!", mensagens.get(0).conteudo());
    }
}
