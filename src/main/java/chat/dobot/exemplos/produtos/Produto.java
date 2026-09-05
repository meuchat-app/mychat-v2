package chat.dobot.exemplos.produtos;

import chat.dobot.bot.annotations.Entidade;
import chat.dobot.bot.annotations.Id;

@Entidade
public record Produto(@Id int id, String nome, double preco) {
    @Override
    public String toString() {
        return nome + " - R$" + String.format("%.2f", preco);
    }
}
