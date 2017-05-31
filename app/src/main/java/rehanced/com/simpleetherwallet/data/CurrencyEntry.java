package rehanced.com.simpleetherwallet.data;

public class CurrencyEntry {

    private String name;
    private double rate;
    private String shorty;

    public CurrencyEntry(String name, double rate, String shorty) {
        this.name = name;
        this.rate = rate;
        this.shorty = shorty;
    }

    public String getShorty() {
        return shorty;
    }

    public void setShorty(String shorty) {
        this.shorty = shorty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
