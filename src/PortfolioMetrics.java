import java.math.BigDecimal;
import java.math.RoundingMode;

public class PortfolioMetrics {

    private final BigDecimal portfolioReturn;
    private final BigDecimal portfolioVolatility;
    private final BigDecimal sharpeRatio;
    private final BigDecimal maxDrawdown;
    private final BigDecimal annualizedReturn;

    public PortfolioMetrics(BigDecimal portfolioReturn, BigDecimal portfolioVolatility, BigDecimal sharpeRatio,
                            BigDecimal maxDrawdown, BigDecimal annualizedReturn) {
        this.portfolioReturn = portfolioReturn;
        this.portfolioVolatility = portfolioVolatility;
        this.sharpeRatio = sharpeRatio;
        this.maxDrawdown = maxDrawdown;
        this.annualizedReturn = annualizedReturn;
    }

    // Getters
    public BigDecimal getPortfolioReturn() {
        return portfolioReturn;
    }

    public BigDecimal getPortfolioVolatility() {
        return portfolioVolatility;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public BigDecimal getAnnualizedReturn() {
        return annualizedReturn;
    }

    @Override
    public String toString() {
        return "PortfolioMetrics{" +
                "portfolioReturn=" + portfolioReturn +
                ", portfolioVolatility=" + portfolioVolatility +
                ", sharpeRatio=" + sharpeRatio +
                ", maxDrawdown=" + maxDrawdown +
                ", annualizedReturn=" + annualizedReturn +
                '}';
    }
}
