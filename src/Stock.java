import java.math.BigDecimal;
import java.time.LocalDate;

public class Stock {
    private final LocalDate date;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final BigDecimal adjClose;
    private final long volume;
    private final String ticker;

    public Stock(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjClose, long volume, String ticker) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjClose = adjClose;
        this.volume = volume;
        this.ticker = ticker;
    }
    public Stock(LocalDate date,BigDecimal adjClose) {
        this.date = date;
        this.open = null;
        this.high = null;
        this.low = null;
        this.close = null;
        this.adjClose = adjClose;
        this.volume = 0;
        this.ticker = null;
    }
    @Override
    public String toString() {
        return "Stock{" +
                "date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", adjClose=" + adjClose +
                ", volume=" + volume +
                ", ticker='" + ticker + '\'' +
                '}';
    }

    // Getters
    public LocalDate getDate() { return date; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public BigDecimal getAdjClose() { return adjClose; }
    public long getVolume() { return volume; }
    public String getTicker() { return ticker; }
}
