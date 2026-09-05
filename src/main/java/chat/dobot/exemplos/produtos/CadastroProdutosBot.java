package chat.dobot.exemplos.produtos;


import chat.dobot.bot.Contexto;
import chat.dobot.bot.DoBotChatApp;
import chat.dobot.bot.DoBotConfig;
import chat.dobot.bot.annotations.Config;
import chat.dobot.bot.annotations.DoBotChat;
import chat.dobot.bot.annotations.EstadoChat;

@DoBotChat(id = "cadProdutos", nome = "Cadastro de Produtos", descricao = "Bot que Cadastra e lista produtos")
public class CadastroProdutosBot {

    public static void main(String[] args) {
        DoBotChatApp meubot = DoBotChatApp.novoBot();
        meubot.ativarExemplos();
        meubot.start(8083,8084);

    }

    @Config
    public void config(DoBotConfig config){
        config.setMensagemInicial("👋 Olá! Eu sou o chatbot De Cadastro de produtos!");
    }

    @EstadoChat(inicial = true)
    public void aloMundo(Contexto chat) {
        chat.responder("Bem-vindo ao exemplo de cadastro de produtos!");
        chat.responder("Menu: \n1 - Cadastrar produto\n2 - Listar produtos\n3 - Sair");
        chat.mudarEstado("opcao");
    }

    @EstadoChat(estado = "menu")
    public void menu(Contexto chat) {
        chat.responder("Menu: \n1 - Cadastrar produto\n2 - Listar produtos\n3 - Sair");
        chat.mudarEstado("opcao");
    }

    @EstadoChat(estado = "opcao")
    public void opcao(Contexto chat) {
        String msg = chat.getMensagemUsuario().trim();
        switch (msg) {
            case "1" -> {
                chat.responder("Envie o produto no formato: nome;preco (ex: Caneca;12.50)");
                chat.mudarEstado("cadastrar");
            }
            case "2" -> {
                var serv = chat.getServico(Produto.class);
                try {
                    var todos = serv.buscarTodos();
                    if (todos == null || todos.isEmpty()) {
                        chat.responder("Nenhum produto cadastrado.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Produtos cadastrados:\n");
                        for (var p : todos) {
                            sb.append(p.toString()).append("\n");
                        }
                        chat.responder(sb.toString());
                    }
                } catch (Exception e) {
                    chat.responder("Falha ao listar produtos: " + e.getMessage());
                }
                chat.mudarEstado("main");
            }
            case "3" -> {
                chat.responder("Até logo!");
                chat.mudarEstado("main");
            }
            default -> chat.responder("Opção inválida. Digite 1, 2 ou 3.");
        }
    }

    @EstadoChat(estado = "cadastrar")
    public void cadastrar(Contexto chat) {
        String msg = chat.getMensagemUsuario();
        String[] parts = msg.split(";");
        if (parts.length < 2) {
            chat.responder("Formato inválido. Use: nome;preco (ex: Caneca;12.50)");
            return;
        }
        String nome = parts[0].trim();
        String precoStr = parts[1].trim().replace(',', '.');
        try {
            double preco = Double.parseDouble(precoStr);
            var serv = chat.getServico(Produto.class);
            Produto p = new Produto(0, nome, preco);
            serv.salvar(p);
            chat.responder("Produto cadastrado: " + p.toString());
        } catch (NumberFormatException nfe) {
            chat.responder("Preço inválido. Use um número (ex: 12.50)");
            return;
        } catch (Exception e) {
            chat.responder("Falha ao salvar produto: " + e.getMessage());
        }
        chat.mudarEstado("main");
    }


}
