package rehanced.com.simpleetherwallet.data;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenDisplay implements Comparable {

    private String name;
    private String shorty;
    private BigDecimal balance;
    private int digits;
    private double usdprice;
    private String contractAddr;
    private String totalSupply;
    private long holderCount;
    private long createdAt;

    public TokenDisplay(String name, String shorty, BigDecimal balance, int digits, double usdprice, String contractAddr, String totalSupply, long holderCount, long createdAt) {
        this.name = name;
        this.shorty = shorty;
        this.balance = balance;
        this.digits = digits;
        this.usdprice = usdprice;
        this.contractAddr = contractAddr;
        this.totalSupply = totalSupply;
        this.holderCount = holderCount;
        this.createdAt = createdAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShorty() {
        return shorty;
    }

    public void setShorty(String shorty) {
        this.shorty = shorty;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Uses digits and balance to create a double value
     *
     * @return Token balance in double
     */
    public double getBalanceDouble() {
        return balance.divide((new BigDecimal("10").pow(digits))).doubleValue();
    }

    /**
     * Uses digits and total supply to create a long value
     *
     * @return Token supply in long
     */
    public long getTotalSupplyLong() {
        return new BigInteger(totalSupply).divide((new BigInteger("10").pow(digits))).longValue();
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public double getUsdprice() {
        return usdprice;
    }

    public void setUsdprice(double usdprice) {
        this.usdprice = usdprice;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr = contractAddr;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

    public long getHolderCount() {
        return holderCount;
    }

    public void setHolderCount(long holderCount) {
        this.holderCount = holderCount;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return ((TokenDisplay) o).getShorty().compareTo(shorty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenDisplay that = (TokenDisplay) o;

        if (digits != that.digits) return false;
        if (!name.equals(that.name)) return false;
        return shorty.equals(that.shorty);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + shorty.hashCode();
        result = 31 * result + digits;
        return result;
    }
}
