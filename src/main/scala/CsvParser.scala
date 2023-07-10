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

object CsvParser {
  def parseGame(row: Map[String, String]): Game = {
    val date = GameDate(LocalDate.parse(row("date")))
    val season = SeasonYear(row("season").toInt)
    val homeTeam = HomeTeam(row("team1"))
    val awayTeam = AwayTeam(row("team2"))
    val elo1Pre =
      if (row("elo1_pre").isEmpty) None else Some(Rating(row("elo1_pre").toFloat))
    val elo2Pre =
      if (row("elo2_pre").isEmpty) None else Some(Rating(row("elo2_pre").toFloat))
    val eloProb1 =
      if (row("elo_prob1").isEmpty) None else Some(Prob(row("elo_prob1").toFloat))
    val eloProb2 =
      if (row("elo_prob2").isEmpty) None else Some(Prob(row("elo_prob2").toFloat))
    Game(date, season, homeTeam, awayTeam, elo1Pre, elo2Pre, eloProb1, eloProb2)
  }
}
