package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.ThreadLocalRandom;

public class Philosophy extends AbstractBehavior<Philosophy.Command> {

    public interface Command {
    }

    public enum PhilosophyCommand implements Command {
        THINKING,
        WAIT_LEFT,
        WAIT_RIGHT,
        EATING,
        DONE
    }

    @RequiredArgsConstructor
    public static class WrappedForkResponse implements Command {
        public final Fork.Response response;
    }

    @RequiredArgsConstructor
    public static class WrappedForkEvent implements Command {
        public final Fork.Event event;
    }

    public static Behavior<Command> create(
            ActorRef<Fork.Command> leftFork, ActorRef<Fork.Command> rightFork, String name
    ) {
        return Behaviors.setup(context -> new Philosophy(context, leftFork, rightFork, name));
    }

    private final ActorRef<Fork.Response> forkResponseAdapter;
    private final ActorRef<Fork.Event> forkEventAdapter;
    private final ActorRef<Fork.Command> leftFork;
    private final ActorRef<Fork.Command> rightFork;
    private int forklift = 0;
    private final String name;

    public Philosophy(
            ActorContext<Command> context,
            ActorRef<Fork.Command> leftFork, ActorRef<Fork.Command> rightFork,
            String name
    ) {
        super(context);
        forkResponseAdapter = context.messageAdapter(Fork.Response.class, WrappedForkResponse::new);
        forkEventAdapter = context.messageAdapter(Fork.Event.class, WrappedForkEvent::new);
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.name = name;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(PhilosophyCommand.THINKING, this::onThinking)
                .onMessageEquals(PhilosophyCommand.WAIT_RIGHT, this::onWaitRight)
                .onMessageEquals(PhilosophyCommand.WAIT_LEFT, this::onWaitLeft)
                .onMessageEquals(PhilosophyCommand.EATING, this::onEating)
                .onMessageEquals(PhilosophyCommand.DONE, this::onDone)
                .onMessage(WrappedForkResponse.class, this::onWrappedForkResponse)
                .onMessage(WrappedForkEvent.class, this::onWrappedForkEvent)
                .build();
    }

    private Behavior<Command> onDone() {
        rightFork.tell(Fork.Release.INSTANCE);
        leftFork.tell(Fork.Release.INSTANCE);
        forklift = 0;
        log("Finish eating.");
        getContext().getSelf().tell(PhilosophyCommand.THINKING);
        return this;
    }

    private Behavior<Command> onWrappedForkEvent(WrappedForkEvent event) {
        if (event.event instanceof Fork.ForkReleased) {
            Fork.ForkReleased forkReleased = (Fork.ForkReleased) event.event;
            log("Picked up fork: " + forkReleased.forkName);
            forklift++;
            checkForkLift();
        }
        return this;
    }

    private Behavior<Command> onWrappedForkResponse(WrappedForkResponse response) {
        if (response.response instanceof Fork.ForkResponse) {
            final Fork.ForkResponse forkResponse = (Fork.ForkResponse) response.response;
            if (forkResponse.isPicked) {
                log("Picked up fork: " + forkResponse.forkName);
                forklift++;
            }
            checkForkLift();
        }
        return this;
    }

    @SneakyThrows
    private Behavior<Command> onEating() {
        log("Eating...");
        final int eatTime = ThreadLocalRandom.current().nextInt(1000, 1500);
        Thread.sleep(eatTime);
        getContext().getSelf().tell(PhilosophyCommand.DONE);
        return this;
    }

    private Behavior<Command> onWaitLeft() {
        log("Waiting left fork...");
        leftFork.tell(new Fork.Peek(forkResponseAdapter, forkEventAdapter));
        return this;
    }

    private Behavior<Command> onWaitRight() {
        log("Waiting right fork...");
        rightFork.tell(new Fork.Peek(forkResponseAdapter, forkEventAdapter));
        return this;
    }

    @SneakyThrows
    private Behavior<Command> onThinking() {
        log("Thinking...");
        final int thinkTime = ThreadLocalRandom.current().nextInt(1500, 2000);
        Thread.sleep(thinkTime);
        getContext().getSelf().tell(PhilosophyCommand.WAIT_RIGHT);
        getContext().getSelf().tell(PhilosophyCommand.WAIT_LEFT);
        return this;
    }

    private void log(String msg) {
        getContext().getLog().info("{}: {}", name, msg);
    }

    private void checkForkLift() {
        if (forklift == 2) {
            getContext().getSelf().tell(PhilosophyCommand.EATING);
        }
    }

}