package chat.dobot.bot;

import chat.dobot.bot.domain.DoBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador de canal Telegram usando Long Polling. */
public class DoBotTelegramApp extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(DoBotTelegramApp.class);

    private final String token;
    private final String username;
    private final String botId;
    private final Map<Long, DoBot> sessoes = new ConcurrentHashMap<>();
    private DoBotRuntime runtime;
    private boolean carregarExemplos;

    private DoBotTelegramApp(String token, String username, String botId) {
        this.token = token;
        this.username = username;
        this.botId = botId;
    }

    public static DoBotTelegramApp novoBot(String token, String username, String botId) {
        return new DoBotTelegramApp(token, username, botId);
    }

    public void ativarExemplos() {
        carregarExemplos = true;
    }

    public void start() throws TelegramApiException {
        start(8082);
    }

    public void start(int portaH2) throws TelegramApiException {
        DoBotChatApp app = DoBotChatApp.novoBot();
        if (carregarExemplos) {
            app.ativarExemplos();
        }
        runtime = app.prepararRuntime(portaH2);
        new TelegramBotsApi(DefaultBotSession.class).registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (runtime == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        DoBot sessao = sessoes.computeIfAbsent(chatId, id -> runtime.novaSessao(botId));
        try {
            if (sessao.getMensagens().size() == 1 && !sessao.getConfig().getMensagemInicial().isBlank()) {
                enviar(chatId, sessao.getConfig().getMensagemInicial());
            }
            Contexto contexto = runtime.processar(sessao, update.getMessage().getText());
            for (String resposta : contexto.getRespostas()) {
                execute(new SendMessage(Long.toString(chatId), resposta));
            }
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem do Telegram para o chat {}", chatId, e);
            enviar(chatId, "Ocorreu um erro ao processar sua mensagem.");
        }
    }

    private void enviar(long chatId, String texto) {
        try {
            execute(new SendMessage(Long.toString(chatId), texto));
        } catch (TelegramApiException e) {
            logger.error("Erro ao responder ao chat do Telegram {}", chatId, e);
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}