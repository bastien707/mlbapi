package mlb

import com.github.tototoshi.csv._
import Constants.*
import java.time.LocalDate
import GameDates.GameDate
import HomeTeams.HomeTeam
import AwayTeams.AwayTeam
import SeasonYears.SeasonYear
import Ratings.Rating
import Probs.Prob
import Scores.Score

object CsvParser {
  def parseGame(row: Map[String, String]): Game = {
    val date = GameDate(LocalDate.parse(row("date")))
    val season = SeasonYear(row("season").toInt)
    val homeTeam = HomeTeam(row("team1"))
    val awayTeam = AwayTeam(row("team2"))
    val elo1Pre = Rating(row("elo1_pre").toFloat)
    val elo2Pre = Rating(row("elo2_pre").toFloat)
    val eloProb1 = Prob(row("elo_prob1").toFloat)
    val eloProb2 = Prob(row("elo_prob2").toFloat)
    val elo1_post =
      if (row("elo1_post").isEmpty) None else Some(Rating(row("elo1_post").toFloat))
    val elo2_post =
      if (row("elo2_post").isEmpty) None else Some(Rating(row("elo2_post").toFloat))
    val rating1Pre = Rating(row("rating1_pre").toFloat)
    val rating2Pre = Rating(row("rating2_pre").toFloat)
    val ratingProb1 = Prob(row("rating_prob1").toFloat)
    val ratingProb2 = Prob(row("rating_prob2").toFloat)
    val rating1Post =
      if (row("rating1_post").isEmpty) None else Some(Rating(row("rating1_post").toFloat))
    val rating2Post =
      if (row("rating2_post").isEmpty) None else Some(Rating(row("rating2_post").toFloat))
    val score1 = if (row("score1").isEmpty) None else Some(Score(row("score1").toInt))
    val score2 = if (row("score2").isEmpty) None else Some(Score(row("score2").toInt))
    Game(date, season, homeTeam, awayTeam, elo1Pre, elo2Pre, eloProb1, eloProb2, elo1_post, elo2_post, rating1Pre, rating2Pre, ratingProb1, ratingProb2, rating1Post, rating2Post, score1, score2)
  }
}
