package edu.knoldus

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.json._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream._

object ZioKafkaProducer extends zio.App {
  val managedProducer = Producer.make(ProducerSettings(List("localhost:9092")),Serde.string,Serde.string)
  val producer: ZLayer[Blocking, Throwable, Producer[Any, String,String]] = ZLayer.fromManaged(managedProducer)


  val record = new ProducerRecord("kafkaTopic","key-1","abc")
  val producerEffect: ZIO[Producer[Any, String,String],Throwable, RecordMetadata] = Producer.produce(record)
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    producerEffect.provideSomeLayer(producer).exitCode
}
object ZioKafkaConsumer extends zio.App {

  val managedConsumer = Consumer.make(ConsumerSettings(List("localhost:9092"))
    .withGroupId("zio-group"))
  val consumer = ZLayer.fromManaged(managedConsumer)

  val streams: ZStream[Console with Clock, Throwable, OffsetBatch] = Consumer.subscribeAnd(Subscription.topics("kafkaTopic"))
    .plainStream(Serde.string,Serde.string)
    .map(cr => (cr.value,cr.offset))
    .tap {
      case (value, _ ) => zio.console.putStrLn(s"| $value |")
    }
    .map(_._2) //stream of offsets
    .aggregateAsync(Consumer.offsetBatches)


  val streamEffect = streams.run(ZSink.foreach((offset => offset.commit)))

  override def run(args: List[String]) =
    streamEffect.provideSomeLayer(consumer ++ zio.console.Console.live).exitCode
}
