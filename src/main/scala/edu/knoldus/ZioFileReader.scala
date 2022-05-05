package edu.knoldus

import java.io.File

import zio.console.Console
import zio.{ExitCode, URIO, _}

import scala.io.Source
object ZioFileReader extends zio.App {

val filePaths = List("file.txt","file2.txt","file3.txt","file4.txt")

  val wordCounter: String => Task[Int] = (str: String) => ZIO{
    str.split(" ").length
  }
  val fileWordCount: ZIO[Console, Throwable, Unit] =
    for {
    filesData: List[String] <- ZIO.foreach(filePaths) {
      path => ZIO(Source.fromFile(new File(path)))
      .bracket(s => URIO(s.close()))
      { s =>
        ZIO(s.getLines()
          .mkString)
      }
    }
    count: List[Int] <- ZIO.foreach(filesData) {
      data =>
       wordCounter(data)
    }
    _ <- ZIO.foreach_(count)(res4 => zio.console.putStrLn(res4.toString))

  } yield ()


  override def run(args: List[String]): URIO[Console, ExitCode] = fileWordCount.exitCode
}

object ZioFileReaderFiber extends zio.App {
  def printThread = s"[${Thread.currentThread().getName}]"
  val filePaths = List("file.txt","file2.txt","file3.txt")

  val wordCounter: String => Task[Int] = (str: String) => ZIO{
    str.split(" ").length
  }
  val fileWordCount: ZIO[Console, Throwable, Unit] = for {
    filesData: Fiber.Runtime[Throwable, List[String]] <- ZIO.foreach(filePaths) {
      path => ZIO(Source.fromFile(new File(path)))
        .bracket(s => URIO(s.close()))(s => ZIO(s.getLines().mkString))
    }.debug(printThread).fork
    v1: List[String] <- filesData.join

    count: Fiber.Runtime[Throwable, List[Int]] <- ZIO.foreach(v1) {
      data =>
        wordCounter(data)
    }.debug(printThread).fork
    v2 <- count.join
    _ <- ZIO.foreach_(v2)(data => zio.console.putStrLn(data.toString))

  } yield ()


  override def run(args: List[String]): URIO[Console, ExitCode] = fileWordCount.exitCode
}


//    count <- ZIO.effect{
//      for {
//        text <- filesData
//        data <- text
//          .toLowerCase
//          .replaceAll("[^\\p{L}]", " ")
//          .split("\\s") //	Matches whitespace //s
//          .filterNot(_ == "")
//      }  yield data
//
//      .foldLeft(Map.empty[String, Int].withDefaultValue(0)) {
//        case (counts, word) => counts.updated(word, counts(word) + 1)
//      }
//
//
//    length <- counter(count)
//    _ <- zio.console.putStrLn(length)
//
//
//
//
//    res: List[MapView[String, Int]] <- ZIO.foreach(filesData){
//      data =>
//         ZIO(data.split(" ").map(_.toLowerCase).groupBy(identity).view.mapValues(_.length))
//    }
//
//def tokenize(text: String): Task[Array[String]] = ZIO{
//  text
//  .toLowerCase
//  .replaceAll("[^\\p{L}]", " ")
//  .split("\\s")
//  .filterNot(_ == "")
//}