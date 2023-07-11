# MLB REST API

## API Description

This is a REST API that provides information about Major League Baseball (MLB) teams, and games or even predictions ! The API is built using the ZIO framework -- which is a functional framework for Scala -- h2 and a set of ZIO libraries such as `zio-jdbc`, `zio-streams`, `zio-json`, or `zio-http`

## How to run the API

To run the project you need to have sbt installed on your computer. You can download it here : https://www.scala-sbt.org/download.html. After that you just need to clone the project and run the command sbt run in the root of the project.

Before running it's recommended to clean the project with the command sbt clean.

```
sbt clean
```

To compile the project you can use the command sbt compile.

```
sbt compile
```

To run the project you can use the command sbt run.

```
sbt run
```

To run the tests you can use the command sbt test.

```
sbt test
```

## Dataset Description

The "Major League Baseball Dataset" from Kaggle is a comprehensive collection of data related to Major League Baseball (MLB) games, players, teams, and statistics. The dataset contains information about game-by-game Elo ratings and forecasts back to 1871. You can visit the Kaggle page for a more detailed description of the dataset.

## API Endpoints

# MLB Endpoints

This Scala code defines several endpoints for an MLB (Major League Baseball) API. The endpoints are responsible for handling HTTP requests and interacting with a database using ZIO and ZIO JDBC.

## Package Structure

The endpoints are defined within the `mlb` package and organized in the `Endpoints` object.

## Endpoint List

The following endpoints are defined:

1. **Initialization Endpoint**

   - **Path:** GET /init
   - **Description:** Initializes the database by dropping existing tables and running the initialization logic.
   - **Response:** Returns a success message if the database is initialized successfully or an error message if initialization fails.

2. **Game Count Endpoint**

   - **Path:** GET /games/count
   - **Description:** Retrieves the count of games in the historical data.
   - **Response:** Returns the game count if there are games available or a message indicating no games in the historical data.

3. **Games by Season Endpoint**

   - **Path:** GET /games/{season}
   - **Description:** Retrieves the games for a specific season.
   - **Response:** Returns the games for the given season in JSON format if available or a message indicating no games found.

4. **Games Won by Team Endpoint**

   - **Path:** GET /games/win/{team1}/{team2}
   - **Description:** Retrieves the number of victories for one team against the other.
   - **Response:** Returns the number of victories as JSON if available or a message indicating no games found.

## Data structures

### Database

We've made the decision to implement only one table in our database that contains almost all the columns of the dataset. The table is called `games` and contains the following columns:

- date Date of game
- season Year of season
- team1 Abbreviation for home team
- team2 Abbreviation for away team
- elo1_pre Home team's Elo rating before the game
- elo2_pre Away team's Elo rating before the game
- elo_prob1 Home team's probability of winning according to Elo ratings
- elo_prob2 Away team's probability of winning according to Elo ratings
- elo1_post Home team's Elo rating after the game
- elo2_post Away team's Elo rating after the game
- rating1_pre Home team's rating before the game
- rating2_pre Away team's rating before the game
- rating_prob1 Home team's probability of winning according to team ratings and starting pitchers
- rating_prob2 Away team's probability of winning according to team ratings and starting pitchers
- rating1_post Home team's rating after the game
  rating2_post Away team's rating after the game
- score1 Home team's score
- score2 Away team's score

We kept only columns with which we can do some interesting queries.

### Data structures in Scala

We've decomposed our games rows into differents objects.

**Teams** object contains the a team in a String format

**SeasonYears** object contains the season of the game in an Integer format. We've decided to specify also that with a safe def to be sure that the season is between 1876 and the current year.

**Teams** object: This object represents a team in the MLB. It defines the `HomeTeam` and `AwayTeam` opaque types, which wrap a team name as a `String`. These opaque types enhance type safety by preventing the accidental mixing of home and away team names.

**GameDates** object: This object represents the date of an MLB game. It defines the `GameDate` opaque type, which wraps a date value as a `LocalDate`. Using an opaque type ensures that the date is always represented correctly and prevents confusion with other date-related values.

**SeasonYears** object: This object represents the season of an MLB game. It defines the `SeasonYear` opaque type, which is a subtype of `Int`. The `SeasonYear` type provides additional safety by restricting the range of valid years to between 1876 (the earliest MLB season) and the current year. The `safe` function ensures that only valid season years can be created.

**Probs** object: This object represents probabilities in MLB games. It defines the `Prob` opaque type, which wraps a probability value as a `Float`. The `Prob` type includes a `safe` function that allows creating `Prob` instances only for values between 0 and 1, ensuring that probabilities are within a valid range.

**Ratings** object: This object represents ratings in MLB games. It defines the `Rating` opaque type, which wraps a rating value as a `Float`. The `Rating` type includes a `safe` function that allows creating `Rating` instances only for values greater than or equal to 0. This distinction between `Ratings` and `Probs` ensures clear differentiation between rating values and probability values in the game data.

**Scores** object: This object represents scores in MLB games. It defines the `Score` opaque type, which wraps a score value as an `Int`. The `Score` type includes a `safe` function that allows creating `Score` instances only for non-negative values. While it is currently used for storing team scores (`score1` and `score2`), it can also be used to represent any other integer-based scores in the future.

**Game** case class: This case class represents an MLB game with various attributes. It includes fields such as `date`, `season`, `homeTeam`, `awayTeam`, `elo1_pre`, `elo2_pre`, `elo_prob1`, `elo_prob2`, `elo1_post`, `elo2_post`, `rating1_pre`, `rating2_pre`, `rating_prob1`, `rating_prob2`, `rating1_post`, `rating2_post`, `score1`, and `score2`. Each field corresponds to a specific aspect of an MLB game, such as date, teams, ratings, probabilities, and scores. The use of the opaque types ensures that the data is correctly typed and prevents mixing incompatible values. We've also decided to represent some values as Optional values. This is because some values are not always available in the dataset. For example, the `elo1_pre` and `elo2_pre` values are not available for the first game of the season. In such cases, the values are represented as `None`. This also allows us to distinguish between missing values and values that are actually 0.

**Wins** case class: This case class is used to represent the number of wins for a team against another team. It includes two fields such as `season` and `wins`. The `season` field represents the season for which the wins are calculated, while the `wins` field represents the number of wins for the team against the selected one. The `wins` field is represented as an `Score` opaque type, which ensures that the value is always non-negative. And season is represented as a `SeasonYear` opaque type, which ensures that the value is always a valid season year.