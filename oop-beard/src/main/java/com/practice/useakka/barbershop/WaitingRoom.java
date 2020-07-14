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

class WaitingRoom {

    public interface Command {
    }
    private final ActorContext<Command> context;

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
        return Behaviors.setup(context -> new WaitingRoom(context).waitingRoom());
    }


    public WaitingRoom(ActorContext<Command> context) {
        this.context = context;
        clients = new ArrayDeque<>();
    }

    private Behavior<Command> waitingRoom() {
        return Behaviors.receive(Command.class)
                .onMessage(AddClient.class, this::onAddClient)
                .onMessage(GetClient.class, this::onGetClient)
                .build();
    }

    private Behavior<Command> onGetClient(GetClient m) {
        if (clients.isEmpty()) {
            context.getLog().info("Клиентов нет!");
        } else {
            m.replyTo.tell(new ClientResponse(Optional.of(clients.remove())));
        }
        return Behaviors.same();
    }

    private Behavior<Command> onAddClient(AddClient m) {
        clients.add(m.client);
        return Behaviors.same();
    }

}

