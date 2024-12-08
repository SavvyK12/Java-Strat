import java.util.List;

public class backtest {
    private final List<Double> dailyReturns; // List of daily returns as percentages (e.g., 0.01 for 1%)
    private final double riskFreeRate; // Daily risk-free rate (e.g., 0.0001 for 0.01% daily)

    public backtest(List<Double> dailyReturns, double riskFreeRate) {
        this.dailyReturns = dailyReturns;
        this.riskFreeRate = riskFreeRate;
    }

    // Sharpe Ratio (with compounding)
    public double calculateSharpeRatio() {
        double compoundedReturn = calculateCumulativeReturn();
        double annualizedReturn = Math.pow(compoundedReturn + 1, 252.0 / dailyReturns.size()) - 1;
        double annualizedRiskFreeRate = Math.pow(riskFreeRate + 1, 252.0) - 1;

        double dailyVariance = dailyReturns.stream()
                .mapToDouble(r -> Math.pow(Math.log(1 + r) - Math.log(1 + compoundedReturn / dailyReturns.size()), 2))
                .sum() / dailyReturns.size();

        double annualizedVolatility = Math.sqrt(dailyVariance) * Math.sqrt(252);

        return (annualizedReturn - annualizedRiskFreeRate) / annualizedVolatility;
    }

    // Maximum Drawdown (using compounding)
    public double calculateMaxDrawdown(List<Double> portfolioValues) {
        double maxDrawdown = 0.0;
        double peak = portfolioValues.get(0);

        for (double value : portfolioValues) {
            if (value > peak) {
                peak = value;
            }
            double drawdown = (peak - value) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        return maxDrawdown;
    }

    // Win Rate (unchanged, based on compounding is not applicable here)
    public double calculateWinRate() {
        long winningDays = dailyReturns.stream().filter(r -> r > 0).count();
        return (double) winningDays / dailyReturns.size();
    }

    // Sortino Ratio (with compounding)
    public double calculateSortinoRatio() {
        double compoundedReturn = calculateCumulativeReturn();
        double annualizedReturn = Math.pow(compoundedReturn + 1, 252.0 / dailyReturns.size()) - 1;
        double annualizedRiskFreeRate = Math.pow(riskFreeRate + 1, 252.0) - 1;

        double downsideDeviation = Math.sqrt(dailyReturns.stream()
                .filter(r -> r < 0)
                .mapToDouble(r -> Math.pow(Math.log(1 + r), 2))
                .sum() / dailyReturns.size()) * Math.sqrt(252);

        return (annualizedReturn - annualizedRiskFreeRate) / downsideDeviation;
    }

    // Annualized Returns (with compounding)
    public double calculateAnnualizedReturns() {
        double compoundedReturn = calculateCumulativeReturn();
        return Math.pow(compoundedReturn + 1, 252.0 / dailyReturns.size()) - 1; // 252 trading days per year
    }

    // Helper: Calculate Cumulative Return with Compounding
    private double calculateCumulativeReturn() {
        return dailyReturns.stream().reduce(1.0, (prod, r) -> prod * (1 + r)) - 1;
    }

    // Example usage
    public static void main(String[] args) {
        // Example daily returns (percentages in decimal form)
        List<Double> dailyReturns = List.of(0.002, -0.001, 0.003, -0.002, 0.001, 0.004, -0.001);
        List<Double> portfolioValues = List.of(100.0, 101.0, 102.5, 101.0, 102.0, 104.0, 103.5);

        backtest backtest = new backtest(dailyReturns, 0.0001); // Example risk-free rate

        System.out.println("Sharpe Ratio: " + backtest.calculateSharpeRatio());
        System.out.println("Maximum Drawdown: " + backtest.calculateMaxDrawdown(portfolioValues));
        System.out.println("Win Rate: " + backtest.calculateWinRate());
        System.out.println("Sortino Ratio: " + backtest.calculateSortinoRatio());
        System.out.println("Annualized Returns: " + backtest.calculateAnnualizedReturns());
    }
}
