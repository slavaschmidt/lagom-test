package concept

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.Method

trait ManagerService extends Service {
  def process(count: Int): ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("ManagerService").withCalls(
      restCall(Method.GET, "/schedule/:count", process _)
    )
  }
}
