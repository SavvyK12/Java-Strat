import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Main Class
public class TradingSystem {

    public static void main(String[] args) {
        // Extract stock data
        String csvFile = "stock_data.csv";
        Map<String, List<Stock>> stockDataMap = StockDataExtractor.extractData(csvFile);

        // Backtest the strategy
        List<Double> portfolioDailyReturns = TradingStrategy.backtestStrategy(stockDataMap);

        // Perform portfolio optimization using Monte Carlo simulation
        PortfolioOptimization.optimizePortfolio(stockDataMap);

        // Backtesting metrics (using backtest class from your existing file)
        backtest backtest = new backtest(portfolioDailyReturns, 0.04);

        // Calculate portfolio values based on daily returns and initial capital
        List<Double> portfolioValues = calculatePortfolioValues(portfolioDailyReturns, 100000);

        System.out.println("Sharpe Ratio: " + backtest.calculateSharpeRatio());
        System.out.println("Maximum Drawdown: " + backtest.calculateMaxDrawdown(portfolioValues));
        System.out.println("Win Rate: " + backtest.calculateWinRate());
        System.out.println("Sortino Ratio: " + backtest.calculateSortinoRatio());
        System.out.println("Annualized Returns: " + backtest.calculateAnnualizedReturns());
    }

    // Calculate portfolio values based on daily returns and initial capital
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

// StockDataExtractor
class StockDataExtractor {
    public static Map<String, List<Stock>> extractData(String csvFile) {
        Map<String, List<Stock>> stockDataMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = Files.newBufferedReader(Paths.get(csvFile))) {
            String line;
            String[] headers = br.readLine().split(",");
            List<String> stockTickers = Arrays.asList(headers).subList(1, headers.length);

            for (String ticker : stockTickers) {
                stockDataMap.put(ticker, new ArrayList<>());
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                LocalDate date = LocalDate.parse(values[0], formatter);

                for (int i = 1; i < values.length; i++) {
                    String ticker = headers[i];
                    try {
                        BigDecimal adjClose = new BigDecimal(values[i]);
                        stockDataMap.get(ticker).add(new Stock(date, adjClose));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for stock " + ticker + " on date " + date);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return stockDataMap;
    }
}

// TradingStrategy
class TradingStrategy {
    public static List<Double> backtestStrategy(Map<String, List<Stock>> stockDataMap) {
        List<Double> dailyPortfolioReturns = new ArrayList<>();
        double portfolioValue = 100000;

        for (int day = 0; day < stockDataMap.values().iterator().next().size(); day++) {
            double dailyReturn = 0;
            int stockCount = stockDataMap.size();

            for (String ticker : stockDataMap.keySet()) {
                List<Stock> stockPrices = stockDataMap.get(ticker);
                if (day >= 26) {
                    RSI rsi = new RSI(14);
                    EMA ema12 = new EMA(10);
                    EMA ema26 = new EMA(20);

                    List<BigDecimal> closePrices = getClosingPrices(stockPrices);
                    BigDecimal rsiValue = rsi.calculate(closePrices).get(day - 26);
                    BigDecimal ema12Value = ema12.calculate(closePrices).get(day - 26);
                    BigDecimal ema26Value = ema26.calculate(closePrices).get(day - 26);

                    if (rsiValue.doubleValue() > 60 && ema26Value.compareTo(ema12Value) < 0) {
                        dailyReturn -= 0.01; // Short signal
                    } else if (rsiValue.doubleValue() < 40 && ema12Value.compareTo(ema26Value) < 0) {
                        dailyReturn += 0.01; // Buy signal
                    }
                }
            }

            dailyPortfolioReturns.add(dailyReturn / stockCount);
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

// PortfolioOptimization
class PortfolioOptimization {
    public static void optimizePortfolio(Map<String, List<Stock>> stockDataMap) {
        // Monte Carlo simulation to find optimal weights
        int numSimulations = 100;
        int numStocks = stockDataMap.size();
        Random random = new Random();
        BigDecimal bestSharpeRatio = BigDecimal.ZERO;
        List<BigDecimal> optimalWeights = new ArrayList<>();

        for (int sim = 0; sim < numSimulations; sim++) {
            List<BigDecimal> weights = generateRandomWeights(numStocks, random);
            PortfolioMetrics metrics = calculatePortfolioMetrics(stockDataMap, weights);

            if (metrics.getSharpeRatio().compareTo(bestSharpeRatio) > 0) {
                bestSharpeRatio = metrics.getSharpeRatio();
                optimalWeights = weights;
            }
        }

        System.out.println("Optimal Portfolio Weights:");
        for (BigDecimal weight : optimalWeights) {
            System.out.println(weight.multiply(BigDecimal.valueOf(100)) + "%");
        }
    }

    private static List<BigDecimal> generateRandomWeights(int numStocks, Random random) {
        List<BigDecimal> weights = new ArrayList<>();
        double total = 0;

        for (int i = 0; i < numStocks; i++) {
            double weight = random.nextDouble();
            total += weight;
            weights.add(BigDecimal.valueOf(weight));
        }

        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i).divide(BigDecimal.valueOf(total), BigDecimal.ROUND_HALF_UP));
        }

        return weights;
    }

    private static PortfolioMetrics calculatePortfolioMetrics(Map<String, List<Stock>> stockDataMap, List<BigDecimal> weights) {
        int numDays = stockDataMap.values().iterator().next().size();
        int numStocks = weights.size();
        BigDecimal riskFreeRate = BigDecimal.valueOf(0.0001); // Example daily risk-free rate (0.01%)

        List<List<BigDecimal>> dailyReturns = new ArrayList<>();
        List<BigDecimal> portfolioDailyReturns = new ArrayList<>();

        // Calculate daily returns for each stock
        for (String ticker : stockDataMap.keySet()) {
            List<Stock> stockPrices = stockDataMap.get(ticker);
            List<BigDecimal> stockDailyReturns = new ArrayList<>();

            for (int i = 1; i < stockPrices.size(); i++) {
                BigDecimal prevPrice = stockPrices.get(i - 1).getAdjClose();
                BigDecimal currPrice = stockPrices.get(i).getAdjClose();
                stockDailyReturns.add(currPrice.subtract(prevPrice).divide(prevPrice, BigDecimal.ROUND_HALF_UP));
            }
            dailyReturns.add(stockDailyReturns);
        }

        // Calculate portfolio daily returns
        for (int day = 0; day < numDays - 1; day++) {
            BigDecimal dailyReturn = BigDecimal.ZERO;
            for (int stock = 0; stock < numStocks; stock++) {
                dailyReturn = dailyReturn.add(dailyReturns.get(stock).get(day).multiply(weights.get(stock)));
            }
            portfolioDailyReturns.add(dailyReturn);
        }

        // Portfolio return
        BigDecimal portfolioReturn = portfolioDailyReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioDailyReturns.size()), BigDecimal.ROUND_HALF_UP);

        // Portfolio volatility
        BigDecimal meanReturn = portfolioReturn;
        BigDecimal variance = portfolioDailyReturns.stream()
                .map(r -> r.subtract(meanReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(portfolioDailyReturns.size()), BigDecimal.ROUND_HALF_UP);

        BigDecimal portfolioVolatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        // Sharpe ratio
        BigDecimal excessReturn = portfolioReturn.subtract(riskFreeRate);
        BigDecimal sharpeRatio = excessReturn.divide(portfolioVolatility, BigDecimal.ROUND_HALF_UP);

        // Maximum drawdown
        BigDecimal maxDrawdown = calculateMaxDrawdown(portfolioDailyReturns);

        // Annualized return
        BigDecimal annualizedReturn = BigDecimal.valueOf(
                Math.pow(portfolioReturn.add(BigDecimal.ONE).doubleValue(), 252.0 / numDays) - 1);

        return new PortfolioMetrics(portfolioReturn, portfolioVolatility, sharpeRatio, maxDrawdown, annualizedReturn);
    }

    private static BigDecimal calculateMaxDrawdown(List<BigDecimal> dailyReturns) {
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = BigDecimal.ONE;
        BigDecimal portfolioValue = BigDecimal.ONE;

        for (BigDecimal dailyReturn : dailyReturns) {
            portfolioValue = portfolioValue.multiply(dailyReturn.add(BigDecimal.ONE));
            peak = portfolioValue.max(peak);
            BigDecimal drawdown = peak.subtract(portfolioValue).divide(peak, BigDecimal.ROUND_HALF_UP);
            maxDrawdown = maxDrawdown.max(drawdown);
        }

        return maxDrawdown;
    }

}
