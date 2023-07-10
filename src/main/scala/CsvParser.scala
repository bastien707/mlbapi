package mlb

import com.github.tototoshi.csv._
import Constants.*
import java.time.LocalDate
import GameDates.GameDate
import HomeTeams.HomeTeam
import AwayTeams.AwayTeam
import SeasonYears.SeasonYear
import PlayoffRounds.PlayoffRound

object CsvParser {
  def parseGame(row: Map[String, String]): Game = {
    val date = GameDate(LocalDate.parse(row("date")))
    val season = SeasonYear(row("season").toInt)
    val playoffRound =
      if (row("playoff").isEmpty) None else Some(PlayoffRound(row("playoff").toInt))
    val homeTeam = HomeTeam(row("team1"))
    val awayTeam = AwayTeam(row("team2"))
    Game(date, season, playoffRound, homeTeam, awayTeam)
  }
}
