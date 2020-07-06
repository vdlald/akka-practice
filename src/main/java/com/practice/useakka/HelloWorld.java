package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class HelloWorld extends AbstractBehavior<HelloWorld.Command> {

    interface Command {
    }

    public enum SayHello implements Command {
        INSTANCE
    }

    @RequiredArgsConstructor
    public static class ChangeMessage implements Command {
        public final String newMessage;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(HelloWorld::new);
    }

    private String message = "Hello World";

    private HelloWorld(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(SayHello.INSTANCE, this::onSayHello)
                .onMessage(ChangeMessage.class, this::onChangeMessage)
                .build();
    }

    private Behavior<Command> onChangeMessage(ChangeMessage command) {
        message = command.newMessage;
        return this;
    }

    private Behavior<Command> onSayHello() {
        System.out.println(message);
        return this;
    }
}
