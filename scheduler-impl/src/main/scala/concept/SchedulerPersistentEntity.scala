package concept

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import concept.model.JobRequest
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntity, PersistentEntityRef, PersistentEntityRegistry}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

final class SchedulerPersistentEntity(persistentEntities: PersistentEntityRegistry,
                                      as: ActorSystem)
    extends PersistentEntity {

  override type Command = SchedulerCommand
  override type Event = SchedulerEvent
  override type State = SchedulerState

  override def initialState: SchedulerState = SchedulerState(Nil)

  private val schedulingTime = 1.second

  implicit val ec: ExecutionContext = as.dispatcher
  lazy val thisEntity: PersistentEntityRef[SchedulerCommand] =
    persistentEntities.refFor[SchedulerPersistentEntity](this.entityId)

  override def behavior: Behavior =
    Actions()
      .onCommand[ScheduleCommand, Done] {
        case (ScheduleCommand(JobRequest(count)), ctx, _) if count <= 0 =>
          ctx.invalidCommand(s"Count must be positive but was $count")
          ctx.done

        case (ScheduleCommand(request), ctx, _) =>
          val id = UUID.randomUUID()
          ctx.thenPersist(Scheduling(id, request)) { evt =>
            as.scheduler.scheduleOnce(schedulingTime)(
              thisEntity.ask(DoneCommand(id)))
            ctx.reply(Done)
          }
      }
      .onCommand[DoneCommand, Done] {
        case (DoneCommand(id), ctx, state) =>
          state.batches
            .find(_.id == id)
            .map { g =>
              ctx.thenPersist(Scheduled(id, g.jobRequest)) { _ =>
                ctx.reply(Done)
              }
            }
            .getOrElse(ctx.done)
      }
      .onEvent {
        case (m: Scheduling, state) =>
          SchedulerState(state.batches :+ m)

        case (Scheduled(id, _), state) =>
          SchedulerState(state.batches.filterNot(_.id == id))
      }

}
