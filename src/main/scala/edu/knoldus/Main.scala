package edu.knoldus

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.json._
import zio.kafka.consumer._
import zio.kafka.serde.Serde
import zio.stream._
object Main extends zio.App {

  val consumerSetting: ConsumerSettings = ConsumerSettings(List("localhost:9092"))
    .withGroupId("updates-score")

  val managedConsumer: RManaged[Clock with Blocking, Consumer.Service] = Consumer.make(consumerSetting)
 val consumer: ZLayer[Clock with Blocking, Throwable, Has[Consumer.Service]] = ZLayer.fromManaged(managedConsumer)

  val footballMatchesStream: ZStream[Any with Consumer, Throwable, CommittableRecord[String, String]] = Consumer.subscribeAnd(Subscription.topics("updates"))
    .plainStream(Serde.string,Serde.string)

  case class MatchPlayer(name: String,score: Int) {
    override def toString = s"$name : $score"
  }
  object MatchPlayer {
    implicit val encoder: JsonEncoder[MatchPlayer] = DeriveJsonEncoder.gen[MatchPlayer]
    implicit val decoder: JsonDecoder[MatchPlayer] = DeriveJsonDecoder.gen[MatchPlayer]
  }

  case class Match(players: Array[MatchPlayer]) {
    def score = s"${players(0)} - ${players(1)}"
  }
  object Match {
    implicit val encoder: JsonEncoder[Match] = DeriveJsonEncoder.gen[Match]
    implicit val decoder: JsonDecoder[Match] = DeriveJsonDecoder.gen[Match]
  }

  val matchSerde: Serde[Any,Match]= Serde.string.inmapM{
    string => ZIO.fromEither(string.fromJson[Match].left.map(errorMessage => new RuntimeException(errorMessage)))
  }{
    theMatch =>
      ZIO.effect(theMatch.toJson)
  }

  val matchesStream: ZStream[Any with Consumer, Throwable, CommittableRecord[String, Match]] = Consumer.subscribeAnd(Subscription.topics("updates"))
    .plainStream(Serde.string,matchSerde)

val matchesPrintableStream: ZStream[Console with Any with Consumer with Clock, Throwable, OffsetBatch] = matchesStream
  .map(cr => (cr.value.score,cr.offset))
  .tap {
    case (score, _ ) => zio.console.putStrLn(s"| $score |")
  }
  .map(_._2) //stream of offsets
  .aggregateAsync(Consumer.offsetBatches)

  val streamEffect: ZIO[Console with Any with Consumer with Clock, Throwable, Unit] = matchesPrintableStream.run(ZSink.foreach((offset => offset.commit)))


  override def run(args: List[String]): URIO[Clock with Blocking with Any with Console, ExitCode] =
    streamEffect.provideSomeLayer(consumer ++ zio.console.Console.live).exitCode
}
