package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Manager extends AbstractBehavior<Manager.Command> {

    public interface Command {
    }

    public static Behavior<Manager.Command> create() {
        return Behaviors.setup(Manager::new);
    }

    public Manager(ActorContext<Manager.Command> context) {
        super(context);
    }

    @Override
    public Receive<Manager.Command> createReceive() {
        return newReceiveBuilder()
                .build();
    }
}
