package chat.dobot.exemplos.helloworld;

import chat.dobot.bot.Contexto;
import chat.dobot.bot.DoBotConfig;
import chat.dobot.bot.service.DoBotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorldBotTest {

    @Test
    @DisplayName("Deve responder com Alô concatenado com a mensagem do usuário")
    void aloMundoDeveResponderComOlaMaisMensagem() {
        HelloWorldBot bot = new HelloWorldBot();
        Map<String, DoBotService<Record>> servicos = new HashMap<>();
        Contexto contexto = new Contexto("usuário", "main", servicos);

        bot.aloMundo(contexto);

        assertEquals(List.of("Alô usuário"), contexto.getRespostas());
    }

    @Test
    @DisplayName("Deve definir a mensagem inicial na configuração")
    void configDeveDefinirMensagemInicial() {
        HelloWorldBot bot = new HelloWorldBot();
        DoBotConfig config = new DoBotConfig();

        bot.config(config);

        assertEquals("👋 Olá! Eu sou o chatbot Alô Mundo! Escreva qualquer coisa e responderei com `Alô`.", config.getMensagemInicial());
    }
}

