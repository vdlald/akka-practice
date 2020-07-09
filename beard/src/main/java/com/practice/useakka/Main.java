package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

public class Main {

    private static final int waitingRoomSize = 5;

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    final ScheduledExecutorService executorService = Executors
                            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
                    final ThreadLocalRandom random = ThreadLocalRandom.current();

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "Main");
    }

    private static IntSupplier createIntGenerator() {
        return new IntSupplier() {
            int i = 0;

            @Override
            public int getAsInt() {
                i++;
                return i;
            }
        };
    }
}
