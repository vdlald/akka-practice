package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Barber extends AbstractBehavior<Barber.Command> {

    public interface Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Barber::new);
    }

    public Barber(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .build();
    }
}
