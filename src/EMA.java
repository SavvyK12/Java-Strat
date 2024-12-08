import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EMA {

    private final int period;

    public EMA(int period) {
        this.period = period;
    }

    public List<BigDecimal> calculate(List<BigDecimal> priceData) {
        List<BigDecimal> emaValues = new ArrayList<>();
        if (priceData.size() < period) return emaValues; // Not enough data to calculate EMA

        // Calculate the exponential weighting multiplier (alpha)
        BigDecimal alpha = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(period + 1), BigDecimal.ROUND_HALF_UP);

        // Calculate the initial EMA (using SMA of the first `period` prices)
        BigDecimal initialEma = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            initialEma = initialEma.add(priceData.get(i));
        }
        initialEma = initialEma.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);
        emaValues.add(initialEma);

        // Calculate subsequent EMA values using exponential weighting
        BigDecimal prevEma = initialEma;
        for (int i = period; i < priceData.size(); i++) {
            BigDecimal currentPrice = priceData.get(i);
            BigDecimal weightedPrice = currentPrice.multiply(alpha);
            BigDecimal weightedPrevEma = prevEma.multiply(BigDecimal.ONE.subtract(alpha));

            // EMA is a weighted average of the current price and the previous EMA
            BigDecimal ema = weightedPrice.add(weightedPrevEma);
            emaValues.add(ema);
            prevEma = ema;
        }

        return emaValues;
    }
}
