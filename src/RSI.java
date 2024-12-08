import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RSI {

    private final int period;

    public RSI(int period) {
        this.period = period;
    }

    public List<BigDecimal> calculate(List<BigDecimal> priceData) {
        List<BigDecimal> rsiValues = new ArrayList<>();
        if (priceData.size() < period) return rsiValues; // Not enough data to calculate RSI

        BigDecimal averageGain = BigDecimal.ZERO;
        BigDecimal averageLoss = BigDecimal.ZERO;

        // Calculate initial gains and losses
        for (int i = 1; i <= period; i++) {
            BigDecimal change = priceData.get(i).subtract(priceData.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                averageGain = averageGain.add(change);
            } else {
                averageLoss = averageLoss.add(change.abs());
            }
        }

        averageGain = averageGain.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);
        averageLoss = averageLoss.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);

        // Calculate the RSI values
        for (int i = period; i < priceData.size(); i++) {
            BigDecimal change = priceData.get(i).subtract(priceData.get(i - 1));
            BigDecimal gain = (change.compareTo(BigDecimal.ZERO) > 0) ? change : BigDecimal.ZERO;
            BigDecimal loss = (change.compareTo(BigDecimal.ZERO) < 0) ? change.abs() : BigDecimal.ZERO;

            averageGain = averageGain.multiply(BigDecimal.valueOf(period - 1)).add(gain)
                    .divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);
            averageLoss = averageLoss.multiply(BigDecimal.valueOf(period - 1)).add(loss)
                    .divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);

            if (averageLoss.compareTo(BigDecimal.ZERO) == 0) {
                rsiValues.add(BigDecimal.valueOf(100)); // If no losses, RSI is 100
            } else {
                BigDecimal rs = averageGain.divide(averageLoss, BigDecimal.ROUND_HALF_UP);
                BigDecimal rsi = BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), BigDecimal.ROUND_HALF_UP));
                rsiValues.add(rsi);
            }
        }

        return rsiValues;
    }
}
