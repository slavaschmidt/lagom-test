package concept

object model {
  final case class JobRequest(count: Int)
  final case class PreBatch(count: Int) {
    def +(c: PreBatch): PreBatch = PreBatch(count + c.count)
  }
  final case class FullBatch(count: Int)

  import play.api.libs.json._

  implicit val jobRequest: Format[JobRequest] = Json.format
  implicit val preBatch: Format[PreBatch] = Json.format
  implicit val fullBatch: Format[FullBatch] = Json.format
}
