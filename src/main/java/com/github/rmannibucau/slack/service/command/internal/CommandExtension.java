package com.github.rmannibucau.slack.service.command.internal;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

public class CommandExtension implements Extension {

    private final Map<String, Bean<?>> commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Map<String, Function<Message, String>> commandInstances = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Collection<Throwable> errors = new ArrayList<>();

    private final Collection<CreationalContext<?>> contexts = new ArrayList<>();

    private final ParameterizedType commandType = new ParameterizedType() {

        private final Type[] arguments = new Type[] { Message.class, String.class };

        @Override
        public Type[] getActualTypeArguments() {
            return arguments;
        }

        @Override
        public Type getRawType() {
            return Function.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    private Function<Message, String> defaultCommand = m -> "Je ne comprends pas ! :sleepy: \nSi tu veux m'améliorer, fait une PR sur https://github.com/rmannibucau/slack-bot";

    public Stream<String> commandNames() {
        return commandInstances.keySet().stream();
    }

    public Function<Message, String> findCommand(final String command) {
        return ofNullable(commandInstances.get(command))
                .orElseGet(() -> commandInstances.getOrDefault(command.trim().split(" ")[0], defaultCommand));
    }

    void onProcessBean(@Observes final ProcessBean<?> bean) {
        final Command command = bean.getAnnotated().getAnnotation(Command.class);
        if (command != null && commands.putIfAbsent(command.value(), bean.getBean()) != null) {
            errors.add(new IllegalArgumentException("Ambiguous command: " + command.value()));
        }
    }

    void afterDeploymentValidation(@Observes final AfterDeploymentValidation afterDeploymentValidation,
            final BeanManager beanManager) {
        if (errors.isEmpty()) {
            errors.forEach(afterDeploymentValidation::addDeploymentProblem);
        } else {
            commandInstances.putAll(commands.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> {
                final Bean<?> bean = entry.getValue();
                final CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
                if (!beanManager.isNormalScope(bean.getScope())) {
                    contexts.add(creationalContext);
                }
                return Function.class.cast(beanManager.getReference(
                        beanManager.resolve(beanManager.getBeans(bean.getBeanClass(),
                                bean.getQualifiers().toArray(new Annotation[bean.getQualifiers().size()]))),
                        commandType, creationalContext));
            })));
            commands.clear();
        }
    }

    void beforeShutdown(@Observes final BeforeShutdown beforeShutdown) {
        contexts.forEach(CreationalContext::release);
    }
}
