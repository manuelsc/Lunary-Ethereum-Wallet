package rehanced.com.simpleetherwallet.data;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;

import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;

public class WalletDisplay implements Comparable {

    public static final byte NORMAL = 0;
    public static final byte WATCH_ONLY = 1;
    public static final byte CONTACT = 2;

    private String name;
    private String publicKey;
    private BigInteger balance;
    private byte type;

    public WalletDisplay(String name, String publicKey, BigInteger balance, byte type) {
        this.name = name;
        this.publicKey = publicKey;
        this.balance = balance;
        this.type = type;
    }

    public WalletDisplay(String name, String publicKey) {
        this.name = name;
        this.publicKey = publicKey;
        this.balance = null;
        this.type = CONTACT;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public BigInteger getBalanceNative() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return new BigDecimal(balance).divide(ExchangeCalculator.ONE_ETHER, 8, BigDecimal.ROUND_UP).doubleValue();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey.toLowerCase();
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return name.compareTo(((WalletDisplay) o).getName());
    }
}
