package com.practice.useakka

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Reader {
  sealed trait Command
  object ReadMessage extends Command
  private final case class WrappedMailResponse(response: Mail.Response) extends Command

  def apply(mail: ActorRef[Mail.Command]): Behavior[Command] = Behaviors.setup(context => new Reader(context, mail))
}

class Reader(context: ActorContext[Reader.Command], mail: ActorRef[Mail.Command])
  extends AbstractBehavior[Reader.Command](context) {

  import Reader._

  private val mailResponseAdapter: ActorRef[Mail.Response] =
    context.messageAdapter(response => WrappedMailResponse(response))

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case ReadMessage =>
      mail.tell(Mail.PopMessage(mailResponseAdapter))
      this
    case WrappedMailResponse(response) =>
      response match {
        case Mail.MessageRespond(message) =>
          context.log.info("Read message: {}", message)
          this
      }
  }
}
