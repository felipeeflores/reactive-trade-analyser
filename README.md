# reactive-trade-analyser

Simple app using Monix functional reactive streams (Observable).

Monix: https://monix.io/

## Pre-requisites

- Java Development Kit 8
- [SBT](https://www.scala-sbt.org/1.0/docs/Setup.html)

## Data 

Data is to be downloaded from https://www.asxhistoricaldata.com/ and it should be extracted conserving its compressed form
internal structure.

## Running

```
> # Environment variable to specify data directory (assuming Mac or Linux). Defaults to <current-folder>/data 
> export MARKET_DATA_DIR=<path-to-stock-data>
>
> # Run only once (will take a while the first time)
> sbt assembly 
>
> # then run: 
> ./analyser $SYMBOLS [[optional] $DAYS] [[optional] IND=$INDICATORS]

```

where:
- $SYMBOLS: List of ticker codes separated by comma (i.e. AEE, AGO, PLS)
- $DAYS: Size of the data frame in days; it is provided as an integer
- $INDICATORS: List of supported indicators that user want to see included in the final report (i.e. 10SMA, 50SMA, 10EMA)

## Architecture

The application is designed/built as an ETL process where the loading is just a dump to the standard output.

The reactive pipeline built executes the following self-explanatory steps:

1. Data extraction 
2. Data parsing (and validation)
3. Data enrichment (stock indicators computation, only 10SMA and 50SMA currently supported)
4. Data aggregation
5. Reporting
