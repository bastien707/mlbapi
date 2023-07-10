package mlb

import zio.json._
import zio.jdbc._

import java.time.LocalDate
import org.h2.tools.Csv

object HomeTeams {

  opaque type HomeTeam = String

  object HomeTeam {

    def apply(value: String): HomeTeam = value

    def unapply(homeTeam: HomeTeam): String = homeTeam
  }

  given CanEqual[HomeTeam, HomeTeam] = CanEqual.derived
  implicit val homeTeamEncoder: JsonEncoder[HomeTeam] = JsonEncoder.string
  implicit val homeTeamDecoder: JsonDecoder[HomeTeam] = JsonDecoder.string
}

object AwayTeams {

  opaque type AwayTeam = String

  object AwayTeam {

    def apply(value: String): AwayTeam = value

    def unapply(awayTeam: AwayTeam): String = awayTeam
  }

  given CanEqual[AwayTeam, AwayTeam] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayTeam] = JsonEncoder.string
  implicit val awayTeamDecoder: JsonDecoder[AwayTeam] = JsonDecoder.string
}

object GameDates {

  opaque type GameDate = LocalDate

  object GameDate {

    def apply(value: LocalDate): GameDate = value

    def unapply(gameDate: GameDate): LocalDate = gameDate
  }

  given CanEqual[GameDate, GameDate] = CanEqual.derived
  implicit val gameDateEncoder: JsonEncoder[GameDate] = JsonEncoder.localDate
  implicit val gameDateDecoder: JsonDecoder[GameDate] = JsonDecoder.localDate
}

object SeasonYears {

  opaque type SeasonYear <: Int = Int

  object SeasonYear {

    def apply(year: Int): SeasonYear = year

    def safe(value: Int): Option[SeasonYear] =
      Option.when(value >= 1876 && value <= LocalDate.now.getYear)(value)

    def unapply(seasonYear: SeasonYear): Int = seasonYear
  }

  given CanEqual[SeasonYear, SeasonYear] = CanEqual.derived
  implicit val seasonYearEncoder: JsonEncoder[SeasonYear] = JsonEncoder.int
  implicit val seasonYearDecoder: JsonDecoder[SeasonYear] = JsonDecoder.int
}

// For elos and ratings probabilities
object Probs {

  opaque type Prob = Float

  object Prob {

    def apply(value: Float): Prob = value

    def safe(value: Float): Option[Prob] =
      Option.when(value >= 0 && value <= 1)(value)

    def unapply(prob: Prob): Float = prob
  }

  given CanEqual[Prob, Prob] = CanEqual.derived
  implicit val probEncoder: JsonEncoder[Prob] = JsonEncoder.float
  implicit val probDecoder: JsonDecoder[Prob] = JsonDecoder.float
}

// For elos and ratings
object Ratings {

  opaque type Rating = Float

  object Rating {

    def apply(value: Float): Rating = value

    def safe(value: Float): Option[Rating] =
      Option.when(value >= 0)(value)

    def unapply(rating: Rating): Float = rating
  }

  given CanEqual[Rating, Rating] = CanEqual.derived
  implicit val ratingEncoder: JsonEncoder[Rating] = JsonEncoder.float
  implicit val ratingDecoder: JsonDecoder[Rating] = JsonDecoder.float
}

object Scores {

  opaque type Score = Int

  object Score {

    def apply(value: Int): Score = value

    def safe(value: Int): Option[Score] =
      Option.when(value >= 0)(value)

    def unapply(score: Score): Int = score
  }

  given CanEqual[Score, Score] = CanEqual.derived
  implicit val scoreEncoder: JsonEncoder[Score] = JsonEncoder.int
  implicit val scoreDecoder: JsonDecoder[Score] = JsonDecoder.int

}

import GameDates.*
import SeasonYears.*
import HomeTeams.*
import AwayTeams.*
import Probs.*
import Ratings.*
import Scores.*


final case class Game(
    date: GameDate,
    season: SeasonYear,
    homeTeam: HomeTeam,
    awayTeam: AwayTeam,
    elo1_pre: Option[Rating],
    elo2_pre: Option[Rating],
    elo_prob1: Option[Prob],
    elo_prob2: Option[Prob]
)

object Game {

  given CanEqual[Game, Game] = CanEqual.derived
  implicit val gameEncoder: JsonEncoder[Game] = DeriveJsonEncoder.gen[Game]
  implicit val gameDecoder: JsonDecoder[Game] = DeriveJsonDecoder.gen[Game]

  def unapply(
      game: Game
  ): (GameDate, SeasonYear, HomeTeam, AwayTeam, Option[Rating], Option[Rating], Option[Prob], Option[Prob]) =
    (
      game.date,
      game.season,
      game.homeTeam,
      game.awayTeam,
      game.elo1_pre,
      game.elo2_pre,
      game.elo_prob1,
      game.elo_prob2
    )

  // a custom decoder from a tuple
  type Row = (String, Int, String, String, Option[Float], Option[Float], Option[Float], Option[Float])

  extension (g: Game)
    def toRow: Row =
      val (d, y, h, a, e1p, e2p, ep1, ep2) = Game.unapply(g)
      (
        GameDate.unapply(d).toString,
        SeasonYear.unapply(y),
        HomeTeam.unapply(h),
        AwayTeam.unapply(a),
        e1p.map(Rating.unapply),
        e2p.map(Rating.unapply),
        ep1.map(Prob.unapply),
        ep2.map(Prob.unapply)
      )

  implicit val jdbcDecoder: JdbcDecoder[Game] = JdbcDecoder[Row]().map[Game] {
    t =>
      val (date, season, home, away, elo1_pre, elo2_pre, elo_prob1, elo_prob2) = t
      Game(
        GameDate(LocalDate.parse(date)),
        SeasonYear(season),
        HomeTeam(home),
        AwayTeam(away),
        elo1_pre.map(Rating(_)),
        elo2_pre.map(Rating(_)),
        elo_prob1.map(Prob(_)),
        elo_prob2.map(Prob(_))
      )
  }
}
