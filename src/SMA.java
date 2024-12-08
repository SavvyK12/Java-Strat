import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class SMA {

    private final int period;

    public SMA(int period) {
        this.period = period;
    }

    public List<BigDecimal> calculate(List<BigDecimal> priceData) {
        List<BigDecimal> smaValues = new ArrayList<>();
        if (priceData.size() < period) return smaValues; // Not enough data to calculate SMA

        for (int i = period - 1; i < priceData.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i - (period - 1); j <= i; j++) {
                sum = sum.add(priceData.get(j));
            }
            smaValues.add(sum.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP));

        }

        return smaValues;
    }
}

