package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

public class Main {
    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    final ScheduledExecutorService executorService = Executors
                            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
                    final ThreadLocalRandom random = ThreadLocalRandom.current();

                    final ActorRef<Mail.Command> mail = context.spawn(Mail.create(new ArrayDeque<>()), "Mail");
                    context.watch(mail);

                    for (int i = 1; i <= 2; i++) {
                        final IntSupplier generator = createIntGenerator();
                        final ActorRef<Writer.Command> writer = context.spawn(Writer.create(), "Writer" + i);
                        executorService.scheduleAtFixedRate(() -> {
                            writer.tell(new Writer.WriteMessage(mail, "Message" + generator.getAsInt()));
                        }, 0L, 200L + random.nextInt(50, 100), TimeUnit.MILLISECONDS);
                    }

                    for (int i = 0; i < 3; i++) {
                        final ActorRef<Reader.Command> reader = context
                                .spawn(Reader.create("Reader" + i), "Reader" + i);
                        executorService.scheduleAtFixedRate(() -> {
                            reader.tell(new Reader.ReadMessage(mail));
                        }, 0L, 100L + random.nextInt(50, 100), TimeUnit.MILLISECONDS);
                    }

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
