package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Manager extends AbstractBehavior<Manager.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ServeClient implements Command {
        public final Client client;
    }

    private final ActorRef<Barber.Command> barber;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<Manager.Command> create(
            ActorRef<Barber.Command> barber, ActorRef<WaitingRoom.Command> waitingRoom
    ) {
        return Behaviors.setup(context -> new Manager(context, barber, waitingRoom));
    }

    public Manager(
            ActorContext<Command> context, ActorRef<Barber.Command> barber, ActorRef<WaitingRoom.Command> waitingRoom
    ) {
        super(context);
        this.barber = barber;
        this.waitingRoom = waitingRoom;
    }

    @Override
    public Receive<Manager.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ServeClient.class, this::onServeClient)
                .build();
    }

    private Behavior<Command> onServeClient(ServeClient m) {
        waitingRoom.tell(new WaitingRoom.AddClient(m.client));
        return this;
    }
}
