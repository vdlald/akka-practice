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

public class Mail {

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

    private final ActorContext<Mail.Command> context;

    public static Behavior<Command> create(Queue<String> messages) {
        return Behaviors.setup(context -> new Mail(context).mail(messages));
    }

    public Mail(ActorContext<Command> context) {
        this.context = context;
    }


    public Behavior<Command> mail(Queue<String> messages) {
        return Behaviors.receive(Command.class)
                .onMessage(GetMessage.class, command -> onGetMessage(command, messages))
                .onMessage(PushMessage.class, command -> onPushMessage(command, messages))
                .build();
    }

    private Behavior<Command> onGetMessage(GetMessage m, Queue<String> messages) {
        if (messages.isEmpty()) {
            m.replyTo.tell(new MessageRespond("No messages"));
        } else {
            m.replyTo.tell(new MessageRespond(messages.remove()));
        }
        return Behaviors.same();
    }

    private Behavior<Command> onPushMessage(PushMessage m, Queue<String> messages) {
        messages.add(m.message);
        return Behaviors.same();
    }

}
