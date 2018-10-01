package concept

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.{Done, NotUsed}
import concept.model._
import com.lightbend.lagom.scaladsl.api._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class ManagerServiceImpl(schedulerService: SchedulerService,
                         batchedExecutor: ExecutionService,
                         as: ActorSystem)
    extends ManagerService {

  private val count: AtomicInteger = new AtomicInteger(0)

  private val logger = Logger("Manager")

  override def process(count: Int): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    logger.info(s"Processing : $count")
    schedulerService.schedule.invoke(JobRequest(count))
  }

  private implicit lazy val ec: ExecutionContext = as.dispatcher
  private implicit lazy val am: ActorMaterializer = ActorMaterializer()(as)

  val sub: Future[Done] =
    schedulerService.scheduledJobs.subscribe.atLeastOnce(executorFlow)

  private def update(done: FullBatch) = {
    val result = count.addAndGet(done.count)
    logger.info(s"Got batch of jobs done, now there are $result available")
    result
  }

  private lazy val executorFlow: Flow[JobRequest, Done, NotUsed] = Flow[JobRequest]
    .map { jobRequest: JobRequest =>
      val fut = distribute(jobRequest) // simulates another service call
      val src: Source[PreBatch, NotUsed] = Source.fromFuture(fut)
      val ready = batch(src) // or local(src) to fake it
      Source.fromFutureSource(ready)
    }
    .flatMapConcat(identity)
    .map(update)
    .map(_ => Done)

  private def batch(in: Source[PreBatch, NotUsed]): Future[Source[FullBatch, NotUsed]] = batchedExecutor.execute.invoke(in)
  private def local(in: Source[PreBatch, NotUsed]): Future[Source[FullBatch, NotUsed]] = Future.successful(in.via(ExecutionServiceFlow.shortFlow))

  private def distribute(jobRequest: JobRequest) = Future.successful(PreBatch(jobRequest.count)) // simulates another service call

}
