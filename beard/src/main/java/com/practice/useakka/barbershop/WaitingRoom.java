package com.practice.useakka.barbershop;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class WaitingRoom extends AbstractBehavior<WaitingRoom.Command> {

    public interface Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WaitingRoom::new);
    }

    public WaitingRoom(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .build();
    }

}

