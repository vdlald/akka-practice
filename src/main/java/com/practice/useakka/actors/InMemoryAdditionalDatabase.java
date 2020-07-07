package com.practice.useakka.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.practice.useakka.models.Additional;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import java.util.Optional;

public class InMemoryAdditionalDatabase extends AbstractBehavior<InMemoryAdditionalDatabase.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class PutAdditional implements Command {
        public final Additional additional;
    }

    @RequiredArgsConstructor
    public static class GetAdditional implements Command {
        public final String name;
        public final ActorRef<RespondAdditional> replyTo;
    }

    @RequiredArgsConstructor
    public static class RespondAdditional {
        public final Optional<Additional> additional;
    }

    private final MutableMap<String, Additional> map;

    public static Behavior<Command> create() {
        return Behaviors.setup(InMemoryAdditionalDatabase::new);
    }

    public InMemoryAdditionalDatabase(ActorContext<Command> context) {
        super(context);
        map = Maps.mutable.empty();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PutAdditional.class, this::onPutAdditional)
                .onMessage(GetAdditional.class, this::onGetAdditional)
                .build();
    }

    private Behavior<Command> onGetAdditional(GetAdditional command) {
        final Optional<Additional> additional = Optional.ofNullable(map.get(command.name));
        command.replyTo.tell(new RespondAdditional(additional));
        return this;
    }

    private Behavior<Command> onPutAdditional(PutAdditional command) {
        final Additional additional = command.additional;
        map.put(additional.getName(), additional);
        return this;
    }
}
