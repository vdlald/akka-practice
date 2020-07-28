package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

public class Main {

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    final ActorRef<DinnerTable.Command> dinnerTable = context.spawn(
                            DinnerTable.create(5), "DinnerTable");

                    dinnerTable.tell(DinnerTable.Run.INSTANCE);

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "Main");
    }
}
