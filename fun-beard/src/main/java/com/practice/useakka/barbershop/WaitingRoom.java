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

    private final ActorContext<Command> context;

    public static Behavior<Command> create(Queue<Client> clients) {
        return Behaviors.setup(context -> new WaitingRoom(context).waitingRoom(clients));
    }


    public WaitingRoom(ActorContext<Command> context) {
        this.context = context;
    }

    private Behavior<Command> waitingRoom(Queue<Client> clients) {
        return Behaviors.receive(Command.class)
                .onMessage(AddClient.class, command -> onAddClient(command, clients))
                .onMessage(GetClient.class, command -> onGetClient(command, clients))
                .build();
    }

    private Behavior<Command> onGetClient(GetClient m, Queue<Client> clients) {
        if (clients.isEmpty()) {
            context.getLog().info("Клиентов нет!");
        } else {
            m.replyTo.tell(new ClientResponse(Optional.of(clients.remove())));
        }
        return Behaviors.same();
    }

    private Behavior<Command> onAddClient(AddClient m, Queue<Client> clients) {
        clients.add(m.client);
        return Behaviors.same();
    }

}

