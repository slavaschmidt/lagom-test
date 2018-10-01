package concept

import java.util.UUID

import akka.Done
import concept.model.JobRequest
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer


sealed trait SchedulerCommand
final case class ScheduleCommand(jobRequest: JobRequest) extends SchedulerCommand with ReplyType[Done]
final case class DoneCommand(id: UUID) extends SchedulerCommand with ReplyType[Done]

sealed trait SchedulerEvent
final case class Scheduling(id: UUID, jobRequest: JobRequest) extends SchedulerEvent
final case class Scheduled(id: UUID, jobRequest: JobRequest) extends SchedulerEvent with AggregateEvent[Scheduled] {
  override def aggregateTag: AggregateEventTag[Scheduled] = SchedulerModel.EventTag
}

sealed trait SchedulerStateI {
  def batches: List[Scheduling]
}
final case class SchedulerState(batches: List[Scheduling]) extends SchedulerStateI

object SchedulerModel {
  val EventTag: AggregateEventTag[Scheduled] = AggregateEventTag[Scheduled]("Scheduled")
  import play.api.libs.json._
  implicit val schedulingFormat: OFormat[Scheduling] = Json.format[Scheduling]
  val serializers = List(
    JsonSerializer(schedulingFormat),
    JsonSerializer(Json.format[Scheduled]),
    JsonSerializer(Json.format[SchedulerState]))
}
