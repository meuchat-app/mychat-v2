package chat.dobot.exemplos.telegram;

import chat.dobot.bot.Contexto;
import chat.dobot.bot.DoBotConfig;
import chat.dobot.bot.DoBotTelegramApp;
import chat.dobot.bot.annotations.Config;
import chat.dobot.bot.annotations.DoBotChat;
import chat.dobot.bot.annotations.EstadoChat;

/** Exemplo de chatbot DoBot executado pelo canal Telegram. */
@DoBotChat(
        id = "telegramHello",
        nome = "Telegram Alô Mundo",
        descricao = "Exemplo de integração do DoBot.chat com o Telegram"
)
public class TelegramHelloWorldBot {

    public static void main(String[] args) throws Exception {
        String token = "8640441725:AAGKzSKmK-kcpagoDW7MHB7B03L9cqIhflY";
        String username = "dobotchatbot";

        DoBotTelegramApp app = DoBotTelegramApp.novoBot(token, username, "telegramHello");
        app.ativarExemplos();
        app.start();
    }

    @Config
    public void config(DoBotConfig config) {
        config.setMensagemInicial("Olá! Sou um bot DoBot.chat no Telegram. Envie uma mensagem ou use /start.");
    }

    @EstadoChat(inicial = true)
    public void inicio(Contexto contexto) {
        if ("/start".equalsIgnoreCase(contexto.getMensagemUsuario().trim())) {
            contexto.responder("Olá! Eu uso @DoBotChat e @EstadoChat para funcionar no Telegram.");
            contexto.responder("Envie qualquer texto e eu repetirei a mensagem.");
            return;
        }

        contexto.responder("Você disse: " + contexto.getMensagemUsuario());
    }

    private static String obterVariavelObrigatoria(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente " + nome + " antes de iniciar o bot.");
        }
        return valor;
    }
}