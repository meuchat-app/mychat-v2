package chat.dobot.bot;

import chat.dobot.bot.service.DoBotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

record TestEntity(int id, String nome) {}

class ContextoTest {

    @Test
    @DisplayName("Deve inicializar contexto com estado em minúsculas e mensagem do usuário")
    void deveInicializarContexto() {
        Contexto contexto = new Contexto("Olá", "MAIN", new HashMap<>());

        assertEquals("Olá", contexto.getMensagemUsuario());
        assertEquals("main", contexto.getEstado());
        assertEquals(List.of(), contexto.getRespostas());
    }

    @Test
    @DisplayName("Deve alterar estado do contexto")
    void deveMudarEstado() {
        Contexto contexto = new Contexto("Olá", "main", new HashMap<>());

        contexto.mudarEstado("cadastrar");

        assertEquals("cadastrar", contexto.getEstado());
    }

    @Test
    @DisplayName("Deve adicionar respostas ao contexto")
    void deveAdicionarRespostas() {
        Contexto contexto = new Contexto("Olá", "main", new HashMap<>());

        contexto.responder("Primeira resposta");
        contexto.responder("Segunda resposta");

        assertEquals(List.of("Primeira resposta", "Segunda resposta"), contexto.getRespostas());
    }

    @Test
    @DisplayName("Deve retornar serviço tipado quando encontrado")
    void deveRetornarServicoTipado() {
        Map<String, DoBotService<Record>> servicos = new HashMap<>();
        StubTestEntityService service = new StubTestEntityService();

        @SuppressWarnings("unchecked")
        DoBotService<Record> servicoRecord = (DoBotService<Record>) (DoBotService<?>) service;
        servicos.put("TestEntity", servicoRecord);

        Contexto contexto = new Contexto("Olá", "main", servicos);

        DoBotService<TestEntity> servicoObtido = contexto.getServico(TestEntity.class);
        assertEquals(service, servicoObtido);
    }

    @Test
    @DisplayName("Deve lançar DoBotException quando serviço não for encontrado")
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        Contexto contexto = new Contexto("Olá", "main", new HashMap<>());

        org.junit.jupiter.api.Assertions.assertThrows(DoBotException.class, () -> {
            contexto.getServico(TestEntity.class);
        });
    }

    private static class StubTestEntityService extends DoBotService<TestEntity> {
        protected StubTestEntityService() {
            super(TestEntity.class, null);
        }
    }
}
