package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Fork extends AbstractBehavior<Fork.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class Peek implements Command {
        public final ActorRef<Response> replyTo;
        public final ActorRef<Event> replyToWhenRelease;
    }

    public enum Release implements Command {
        INSTANCE
    }

    public interface Response {
    }

    @RequiredArgsConstructor
    public static class ForkResponse implements Response {
        public final boolean isPicked;
        public final String forkName;
    }

    public interface Event {
    }

    @RequiredArgsConstructor
    public static class ForkReleased implements Event {
        public final ActorRef<Command> fork;
        public final String forkName;
    }

    public static Behavior<Command> create(String  name) {
        return Behaviors.setup(context -> new Fork(context, name));
    }

    private boolean isFree = true;
    private ActorRef<Event> nextPhilosophy;
    private final String name;

    public Fork(ActorContext<Command> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Peek.class, this::onPeek)
                .onMessageEquals(Release.INSTANCE, this::onRelease)
                .build();
    }

    private Behavior<Command> onRelease() {
        if (nextPhilosophy != null) {
            nextPhilosophy.tell(new ForkReleased(getContext().getSelf(), name));
            nextPhilosophy = null;
        } else {
            isFree = true;
        }
        return this;
    }

    private Behavior<Command> onPeek(Peek peek) {
        ForkResponse msg;
        if (isFree) {
            isFree = false;
            msg = new ForkResponse(true, name);
        } else {
            msg = new ForkResponse(false, name);
            nextPhilosophy = peek.replyToWhenRelease;
        }
        peek.replyTo.tell(msg);
        return this;
    }

}