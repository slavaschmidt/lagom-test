package concept

import akka.Done
import akka.actor.ActorSystem
import concept.model._
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.softwaremill.macwire.wire

class SchedulerServiceImpl(persistentEntities: PersistentEntityRegistry,
                           as: ActorSystem) extends SchedulerService {

  private lazy val entity = wire[SchedulerPersistentEntity]
  persistentEntities.register(entity)

  override def schedule: ServiceCall[JobRequest, Done] = ServiceCall { request =>
    val ref = persistentEntities.refFor[SchedulerPersistentEntity]("Scheduler")
    ref.ask(ScheduleCommand(request))
  }

  override def scheduledJobs: Topic[JobRequest] =
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntities
        .eventStream(SchedulerModel.EventTag, fromOffset)
        .map { ev => (convertEvent(ev), ev.offset) }
    }

  private def convertEvent(schedulerEvent: EventStreamElement[SchedulerEvent]): JobRequest = {
    schedulerEvent.event match {
      case Scheduled(_, dough) => dough
    }
  }
}
