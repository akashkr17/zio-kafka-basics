//package edu.knoldus
//
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.common.TopicPartition
//import zio._
//import zio.kafka.KafkaTestUtils._
//import zio.kafka.consumer.{ Consumer, ConsumerSettings, OffsetBatch, Subscription }
//import zio.kafka.embedded.Kafka
//import zio.kafka.producer.{ Producer, Transaction, TransactionalProducer }
//import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
//import zio.kafka.serde.Serde
//import zio.test.Assertion._
//import zio.test.TestAspect.withLiveClock
//import zio.test._
//
//object ProducerSpec extends DefaultRunnableSpec {
//  object Kafka {
//    trait Service {
//      def bootstrapServers: List[String]
//      def stop(): UIO[Unit]
//    }
//    final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka.Service {
//      override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
//      override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
//    }
//
//    case object DefaultLocal extends Kafka.Service {
//      override def bootstrapServers: List[String] = List(s"localhost:9092")
//      override def stop(): UIO[Unit]              = UIO.unit
//    }
//
//    val embedded: ZLayer[Any, Throwable, Kafka] = ZLayer.fromScoped {
//      implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
//        customBrokerProperties = Map("group.min.session.timeout.ms" -> "500", "group.initial.rebalance.delay.ms" -> "0")
//      )
//      ZIO.acquireRelease(EmbeddedKafkaService(EmbeddedKafka.start()))
//    }(_.stop())
//
//    val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
//
//    override def spec =
//    suite("producer test suite")(
//      test("one record") {
//        for {
//          _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
//        } yield assertCompletes
//      },
//      // ...
//    ).provideSomeLayerShared[TestEnvironment](
//      (Kafka.embedded >+> (producer ++ transactionalProducer))
//        .mapError(TestFailure.fail) ++ Clock.live
//    )
//
//    object KafkaTestUtils {
//
//      val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
//        ZIO.serviceWith[Kafka](_.bootstrapServers).map(ProducerSettings(_))
//
//      val producer: ZLayer[Kafka, Throwable, Producer] =
//        (ZLayer(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
//          Producer.live
//
//      val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings] =
//        ZIO.serviceWith[Kafka](_.bootstrapServers).map(TransactionalProducerSettings(_, "test-transaction"))
//
//      val transactionalProducer: ZLayer[Kafka, Throwable, TransactionalProducer] =
//        (ZLayer.fromZIO(transactionalProducerSettings) ++ ZLayer.succeed(
//          Serde.string: Serializer[Any, String]
//        )) >>>
//          TransactionalProducer.live
//    }
//}
