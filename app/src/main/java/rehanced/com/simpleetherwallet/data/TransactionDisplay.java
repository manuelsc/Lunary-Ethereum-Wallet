package rehanced.com.simpleetherwallet.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionDisplay implements Comparable {

    public static final byte NORMAL = 0;
    public static final byte CONTRACT = 1;

    private String fromAddress;
    private String toAddress;
    private BigInteger amount;
    private int confirmationStatus;
    private long date;
    private String walletName;
    private byte type;
    private String txHash;
    private String nounce;
    private long block;
    private int gasUsed;
    private long gasprice;
    private boolean error;

    public TransactionDisplay(String fromAddress, String toAddress, BigInteger amount, int confirmationStatus, long date, String walletName, byte type, String txHash, String nounce, long block, int gasUsed, long gasprice, boolean error) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.confirmationStatus = confirmationStatus;
        this.date = date;
        this.walletName = walletName;
        this.type = type;
        this.txHash = txHash;
        this.nounce = nounce;
        this.block = block;
        this.gasUsed = gasUsed;
        this.gasprice = gasprice;
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public int getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(int gasUsed) {
        this.gasUsed = gasUsed;
    }

    public long getGasprice() {
        return gasprice;
    }

    public void setGasprice(long gasprice) {
        this.gasprice = gasprice;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getFromAddress() {
        return fromAddress.toLowerCase();
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress.toLowerCase();
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getAmountNative() {
        return amount;
    }

    public double getAmount() {
        return new BigDecimal(amount).divide(new BigDecimal(1000000000000000000d), 8, BigDecimal.ROUND_UP).doubleValue();
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public int getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(int confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getNounce() {
        return nounce;
    }

    public void setNounce(String nounce) {
        this.nounce = nounce;
    }

    @Override
    public String toString() {
        return "TransactionDisplay{" +
                "fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", amount=" + amount +
                ", confirmationStatus=" + confirmationStatus +
                ", date='" + date + '\'' +
                ", walletName='" + walletName + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if (this.getDate() < ((TransactionDisplay) o).getDate())
            return 1;
        if (this.getDate() == ((TransactionDisplay) o).getDate())
            return 0;
        return -1;
    }
}
