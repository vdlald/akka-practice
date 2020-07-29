package com.practice.useakka;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class DinnerTable {

    public interface Command {
    }

    public enum Run implements Command {
        INSTANCE
    }

    private final MutableList<ActorRef<Philosophy.Command>> philosophies;
    private final MutableList<ActorRef<Fork.Command>> forks;

    public static Behavior<Command> create(int size) {
        return Behaviors.setup(context -> new DinnerTable(context, size).dinerTable());
    }

    private final ActorContext<DinnerTable.Command> context;

    public DinnerTable(ActorContext<Command> context, int size) {
        this.context = context;
        forks = Lists.mutable.empty();
        philosophies = Lists.mutable.empty();

        for (int i = 0; i < size; i++) {
            final String name = "Fork-" + (i + 1);
            final ActorRef<Fork.Command> fork = context.spawn(Fork.create(name), name);
            forks.add(fork);
        }

        for (int i = 0; i < size - 1; i++) {
            final ActorRef<Fork.Command> fork1 = forks.get(i);
            ActorRef<Fork.Command> fork2 = forks.get(i + 1);
            final String name = "Philosophy-" + (i + 1);
            final ActorRef<Philosophy.Command> philosophy = context.spawn(
                    Philosophy.create(fork1, fork2, name), name);
            philosophies.add(philosophy);
        }
        int j = size - 1;
        final ActorRef<Fork.Command> fork1 = forks.get(j);
        ActorRef<Fork.Command> fork2 = forks.get(0);
        final String name = "Philosophy-" + (j + 1);
        final ActorRef<Philosophy.Command> philosophy = context.spawn(
                Philosophy.create(fork2, fork1, name), name);
        philosophies.add(philosophy);
    }


    public Behavior<Command> dinerTable() {
        return Behaviors.receive(Command.class)
                .onMessageEquals(Run.INSTANCE, this::onRun)
                .build();
    }

    private Behavior<Command> onRun() {
        philosophies.forEach(philosophyActor -> philosophyActor.tell(Philosophy.PhilosophyCommand.THINKING));
        return Behaviors.same();
    }

}
