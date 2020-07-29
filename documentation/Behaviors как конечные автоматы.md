# Behaviors как конечные автоматы

Актор может быть использован для моделирования машины конечного состояния (FSM).

Для демонстрации этого рассмотрим агента, который должен получать сообщения и ставить их в очередь, пока они поступают во время разрыва, и посылать их дальше после завершения разрыва или получения запроса на флеш.

Этот пример демонстрирует, как это сделать:

* Моделировать состояния с использованием различных моделей поведения

* Моделирование хранения данных в каждом состоянии путем представления поведения как метода

* Внедрить государственные таймауты

События, которые может получать FSM, становятся тем типом сообщений, которые может получать актер:

```java
public abstract class Buncher {

  public interface Event {}

  public static final class SetTarget implements Event {
    public final ActorRef<Batch> ref;

    public SetTarget(ActorRef<Batch> ref) {
      this.ref = ref;
    }
  }

  private enum Timeout implements Event {
    INSTANCE
  }

  public enum Flush implements Event {
    INSTANCE
  }

  public static final class Queue implements Event {
    public final Object obj;

    public Queue(Object obj) {
      this.obj = obj;
    }
  }
}
```

`SetTarget` необходима для ее запуска, устанавливая пункт назначения для передачи `Batches`; `Queue` добавит во внутреннюю очередь, в то время как `Flush` отметит конец взрыва.

```java
interface Data {}

public static final class Todo implements Data {
  public final ActorRef<Batch> target;
  public final List<Object> queue;

  public Todo(ActorRef<Batch> target, List<Object> queue) {
    this.target = target;
    this.queue = queue;
  }

}

public static final class Batch {
  public final List<Object> list;

  public Batch(List<Object> list) {
    this.list = list;
  }

}
```

Каждое состояние становится своеобразным поведением, и после обработки сообщения возвращается следующее состояние в виде `Behavior `.

```java
public abstract class Buncher {
  // FSM состояния, представленные как behaviors

  // начальное состояние
  public static Behavior<Event> create() {
    return uninitialized();
  }

  private static Behavior<Event> uninitialized() {
    return Behaviors.receive(Event.class)
        .onMessage(
            SetTarget.class, message -> idle(new Todo(message.ref, Collections.emptyList())))
        .build();
  }

  private static Behavior<Event> idle(Todo data) {
    return Behaviors.receive(Event.class)
        .onMessage(Queue.class, message -> active(data.addElement(message)))
        .build();
  }

  private static Behavior<Event> active(Todo data) {
    return Behaviors.withTimers(
        timers -> {
          // Состояние тайм-аута с Таймерами
          timers.startSingleTimer("Timeout", Timeout.INSTANCE, Duration.ofSeconds(1));
          return Behaviors.receive(Event.class)
              .onMessage(Queue.class, message -> active(data.addElement(message)))
              .onMessage(Flush.class, message -> activeOnFlushOrTimeout(data))
              .onMessage(Timeout.class, message -> activeOnFlushOrTimeout(data))
              .build();
        });
  }

  private static Behavior<Event> activeOnFlushOrTimeout(Todo data) {
    data.target.tell(new Batch(data.queue));
    return idle(data.copy(new ArrayList<>()));
  }

}
```

@scala[ Метод `idle` выше использует `Behaviors.unhandled`, который советует системе повторно использовать предыдущее поведение, включая намек на то, что сообщение не было обработано. Есть два взаимосвязанных поведения:

вернуть `Behaviors.empty` в качестве следующего поведения в случае, если вы достигли состояния, в котором больше не ожидаете сообщений. Например, если агент только ждет, пока все порожденные детские актеры не остановятся. Необработанные сообщения все еще записываются в журнал с таким поведением.
return `Behaviors.ignore` как следующее поведение в случае, если Вас не волнуют необработанные сообщения. Все сообщения, отправленные агенту с таким поведением, просто отбрасываются и игнорируются (без протоколирования) ]
Для установки таймаутов состояния используйте `Behaviors.withTimers` вместе с `startSingleTimer`.

## Пример проекта

[Пример FSM проекта](https://developer.lightbend.com/start/?group=akka&project=akka-samples-fsm-java) это пример проекта, который можно загрузить, и с инструкциями по его запуску.

Этот проект содержит образец Dining Hakkers, иллюстрирующий, как моделировать машину конечного состояния (FSM) с актерами.