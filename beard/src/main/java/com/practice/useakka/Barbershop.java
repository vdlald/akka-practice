package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Barbershop extends AbstractBehavior<Barbershop.Command> {

    public interface Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Barbershop::new);
    }

    public Barbershop(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .build();
    }

}
