package chat.dobot.bot.utils;

import chat.dobot.bot.annotations.DoBot;
import chat.dobot.bot.annotations.Entidade;
import chat.dobot.bot.annotations.EstadoChat;
import chat.dobot.bot.Contexto;
import chat.dobot.bot.DoBotException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnnotationsUtil {

    public static List<Class<Record>> buscarEntidades() {
        List<Class<Record>> entidades = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
            for (Class<?> classe : scanResult.getClassesWithAnnotation(Entidade.class).getRecords().loadClasses()) {
                @SuppressWarnings("unchecked")
                Class<Record> entidade = (Class<Record>) classe;
                entidades.add(entidade);
            }
        }

        return entidades;
    }

    public static Object buscarClasseChatbot() throws Exception {
        try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
            for (Class<?> classe : scanResult.getClassesWithAnnotation(DoBot.class).loadClasses()) {
                return classe.getDeclaredConstructor().newInstance();
            }
        }

        return null;
    }

    public static Map<String, Consumer<Contexto>> mapearEstados(Object chatbotImpl) {
        Map<String, Consumer<Contexto>> estadosMap = new HashMap<>();

        for (Method metodo : chatbotImpl.getClass().getDeclaredMethods()) {
            if (metodo.isAnnotationPresent(EstadoChat.class)) {
                EstadoChat estadoChat = metodo.getAnnotation(EstadoChat.class);
                String estado = estadoChat.estado().isBlank() ? metodo.getName() : estadoChat.estado();
                validarMetodo(metodo, estado, estadosMap);

                estadosMap.put(estado.toLowerCase(), obj -> {
                    try {
                        metodo.invoke(chatbotImpl, obj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        return estadosMap;
    }

    public static String obterEstadoInicial(Object chatbotImpl) {
        String estadoInicial = null;
        for (Method method : chatbotImpl.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EstadoChat.class)) {
                EstadoChat estadoChat = method.getAnnotation(EstadoChat.class);
                if (estadoChat.inicial()) {
                    if (estadoInicial != null) {
                        throw new DoBotException("Mais de um estado inicial definido!");
                    }
                    estadoInicial = estadoChat.estado().isEmpty() ? method.getName() : estadoChat.estado();
                }
            }
        }

        if (estadoInicial != null) {
            return estadoInicial.toLowerCase();
        }
        throw new DoBotException("Nenhum estado inicial definido!");
    }

    //TODO: implementar interface nossa com um método que possui Context como parâmetro
    private static void validarMetodo(Method method, String estado, Map<String, Consumer<Contexto>> estadosMap) {
        if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].getName().equals(Contexto.class.getName())) {
            throw new DoBotException("O método '" + method.getName() + "' da classe " + method.getDeclaringClass().getName() + " está anotado com " + EstadoChat.class.getName() + " e deve conter um único parâmetro, que precisa ser do tipo " + Contexto.class.getName() + "!");
        }

        if (estadosMap.containsKey(estado.toLowerCase())) {
            throw new DoBotException("O estado '" + estado.toLowerCase() + "' não pode ser mapeado para mais de um método!");
        }
    }
}
