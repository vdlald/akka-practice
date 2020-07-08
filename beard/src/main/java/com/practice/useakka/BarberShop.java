package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class BarberShop extends AbstractBehavior<BarberShop.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ReceiveClient implements Command {
        public final Client client;
    }

    private final ActorRef<Manager.Command> manager;
    private final ActorRef<Barber.Command> barber;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<BarberShop.Command> create(
            ActorRef<Manager.Command> manager,
            ActorRef<Barber.Command> barber,
            ActorRef<WaitingRoom.Command> waitingRoom
    ) {
        return Behaviors.setup(context -> new BarberShop(context, manager, barber, waitingRoom));
    }

    public BarberShop(
            ActorContext<Command> context,
            ActorRef<Manager.Command> manager,
            ActorRef<Barber.Command> barber,
            ActorRef<WaitingRoom.Command> waitingRoom
    ) {
        super(context);
        this.manager = manager;
        this.barber = barber;
        this.waitingRoom = waitingRoom;
    }

    @Override
    public Receive<BarberShop.Command> createReceive() {
        return newReceiveBuilder()
                .build();
    }

}
