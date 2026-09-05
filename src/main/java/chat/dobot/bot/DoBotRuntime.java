package chat.dobot.bot;

import chat.dobot.bot.domain.DoBot;
import chat.dobot.bot.service.DoBotService;

import java.util.Map;

/** Núcleo de execução compartilhado pelos adaptadores de canal do DoBot. */
public class DoBotRuntime {

    private final Map<String, DoBot> bots;
    private final Map<String, DoBotService<Record>> servicos;

    public DoBotRuntime(Map<String, DoBot> bots, Map<String, DoBotService<Record>> servicos) {
        this.bots = bots;
        this.servicos = servicos;
    }

    public Map<String, DoBot> getBots() {
        return bots;
    }

    public Map<String, DoBotService<Record>> getServicos() {
        return servicos;
    }

    public DoBot novaSessao(String botId) {
        DoBot bot = bots.get(botId);
        if (bot == null) {
            throw new DoBotException("Bot não encontrado: " + botId);
        }
        return bot.novaSessao();
    }

    public Contexto processar(DoBot sessao, String mensagem) throws Exception {
        Contexto contexto = new Contexto(mensagem, sessao.getEstadoAtual(), servicos);
        sessao.receberMensagem(contexto);
        return contexto;
    }
}