package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Queue;

public class Mail extends AbstractBehavior<Mail.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class PushMessage implements Command {
        public final String message;
    }

    @RequiredArgsConstructor
    public static class GetMessage implements Command {
        public final ActorRef<Response> replyTo;
    }

    public interface Response {
    }

    @RequiredArgsConstructor
    public static class MessageRespond implements Response {
        public final String message;
    }

    private final Queue<String> messages;

    public static Behavior<Command> create() {
        return Behaviors.setup(Mail::new);
    }

    public Mail(ActorContext<Command> context) {
        super(context);
        messages = new ArrayDeque<>();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetMessage.class, this::onGetMessage)
                .onMessage(PushMessage.class, this::onPushMessage)
                .build();
    }

    private Behavior<Command> onGetMessage(GetMessage m) {
        if (messages.isEmpty()) {
            m.replyTo.tell(new MessageRespond("No messages"));
        } else {
            m.replyTo.tell(new MessageRespond(messages.remove()));
        }
        return this;
    }

    private Behavior<Command> onPushMessage(PushMessage m) {
        messages.add(m.message);
        return this;
    }

}
