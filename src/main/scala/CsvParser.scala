package mlb

import com.github.tototoshi.csv._
import Constants.Elo

object CsvParser {
  def parseMlbData(): List[MLBData] = {
    val reader = CSVReader.open(
      new java.io.File(Elo)
    )

    val mlbDataList = reader
      .allWithHeaders()
      .map(row =>
        MLBData(
          row("date"),
          row("season"),
          row("team1"),
          row("team2"),
          row("elo1_pre").toFloat,
          row("elo2_pre").toFloat,
          row("elo_prob1").toFloat,
          row("elo_prob2").toFloat,
          parseFloat(row("elo1_post")),
          parseFloat(row("elo2_post")),
          row("rating1_pre").toFloat,
          row("rating2_pre").toFloat,
          parseString(row("pitcher1")),
          parseString(row("pitcher2")),
          parseFloat(row("pitcher1_rgs")),
          parseFloat(row("pitcher2_rgs")),
          parseFloat(row("pitcher1_adj")),
          parseFloat(row("pitcher2_adj")),
          row("rating_prob1").toFloat,
          row("rating_prob2").toFloat,
          parseFloat(row("rating1_post")),
          parseFloat(row("rating2_post")),
          parseInt(row("score1")),
          parseInt(row("score2"))
        )
      )

    reader.close()

    mlbDataList
  }

  def parseFloat(value: String): Option[Float] =
    Option(value).flatMap(str => Option(str).filter(_.nonEmpty).map(_.toFloat))

  def parseInt(value: String): Option[Int] =
    Option(value).flatMap(str => Option(str).filter(_.nonEmpty).map(_.toInt))

  def parseString(value: String): String = {
    Option(value)
      .filter(_.nonEmpty)
      .getOrElse("undefined")
  }
}
