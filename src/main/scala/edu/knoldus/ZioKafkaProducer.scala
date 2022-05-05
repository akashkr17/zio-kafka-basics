package edu.knoldus

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio.{ExitCode, RIO, RManaged, URIO, ZIO, ZLayer}
import zio.blocking.Blocking
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde

object ZioKafkaProducer extends zio.App {
  val managedProducer: RManaged[Blocking, Producer.Service[Any, String, String]] = Producer.make(ProducerSettings(List("localhost:9092")),Serde.string,Serde.string)

  val producer: ZLayer[Blocking, Throwable, Producer[Any, String,String]] = ZLayer.fromManaged(managedProducer)


  val record: ProducerRecord[String, String] = new ProducerRecord("kafkaTopic","key-1","abc")

  val producerEffect: RIO[Producer[Any, String, String], RecordMetadata] = Producer.produce(record)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    producerEffect.provideSomeLayer(producer).exitCode
}
