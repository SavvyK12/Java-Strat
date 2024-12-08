import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class MeanReversion {

    public static void main(String[] args) {
        // Step 1: Extract stock data
        String csvFile = "stock_data.csv"; // Replace with your file path
        Map<String, List<Stock>> stockDataMap = StockDataExtractor.extractData(csvFile);

        // Step 2: Backtest the mean reversion strategy
        List<Double> portfolioDailyReturns = MeanReversionTradingStrategy.backtestStrategy(stockDataMap);

        // Step 3: Perform portfolio optimization using Monte Carlo simulation
        PortfolioOptimization.optimizePortfolio(stockDataMap);

        // Step 4: Evaluate backtesting metrics
        backtest backtest = new backtest(portfolioDailyReturns, 0.04); // Assuming 4% annual risk-free rate

        // Step 5: Calculate portfolio values based on daily returns and initial capital
        List<Double> portfolioValues = calculatePortfolioValues(portfolioDailyReturns, 100000);

        System.out.println("Sharpe Ratio: " + backtest.calculateSharpeRatio());
        System.out.println("Maximum Drawdown: " + backtest.calculateMaxDrawdown(portfolioValues));
        System.out.println("Win Rate: " + backtest.calculateWinRate());
        System.out.println("Sortino Ratio: " + backtest.calculateSortinoRatio());
        System.out.println("Annualized Returns: " + backtest.calculateAnnualizedReturns());
    }

    // Utility method to calculate portfolio values based on daily returns
    public static List<Double> calculatePortfolioValues(List<Double> dailyReturns, double initialCapital) {
        List<Double> portfolioValues = new ArrayList<>();
        double portfolioValue = initialCapital;
        portfolioValues.add(portfolioValue);

        for (double dailyReturn : dailyReturns) {
            portfolioValue *= (1 + dailyReturn); // Compounding daily returns
            portfolioValues.add(portfolioValue);
        }

        return portfolioValues;
    }
}

// Mean Reversion Trading Strategy
class MeanReversionTradingStrategy {

    public static List<Double> backtestStrategy(Map<String, List<Stock>> stockDataMap) {
        List<Double> dailyPortfolioReturns = new ArrayList<>();
        double portfolioValue = 100000;

        for (int day = 19; day < stockDataMap.values().iterator().next().size(); day++) { // Need at least 20 days for SMA
            double dailyReturn = 0;
            int stockCount = stockDataMap.size();

            for (String ticker : stockDataMap.keySet()) {
                List<Stock> stockPrices = stockDataMap.get(ticker);
                List<BigDecimal> closePrices = getClosingPrices(stockPrices);

                // Use SMA class
                SMA smaCalculator = new SMA(20); // Create an SMA calculator for 20-day period
                List<BigDecimal> smaValues = smaCalculator.calculate(closePrices);

                if (day - 19 < smaValues.size()) { // Ensure valid SMA value is available
                    BigDecimal currentPrice = stockPrices.get(day).getAdjClose();
                    BigDecimal sma20 = smaValues.get(day - 19);

                    // Linear Regression for price prediction
                    LinearRegression linearRegression = new LinearRegression(closePrices, 5); // Use last 5 days for prediction
                    double predictedPrice = linearRegression.predict(closePrices.size() + 1); // Predict next day's price

                    // Mean reversion signals based on both SMA and Linear Regression
                    if (currentPrice.compareTo(sma20.multiply(BigDecimal.valueOf(1.05))) > 0 || currentPrice.doubleValue() > predictedPrice) {
                        dailyReturn -= 0.01; // Short signal: Price is significantly above SMA or predicted price
                    } else if (currentPrice.compareTo(sma20.multiply(BigDecimal.valueOf(0.95))) < 0 || currentPrice.doubleValue() < predictedPrice) {
                        dailyReturn += 0.01; // Buy signal: Price is significantly below SMA or predicted price
                    }
                }
            }

            dailyPortfolioReturns.add(dailyReturn / stockCount); // Average daily return across stocks
        }

        return dailyPortfolioReturns;
    }

    private static List<BigDecimal> getClosingPrices(List<Stock> stockPrices) {
        List<BigDecimal> prices = new ArrayList<>();
        for (Stock stock : stockPrices) {
            prices.add(stock.getAdjClose());
        }
        return prices;
    }
}

// Linear Regression class for price prediction
class LinearRegression {
    private final List<BigDecimal> data;
    private final int period;

    public LinearRegression(List<BigDecimal> data, int period) {
        this.data = data;
        this.period = period;
    }

    public double predict(int futureIndex) {
        int start = data.size() - period;
        List<BigDecimal> recentData = data.subList(start, data.size());

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = recentData.size();

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = recentData.get(i).doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Calculate the slope (m) and intercept (b) of the regression line
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Predict the value at futureIndex
        return slope * futureIndex + intercept;
    }
}






