package mlb

import zio.json.JsonEncoder
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
    elo1post: Float,
    elo2post: Float,
    rating1Pre: Float,
    rating2Pre: Float,
    pitcher1: String,
    pitcher2: String,
    pitcher1rgs: Float,
    pitcher2rgs: Float,
    pitcher1adj: Float,
    pitcher2adj: Float,
    ratingProb1: Float,
    ratingProb2: Float,
    rating1Post: Float,
    rating2Post: Float,
    score1: Int,
    score2: Int
)

case class Team (
  season: String,
  teamHome: String,
  teamAway: String,
)

object MLBData {
  implicit val encoder: JsonEncoder[MLBData] = DeriveJsonEncoder.gen[MLBData]
  implicit val jdbcDecoder: JdbcDecoder[MLBData] = JdbcDecoder { resultSet =>
    val date = resultSet.getString("date")
    val season = resultSet.getString("season")
    val team1 = resultSet.getString("team1")
    val team2 = resultSet.getString("team2")
    val elo1Pre = resultSet.getFloat("elo1Pre")
    val elo2Pre = resultSet.getFloat("elo2Pre")
    val eloProb1 = resultSet.getFloat("eloProb1")
    val eloProb2 = resultSet.getFloat("eloProb2")
    val elo1post = resultSet.getFloat("elo1post")
    val elo2post = resultSet.getFloat("elo2post")
    val rating1Pre = resultSet.getFloat("rating1Pre")
    val rating2Pre = resultSet.getFloat("rating2Pre")
    val pitcher1 = resultSet.getString("pitcher1")
    val pitcher2 = resultSet.getString("pitcher2")
    val pitcher1rgs = resultSet.getFloat("pitcher1rgs")
    val pitcher2rgs = resultSet.getFloat("pitcher2rgs")
    val pitcher1adj = resultSet.getFloat("pitcher1adj")
    val pitcher2adj = resultSet.getFloat("pitcher2adj")
    val ratingProb1 = resultSet.getFloat("ratingProb1")
    val ratingProb2 = resultSet.getFloat("ratingProb2")
    val rating1Post = resultSet.getFloat("rating1Post")
    val rating2Post = resultSet.getFloat("rating2Post")
    val score1 = resultSet.getInt("score1")
    val score2 = resultSet.getInt("score2")
    MLBData(
      date,
      season,
      team1,
      team2,
      elo1Pre,
      elo2Pre,
      eloProb1,
      eloProb2,
      elo1post,
      elo2post,
      rating1Pre,
      rating2Pre,
      pitcher1,
      pitcher2,
      pitcher1rgs,
      pitcher2rgs,
      pitcher1adj,
      pitcher2adj,
      ratingProb1,
      ratingProb2,
      rating1Post,
      rating2Post,
      score1,
      score2
    )
  }
}

object Team {
  implicit val encoder: JsonEncoder[Team] = DeriveJsonEncoder.gen[Team]
  implicit val jdbcDecoder: JdbcDecoder[Team] = JdbcDecoder { resultSet =>
    val season = resultSet.getString("season") // name of column in database
    val teamHome = resultSet.getString("team1")
    val teamAway = resultSet.getString("team2")
    Team(
      season,
      teamHome,
      teamAway
    )
  }
}

case class SeasonAverage(season: String, avgScore: Float, avgElo: Float, avgRating: Float)

object SeasonAverage {
  implicit val encoder: JsonEncoder[SeasonAverage] = DeriveJsonEncoder.gen[SeasonAverage]
  implicit val decoder: JdbcDecoder[SeasonAverage] = JdbcDecoder { resultSet =>
    val season = resultSet.getString("season")
    val avgScore = resultSet.getFloat("avg_score")
    val avgElo = resultSet.getFloat("avg_elo")
    val avgRating = resultSet.getFloat("avg_rating")
    SeasonAverage(season, avgScore, avgElo, avgRating)
  }
}
