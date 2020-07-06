package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class App implements Runnable {

    private final ActorSystem<HelloWorld.Command> helloWorldSystem;

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new RootModule());
        App app = injector.getInstance(App.class);
        app.run();
    }

    @Override
    public void run() {
        helloWorldSystem.tell(HelloWorld.SayHello.INSTANCE);
        helloWorldSystem.tell(HelloWorld.SayHello.INSTANCE);
        helloWorldSystem.tell(new HelloWorld.ChangeMessage("Hello Actor World!!!"));
        helloWorldSystem.tell(HelloWorld.SayHello.INSTANCE);
        helloWorldSystem.tell(HelloWorld.SayHello.INSTANCE);
    }
}
