package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class RootModule extends AbstractModule {

    @Provides
    public static ActorSystem<HelloWorld.Command> helloWorldSystem() {
        return ActorSystem.create(HelloWorld.create(), "MySystem");
    }

}
