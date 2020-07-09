package com.practice.useakka.barbershop;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.UUID;

public class Barbershop extends AbstractBehavior<Barbershop.Command> {

    public interface Command {
    }

    public interface Event {
    }

    private final ActorRef<Barber.Command> barber;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<Command> create() {
        return Behaviors.setup(Barbershop::new);
    }

    public Barbershop(ActorContext<Command> context) {
        super(context);
        this.barber = context.spawn(Barber.create(), "Barber-" + UUID.randomUUID());
        this.waitingRoom = context.spawn(WaitingRoom.create(), "WaitingRoom-" + UUID.randomUUID());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .build();
    }

}
