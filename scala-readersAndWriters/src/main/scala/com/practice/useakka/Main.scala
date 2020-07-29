package com.practice.useakka

import java.util.{Timer, TimerTask}

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Main {

  def apply(): Behavior[String] = Behaviors.setup(context => {
    Behaviors.receiveMessage(message => {
      val mail = context.spawn(Mail.apply(), "Mail")
      val writer = context.spawn(Writer.apply(mail), "Writer")
      val reader = context.spawn(Reader.apply(mail), "Reader")
      val timer = new Timer()
      var i1 = 0
      val task1 = new TimerTask {
        override def run(): Unit = {
          i1 += 1
          writer.tell(Writer.WriteMessage("msg" + i1))
        }
      }
      val task2 = new TimerTask {
        override def run(): Unit = reader.tell(Reader.ReadMessage)
      }
      timer.scheduleAtFixedRate(task1, 0, 500)
      timer.scheduleAtFixedRate(task2, 0, 1000)
      Behaviors.same
    })
  })

  def main(args: Array[String]): Unit = {
    val actorMain = ActorSystem.apply(Main.apply(), "Main")
    actorMain.tell("Start")
  }
}
