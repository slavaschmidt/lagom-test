package concept

import concept.model._
import com.lightbend.lagom.scaladsl.api._
import Service._
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

trait ExecutionService extends Service {
  def execute: ServiceCall[Source[PreBatch, NotUsed], Source[FullBatch, NotUsed]]
  override def descriptor: Descriptor = named("ExecutionService").withCalls(call(execute))
}

object ExecutionServiceFlow {
  val shortFlow: Flow[PreBatch, FullBatch, NotUsed] = Flow[PreBatch].map(b => FullBatch(b.count))
}
