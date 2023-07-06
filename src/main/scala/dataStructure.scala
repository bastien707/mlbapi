package mlb

import zio.json.JsonEncoder
import zio.json.JsonDecoder
import zio.jdbc.JdbcDecoder
import zio.json.DeriveJsonDecoder
import zio.json.DeriveJsonEncoder

case class MLBData(
    date: String,
    season: String,
    team1: String,
    team2: String,
    elo1Pre: Float,
    elo2Pre: Float,
    eloProb1: Float,
    eloProb2: Float,
    rating1Pre: Float,
    rating2Pre: Float,
    ratingProb1: Float,
    ratingProb2: Float
)
case class Team(id: Int, name: String, elo: Float, rating: Float)
case class Score(elo: Float, rating: Float)

object MLBData {
  implicit val encoder: JsonEncoder[MLBData] = DeriveJsonEncoder.gen[MLBData]
  implicit val decoder: JsonDecoder[MLBData] = DeriveJsonDecoder.gen[MLBData]
  implicit val jdbcDecoder: JdbcDecoder[MLBData] = JdbcDecoder { resultSet =>
    val date = resultSet.getString("date")
    val season = resultSet.getString("season")
    val team1 = resultSet.getString("team1")
    val team2 = resultSet.getString("team2")
    val elo1Pre = resultSet.getFloat("elo1Pre")
    val elo2Pre = resultSet.getFloat("elo2Pre")
    val eloProb1 = resultSet.getFloat("eloProb1")
    val eloProb2 = resultSet.getFloat("eloProb2")
    val rating1Pre = resultSet.getFloat("rating1Pre")
    val rating2Pre = resultSet.getFloat("rating2Pre")
    val ratingProb1 = resultSet.getFloat("ratingProb1")
    val ratingProb2 = resultSet.getFloat("ratingProb2")
    MLBData(
      date,
      season,
      team1,
      team2,
      elo1Pre,
      elo2Pre,
      eloProb1,
      eloProb2,
      rating1Pre,
      rating2Pre,
      ratingProb1,
      ratingProb2
    )
  }
}

object Team {
  implicit val encoder: JsonEncoder[Team] = DeriveJsonEncoder.gen[Team]
  implicit val decoder: JsonDecoder[Team] = DeriveJsonDecoder.gen[Team]
  implicit val jdbcDecoder: JdbcDecoder[Team] = JdbcDecoder { resultSet =>
    val id = resultSet.getInt("id")
    val name = resultSet.getString("name")
    val elo = resultSet.getFloat("elo")
    val rating = resultSet.getFloat("rating")
    Team(id, name, elo, rating)
  }
}

object Score {
  implicit val encoder: JsonEncoder[Score] = DeriveJsonEncoder.gen[Score]
  implicit val decoder: JsonDecoder[Score] = DeriveJsonDecoder.gen[Score]
  implicit val jdbcDecoder: JdbcDecoder[Score] = JdbcDecoder { resultSet =>
    val elo = resultSet.getFloat("elo")
    val rating = resultSet.getFloat("rating")
    Score(elo, rating)
  }
}
