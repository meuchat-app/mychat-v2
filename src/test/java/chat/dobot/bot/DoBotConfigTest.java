package chat.dobot.bot;

import chat.dobot.bot.domain.DoBotTema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DoBotConfigTest {

    @Test
    @DisplayName("Deve inicializar DoBotConfig com valores padrão")
    void deveInicializarComValoresPadrao() {
        DoBotConfig config = new DoBotConfig();

        assertEquals("", config.getMensagemInicial());
        assertNotNull(config.getTema());
    }

    @Test
    @DisplayName("Deve permitir definir mensagem inicial")
    void devePermitirDefinirMensagemInicial() {
        DoBotConfig config = new DoBotConfig();

        config.setMensagemInicial("Bem-vindo ao bot");

        assertEquals("Bem-vindo ao bot", config.getMensagemInicial());
    }

    @Test
    @DisplayName("Deve inicializar DoBotConfig com construtor completo")
    void deveInicializarComConstrutorCompleto() {
        DoBotTema tema = new DoBotTema();
        DoBotConfig config = new DoBotConfig("Mensagem", tema);

        assertEquals("Mensagem", config.getMensagemInicial());
        assertEquals(tema, config.getTema());
    }
}
