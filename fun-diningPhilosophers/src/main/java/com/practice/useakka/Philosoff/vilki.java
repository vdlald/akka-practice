package com.practice.useakka.Philosoff;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.practice.useakka.pojo.Client;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.UUID;

public class vilki {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ReceiveClient implements Command {
        public final Client client;
    }

    public interface Event {
    }

    public enum ReceiveClientEvent implements Event {
        INSTANCE
    }

    private final ActorContext<vilki.Command> context;
    private final ActorRef<philosoff.Command> Philosoff;
    private final ActorRef<waiting.Command> Waiting;

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new vilki(context).vilki());
    }

    public vilki(ActorContext<Command> context) {
        this.context = context;
        this.Waiting = context.spawn(waiting.create(new ArrayDeque<>()), "waiting-" + UUID.randomUUID());
        this.Philosoff = context.spawn(philosoff.create(Waiting), "Philosoff-" + UUID.randomUUID());
    }

    private Behavior<Command> vilki() {
        return Behaviors.receive(Command.class)
                .onMessage(ReceiveClient.class, this::onReceiveClient)
                .build();
    }

    private Behavior<Command> onReceiveClient(ReceiveClient m) {
        context.getLog().info("вилка лежит{}", m.client);
        Waiting.tell(new waiting.AddClient(m.client));
        Philosoff.tell(new philosoff.vilkiEventWrapper(ReceiveClientEvent.INSTANCE));
        return Behaviors.same();
    }

}