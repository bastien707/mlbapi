package mlb

import com.github.tototoshi.csv._
import Constants.*
import java.time.LocalDate
import GameDates.GameDate
import HomeTeams.HomeTeam
import AwayTeams.AwayTeam
import SeasonYears.SeasonYear

object CsvParser {
  def parseGame(): List[Game] = {
    val reader = CSVReader.open(
      new java.io.File(LatestElo)
    )

    val mlbDataList = reader
      .allWithHeaders()
      .map(row =>
        Game(
          GameDate(LocalDate.parse(row("date"))),
          SeasonYear(row("season").toInt),
          None,
          HomeTeam(row("team1")),
          AwayTeam(row("team2")),
        )
      )

    reader.close()

    mlbDataList
  }
}
