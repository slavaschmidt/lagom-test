package concept

import akka.NotUsed
import akka.stream.{Attributes, DelayOverflowStrategy}
import akka.stream.scaladsl.{BidiFlow, Flow, Source}
import concept.model._
import com.lightbend.lagom.scaladsl.api._

import scala.concurrent.duration._
import scala.concurrent.Future

import play.api.Logger

class ExecutionServiceImpl extends ExecutionService {

  private val logger = Logger("ExecutionService")

  override def execute: ServiceCall[Source[PreBatch, NotUsed], Source[FullBatch, NotUsed]] = ServiceCall { batch =>
    logger.info(s"Executing: $batch")
    Future.successful(batch.via(ExecutionServiceFlow.shortFlow))
  }

}
