package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.practice.useakka.actors.InMemoryAdditionalDatabase;
import com.practice.useakka.actors.InMemoryDishDatabase;

public class RootModule extends AbstractModule {

    @Provides
    public static ActorSystem<InMemoryDishDatabase.Command> inMemoryDishDatabaseSystem() {
        return ActorSystem.create(InMemoryDishDatabase.create(), "InMemoryDishDatabaseSystem");
    }

    @Provides
    public static ActorSystem<InMemoryAdditionalDatabase.Command> inMemoryAdditionalDatabaseSystem() {
        return ActorSystem.create(InMemoryAdditionalDatabase.create(), "InMemoryAdditionalDatabaseSystem");
    }

}
