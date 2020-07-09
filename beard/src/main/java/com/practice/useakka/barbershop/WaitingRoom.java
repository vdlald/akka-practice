package com.practice.useakka.barbershop;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.practice.useakka.pojo.Client;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

class WaitingRoom extends AbstractBehavior<WaitingRoom.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class AddClient implements Command {
        public final Client client;
    }

    @RequiredArgsConstructor
    public static class GetClient implements Command {
        public final ActorRef<Response> replyTo;
    }

    public interface Response {
    }

    @RequiredArgsConstructor
    public static class ClientResponse implements Response {
        public final Optional<Client> client;
    }

    private final Queue<Client> clients;

    public static Behavior<Command> create() {
        return Behaviors.setup(WaitingRoom::new);
    }

    public WaitingRoom(ActorContext<Command> context) {
        super(context);
        clients = new ArrayDeque<>();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddClient.class, this::onAddClient)
                .onMessage(GetClient.class, this::onGetClient)
                .build();
    }

    private Behavior<Command> onGetClient(GetClient m) {
        if (clients.isEmpty()) {
            getContext().getLog().info("Клиентов нет!");
        } else {
            m.replyTo.tell(new ClientResponse(Optional.of(clients.remove())));
        }
        return this;
    }

    private Behavior<Command> onAddClient(AddClient m) {
        clients.add(m.client);
        return this;
    }

}

