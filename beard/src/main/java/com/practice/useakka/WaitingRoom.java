package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Queue;

public class WaitingRoom extends AbstractBehavior<WaitingRoom.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class AddClient implements Command {
        public final Client client;
    }

    private final Queue<Client> clients;
    private final int size;

    public static Behavior<Command> create(int size) {
        return Behaviors.setup(context -> new WaitingRoom(context, size));
    }

    public WaitingRoom(ActorContext<Command> context, int size) {
        super(context);
        clients = new ArrayDeque<>();
        this.size = size;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddClient.class, this::onAddClient)
                .build();
    }

    private Behavior<Command> onAddClient(AddClient command) {
        if (clients.size() < size) {
            clients.add(command.client);
        } else {
            getContext().getLog().info("Комната ожидания переполненна!");
        }
        return this;
    }
}
