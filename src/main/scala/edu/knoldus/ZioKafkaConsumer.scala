package edu.knoldus

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream._


object ZioKafkaConsumer extends zio.App {

  val managedConsumer: RManaged[Clock with Blocking, Consumer.Service] = Consumer.make(ConsumerSettings(List("localhost:9092"))
    .withGroupId("zio-group"))

  val consumer: ZLayer[Clock with Blocking, Throwable, Has[Consumer.Service]] = ZLayer.fromManaged(managedConsumer)

  val streams: ZStream[Console with Any with Consumer with Clock, Throwable, OffsetBatch] = Consumer.subscribeAnd(Subscription.topics("kafkaTopic"))
    .plainStream(Serde.string,Serde.string)
    .map(cr => (cr.value,cr.offset))
    .tap {
      case (value, _ ) => zio.console.putStrLn(s"| $value | hello")
    }
    .map(_._2) //stream of offsets
    .aggregateAsync(Consumer.offsetBatches)


  val streamEffect: ZIO[Console with Any with Consumer with Clock, Throwable, Unit] = streams.run(ZSink.foreach((offset => offset.commit)))

  override def run(args: List[String]): URIO[Clock with Blocking with Any with Console, ExitCode] =
    streamEffect.provideSomeLayer(consumer ++ zio.console.Console.live).exitCode
}