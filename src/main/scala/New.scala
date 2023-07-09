package mlb

import zio.json._
import zio.jdbc._
import java.time.LocalDate

import java.time.format.DateTimeFormatter

// Define the custom types
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
    def apply(value: Int): SeasonYear = value
    def safe(value: Int): Option[SeasonYear] =
      Option.when(value >= 1876 && value <= LocalDate.now.getYear)(value)
    def unapply(seasonYear: SeasonYear): Int = seasonYear
  }
  given CanEqual[SeasonYear, SeasonYear] = CanEqual.derived
  implicit val seasonYearEncoder: JsonEncoder[SeasonYear] = JsonEncoder.int
  implicit val seasonYearDecoder: JsonDecoder[SeasonYear] = JsonDecoder.int
}

object TeamNames {
  opaque type TeamName = String
  object TeamName {
    def apply(value: String): TeamName = value
    def unapply(teamName: TeamName): String = teamName
  }
  given CanEqual[TeamName, TeamName] = CanEqual.derived
  implicit val teamNameEncoder: JsonEncoder[TeamName] = JsonEncoder.string
  implicit val teamNameDecoder: JsonDecoder[TeamName] = JsonDecoder.string
}

object EloRatings {
  opaque type EloRating = Float
  object EloRating {
    def apply(value: Float): EloRating = value
    def unapply(eloRating: EloRating): Float = eloRating
  }
  given CanEqual[EloRating, EloRating] = CanEqual.derived
  implicit val eloRatingEncoder: JsonEncoder[EloRating] = JsonEncoder.float
  implicit val eloRatingDecoder: JsonDecoder[EloRating] = JsonDecoder.float
}

object EloProbs {
  opaque type EloProb = Float
  object EloProb {
    def apply(value: Float): EloProb = value
    def unapply(eloProb: EloProb): Float = eloProb
  }
  given CanEqual[EloProb, EloProb] = CanEqual.derived
  implicit val eloProbEncoder: JsonEncoder[EloProb] = JsonEncoder.float
  implicit val eloProbDecoder: JsonDecoder[EloProb] = JsonDecoder.float
}

object Ratings {
  opaque type Rating = Float
  object Rating {
    def apply(value: Float): Rating = value
    def unapply(rating: Rating): Float = rating
  }
  given CanEqual[Rating, Rating] = CanEqual.derived
  implicit val ratingEncoder: JsonEncoder[Rating] = JsonEncoder.float
  implicit val ratingDecoder: JsonDecoder[Rating] = JsonDecoder.float
}

object PitcherNames {
  opaque type PitcherName = String
  object PitcherName {
    def apply(value: String): PitcherName = value
    def unapply(pitcherName: PitcherName): String = pitcherName
  }
  given CanEqual[PitcherName, PitcherName] = CanEqual.derived
  implicit val pitcherNameEncoder: JsonEncoder[PitcherName] = JsonEncoder.string
  implicit val pitcherNameDecoder: JsonDecoder[PitcherName] = JsonDecoder.string
}

object PitcherRGS {
  opaque type PitcherRgs = Option[Float]
  object PitcherRgs {
    def apply(value: Option[Float]): PitcherRgs = value
    def unapply(pitcherRgs: PitcherRgs): Option[Float] = pitcherRgs
  }
  given CanEqual[PitcherRgs, PitcherRgs] = CanEqual.derived
  implicit val pitcherRgsEncoder: JsonEncoder[PitcherRgs] =
    JsonEncoder[Option[Float]]
  implicit val pitcherRgsDecoder: JsonDecoder[PitcherRgs] =
    JsonDecoder[Option[Float]]
}

object PitcherAdjs {
  opaque type PitcherAdj = Option[Float]
  object PitcherAdj {
    def apply(value: Option[Float]): PitcherAdj = value
    def unapply(pitcherAdj: PitcherAdj): Option[Float] = pitcherAdj
  }
  given CanEqual[PitcherAdj, PitcherAdj] = CanEqual.derived
  implicit val pitcherAdjEncoder: JsonEncoder[PitcherAdj] =
    JsonEncoder[Option[Float]]
  implicit val pitcherAdjDecoder: JsonDecoder[PitcherAdj] =
    JsonDecoder[Option[Float]]
}

object RatingProbs {
  opaque type RatingProb = Float
  object RatingProb {
    def apply(value: Float): RatingProb = value
    def unapply(ratingProb: RatingProb): Float = ratingProb
  }
  given CanEqual[RatingProb, RatingProb] = CanEqual.derived
  implicit val ratingProbEncoder: JsonEncoder[RatingProb] = JsonEncoder.float
  implicit val ratingProbDecoder: JsonDecoder[RatingProb] = JsonDecoder.float
}

object Scores {
  opaque type Score = Option[Int]
  object Score {
    def apply(value: Option[Int]): Score = value
    def unapply(score: Score): Option[Int] = score
  }
  given CanEqual[Score, Score] = CanEqual.derived
  implicit val scoreEncoder: JsonEncoder[Score] = JsonEncoder[Option[Int]]
  implicit val scoreDecoder: JsonDecoder[Score] = JsonDecoder[Option[Int]]
}

// And now the game definition
import GameDates.*
import SeasonYears.*
import TeamNames.*
import EloRatings.*
import EloProbs.*
import Ratings.*
import PitcherNames.*
import PitcherRGS.*
import PitcherAdjs.*
import RatingProbs.*
import Scores.*

final case class Game(
    date: GameDate,
    season: SeasonYear,
    team1: TeamName,
    team2: TeamName,
    elo1Pre: EloRating,
    elo2Pre: EloRating,
    eloProb1: EloProb,
    eloProb2: EloProb,
    elo1Post: EloRating,
    elo2Post: EloRating,
    rating1Pre: Rating,
    rating2Pre: Rating,
    pitcher1: PitcherName,
    pitcher2: PitcherName,
    pitcher1Rgs: PitcherRgs,
    pitcher2Rgs: PitcherRgs,
    pitcher1Adj: PitcherAdj,
    pitcher2Adj: PitcherAdj,
    ratingProb1: RatingProb,
    ratingProb2: RatingProb,
    rating1Post: Rating,
    rating2Post: Rating,
    score1: Score,
    score2: Score
)

object Game {

  given CanEqual[Game, Game] = CanEqual.derived
  implicit val gameEncoder: JsonEncoder[Game] = DeriveJsonEncoder.gen[Game]
  implicit val gameDecoder: JsonDecoder[Game] = DeriveJsonDecoder.gen[Game]

  def unapply(game: Game): (
      GameDate,
      SeasonYear,
      TeamName,
      TeamName,
      EloRating,
      EloRating,
      EloProb,
      EloProb,
      EloRating,
      EloRating,
      Rating,
      Rating,
      PitcherName,
      PitcherName,
      PitcherRgs,
      PitcherRgs,
      PitcherAdj,
      PitcherAdj,
      RatingProb,
      RatingProb,
      Rating,
      Rating,
      Score,
      Score
  ) =
    (
      game.date,
      game.season,
      game.team1,
      game.team2,
      game.elo1Pre,
      game.elo2Pre,
      game.eloProb1,
      game.eloProb2,
      game.elo1Post,
      game.elo2Post,
      game.rating1Pre,
      game.rating2Pre,
      game.pitcher1,
      game.pitcher2,
      game.pitcher1Rgs,
      game.pitcher2Rgs,
      game.pitcher1Adj,
      game.pitcher2Adj,
      game.ratingProb1,
      game.ratingProb2,
      game.rating1Post,
      game.rating2Post,
      game.score1,
      game.score2
    )

  type Row = (
      String,
      Int,
      String,
      String,
      Float,
      Float,
      Float,
      Float,
      Float,
      Float,
      Float,
      Float,
      String,
      String,
      Option[Float],
      Option[Float],
      Option[Float],
      Option[Float],
      Float,
      Float,
      Float,
      Float,
      Option[Int],
      Option[Int]
  )

  extension (g: Game)
    def toRow: Row =
      val (
        d,
        sy,
        t1,
        t2,
        e1Pre,
        e2Pre,
        eProb1,
        eProb2,
        e1Post,
        e2Post,
        r1Pre,
        r2Pre,
        p1,
        p2,
        p1Rgs,
        p2Rgs,
        p1Adj,
        p2Adj,
        rProb1,
        rProb2,
        r1Post,
        r2Post,
        s1,
        s2
      ) = Game.unapply(g)
    (
      GameDate.unapply(d).format(DateTimeFormatter.ISO_DATE),
      SeasonYear.unapply(sy),
      TeamName.unapply(t1),
      TeamName.unapply(t2),
      EloRating.unapply(e1Pre),
      EloRating.unapply(e2Pre),
      EloProb.unapply(eProb1),
      EloProb.unapply(eProb2),
      EloRating.unapply(e1Post),
      EloRating.unapply(e2Post),
      Rating.unapply(r1Pre),
      Rating.unapply(r2Pre),
      PitcherName.unapply(p1),
      PitcherName.unapply(p2),
      PitcherRgs.unapply(p1Rgs),
      PitcherRgs.unapply(p2Rgs),
      PitcherAdj.unapply(p1Adj),
      PitcherAdj.unapply(p2Adj),
      RatingProb.unapply(rProb1),
      RatingProb.unapply(rProb2),
      Rating.unapply(r1Post),
      Rating.unapply(r2Post),
      Score.unapply(s1),
      Score.unapply(s2)
    )

    implicit val jdbcDecoder: JdbcDecoder[Game] = JdbcDecoder[Row]().map[Game] {
      t =>
        val (
          date,
          season,
          team1,
          team2,
          elo1Pre,
          elo2Pre,
          eloProb1,
          eloProb2,
          elo1Post,
          elo2Post,
          rating1Pre,
          rating2Pre,
          pitcher1,
          pitcher2,
          pitcher1Rgs,
          pitcher2Rgs,
          pitcher1Adj,
          pitcher2Adj,
          ratingProb1,
          ratingProb2,
          rating1Post,
          rating2Post,
          score1,
          score2
        ) = t
        Game(
          GameDate(LocalDate.parse(date)),
          SeasonYear(season),
          TeamName(team1),
          TeamName(team2),
          EloRating(elo1Pre),
          EloRating(elo2Pre),
          EloProb(eloProb1),
          EloProb(eloProb2),
          EloRating(elo1Post),
          EloRating(elo2Post),
          Rating(rating1Pre),
          Rating(rating2Pre),
          PitcherName(pitcher1),
          PitcherName(pitcher2),
          PitcherRgs(pitcher1Rgs),
          PitcherRgs(pitcher2Rgs),
          PitcherAdj(pitcher1Adj),
          PitcherAdj(pitcher2Adj),
          RatingProb(ratingProb1),
          RatingProb(ratingProb2),
          Rating(rating1Post),
          Rating(rating2Post),
          Score(score1),
          Score(score2)
        )
    }
}

val games: List[Game] = List(
  Game(
    GameDate(LocalDate.parse("2023-07-09")),
    SeasonYear(2023),
    TeamName("NYM"),
    TeamName("ATL"),
    EloRating(1500.0f),
    EloRating(1500.0f),
    EloProb(0.5f),
    EloProb(0.5f),
    EloRating(1500.0f),
    EloRating(1500.0f),
    Rating(0.0f),
    Rating(0.0f),
    PitcherName("John"),
    PitcherName("Doe"),
    PitcherRgs(Some(0.0f)),
    PitcherRgs(Some(0.0f)),
    PitcherAdj(Some(0.0f)),
    PitcherAdj(Some(0.0f)),
    RatingProb(0.5f),
    RatingProb(0.5f),
    Rating(0.0f),
    Rating(0.0f),
    Score(Some(3)),
    Score(Some(2))
  )
)
