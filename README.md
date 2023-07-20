# MLB REST API

## API Description

This is a REST API that provides information about Major League Baseball (MLB) teams, and games or even predictions ! The API is built using the ZIO framework -- which is a functional framework for Scala -- h2 and a set of ZIO libraries such as `zio-jdbc`, `zio-streams`, `zio-json`, or `zio-http`

## How to run the API

To run the project you need to have sbt installed on your computer. You can download it here : https://www.scala-sbt.org/download.html. After that you just need to clone the project and run the command sbt run in the root of the project.

### Configuration

1. Insert in the Constants.scala file the path to the csv file.
2. Replace the path the MlbAPI file [line 33](https://github.com/bastien707/mlbapi/blob/2f284119bc9ef83ffbd4796c921d941227f94131/src/main/scala/MlbAPI.scala#L33) replace "Elop'X'" by the one you previously defined.
3. Same in the file Database.scala [line 48](https://github.com/bastien707/mlbapi/blob/2f284119bc9ef83ffbd4796c921d941227f94131/src/main/scala/Database.scala#L49) replace "Elop'X'" by the one you previously defined.

### Run the project

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

To deletes all generated files (in the target directory).

```
sbt clean
```

To run the tests you can use the command sbt test.

```
sbt test
```

## Dataset Description

The "Major League Baseball Dataset" from Kaggle is a comprehensive collection of data related to Major League Baseball (MLB) games, players, teams, and statistics. The dataset contains information about game-by-game Elo ratings and forecasts back to 1871. You can visit the Kaggle page for a more detailed description of the dataset.

# API Endpoints

## MLB Endpoints

This Scala code defines several endpoints for an MLB (Major League Baseball) API. The endpoints are responsible for handling HTTP requests and interacting with a database using ZIO and ZIO JDBC.

By default the API automatically loads the dataset from the csv file. You are free query localhost/init to initialize the database with the dataset.  

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
  
4. **Games winning probabilities Endpoint**

   - **Path:** GET /games/win/{team1}/{team2}
   - **Description:** Retrieves the number of victories for one team against the other.
   - **Response:** Returns the number of victories as JSON if available or a message indicating no games found.
  
5. **Games Won by Team Endpoint**

   - **Path:** GET /games/probs/{team1}/{team2}
   - **Description:** Compute the probability of win for the team1 against team2.
   - **Response:** Returns the probabilities in the sentence "Probability of {team1} winning against {team2} is {probability}".
  
6. **Team top 10 Endpoint**

   - **Path:** GET /ranking/{season}
   - **Description:** Retrieve the top ten of teams by on their elo on a season.
   - **Response:**  Returns the top 10 for the given season in JSON format if available or a message indicating no games found
  
7. **Team statistic historic Endpoint**

   - **Path:** GET /historic
   - **Description:** Retrieve the number of win, lost and draw match by team and by season.
   - **Response:**  Returns the number of win, lost and draw match by team and by season in JSON format using Historic object. If no games found, returns a message indicating no games found.
   
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
- rating2_post Away team's rating after the game
- score1 Home team's score
- score2 Away team's score

We kept only columns with which we can do some interesting queries.

### Data structures in Scala

We've decomposed our games rows into differents objects.

**SeasonYears** object contains the season of the game in an Integer format. We've decided to specify also that with a safe def to be sure that the season is between 1876 and the current year.

**Teams** object: This object represents a team in the MLB. It defines the `Team` opaque type, which wraps a team value as a `String`. Using an opaque type ensures that the team is always represented correctly and prevents confusion with other team-related values. Since there are two teams: away and home and there is no remarkable difference between them, we've decided to use only one object to represent them.

**GameDates** object: This object represents the date of an MLB game. It defines the `GameDate` opaque type, which wraps a date value as a `LocalDate`. Using an opaque type ensures that the date is always represented correctly and prevents confusion with other date-related values.

**SeasonYears** object: This object represents the season of an MLB game. It defines the `SeasonYear` opaque type, which is a subtype of `Int`. The `SeasonYear` type provides additional safety by restricting the range of valid years to between 1876 (the earliest MLB season) and the current year. The `safe` function ensures that only valid season years can be created.

**Probs** object: This object represents probabilities in MLB games. It defines the `Prob` opaque type, which wraps a probability value as a `Float`. The `Prob` type includes a `safe` function that allows creating `Prob` instances only for values between 0 and 1, ensuring that probabilities are within a valid range.

**Ratings** object: This object represents ratings in MLB games. It defines the `Rating` opaque type, which wraps a rating value as a `Float`. The `Rating` type includes a `safe` function that allows creating `Rating` instances only for values greater than or equal to 0. This distinction between `Ratings` and `Probs` ensures clear differentiation between rating values and probability values in the game data.

**Scores** object: This object represents scores in MLB games. It defines the `Score` opaque type, which wraps a score value as an `Int`. The `Score` type includes a `safe` function that allows creating `Score` instances only for non-negative values. While it is currently used for storing team scores (`score1` and `score2`), it can also be used to represent any other integer-based scores in the future.

**Game** case class: This case class represents an MLB game with various attributes. It includes fields such as `date`, `season`, `homeTeam`, `awayTeam`, `elo1_pre`, `elo2_pre`, `elo_prob1`, `elo_prob2`, `elo1_post`, `elo2_post`, `rating1_pre`, `rating2_pre`, `rating_prob1`, `rating_prob2`, `rating1_post`, `rating2_post`, `score1`, and `score2`. Each field corresponds to a specific aspect of an MLB game, such as date, teams, ratings, probabilities, and scores. The use of the opaque types ensures that the data is correctly typed and prevents mixing incompatible values. We've also decided to represent some values as Optional values. This is because some values are not always available in the dataset. For example, the `elo1_pre` and `elo2_pre` values are not available for the first game of the season. In such cases, the values are represented as `None`. This also allows us to distinguish between missing values and values that are actually 0.

**Wins** case class: This case class is used to represent the number of wins for a team against another team. It includes two fields such as `season` and `wins`. The `season` field represents the season for which the wins are calculated, while the `wins` field represents the number of wins for the team against the selected one. The `wins` field is represented as an `Score` opaque type, which ensures that the value is always non-negative. And season is represented as a `SeasonYear` opaque type, which ensures that the value is always a valid season year.

**Rankings** case class: This case class is used to represent the rankings of teams in a season. It includes two fields such as `position`, `teams`, `elo`, `season`. The `position` field represents the position of the team in the rankings, while the `teams` field represents the team name. The `elo` field represents the Elo rating of the team, while the `season` field represents the season for which the rankings are calculated. The `position` field is represented as an `Int`, while the `elo` field is represented as a `Rating` opaque type. The `season` field is represented as a `SeasonYear` opaque type, which ensures that the value is always a valid season year. And the `teams` field is represented as a `Teams` opaque type, which ensures that the value is always a valid team name.

**Historics** case class: This case class is used to represent the historic of a team in a season. It includes four fields such as `season`, `team`, `wins`, `lost`, `draw`. The `season` field represents the season for which the historic is calculated, while the `team` field represents the team name. The `wins`, `lost` and `draw` fields represent the number of wins, lost and draw for the team in the season. The `season` field is represented as a `SeasonYear` opaque type, which ensures that the value is always a valid season year. And the `team` field is represented as a `Teams` opaque type, which ensures that the value is always a valid team name. The `wins`, `lost` and `draw` fields are represented as `Score` opaque types, which ensures that the values are always non-negative. We created this object because we wanted to have a better representation of the data. We wanted to have a representation of the historic of a team in a season corresponding to the endpoints we wanted to create.

## Tests
To perform our unit tests, we used the Munit framework. Munit is a lightweight, easy-to-use, and highly expressive testing framework designed specifically for Scala and Scala.js. It provides a concise and intuitive syntax for writing unit tests, making the process of code development and maintenance easier. Additionally, Munit supports test parallelization, enabling faster test execution on multicore machines. With these features, Munit offers a powerful and efficient way to ensure code quality in Scala by quickly detecting errors and ensuring the proper functioning of different code units. This explains our choice.

Our tests follow a basic pattern: they make HTTP requests and verify the responses to ensure the proper functioning of the application. They verify scenarios such as retrieving games for a given season, searching for wins between two teams, calculating win probabilities, retrieving rankings, and accessing the historical data.
