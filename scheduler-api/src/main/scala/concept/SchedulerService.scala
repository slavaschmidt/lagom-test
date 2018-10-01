package concept

import concept.model._
import com.lightbend.lagom.scaladsl.api._
import Service._
import akka.Done
import com.lightbend.lagom.scaladsl.api.broker.Topic

trait SchedulerService extends Service {
  def schedule: ServiceCall[JobRequest, Done]

  def scheduledJobs: Topic[JobRequest]

  override def descriptor: Descriptor = {
    named("SchedulerService")
      .withCalls(call(schedule))
      .withTopics(
        topic(SchedulerService.ScheduledJobs, scheduledJobs)
      )
      .withAutoAcl(true)
  }
}
object SchedulerService {
  val ScheduledJobs = "ScheduledJobs"
}
