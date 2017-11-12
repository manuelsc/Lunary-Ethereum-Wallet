package rehanced.com.simpleetherwallet.utils.qr;

import java.io.IOException;
import java.math.BigInteger;

public class AddressEncoder {

    private String address;
    private String gas;
    private String amount;
    private String data;
    private byte type;

    public AddressEncoder(String address, String amount) {
        this(address);
        this.amount = amount;
    }

    public AddressEncoder(String address) {
        this.address = address;
    }

    public static AddressEncoder decode(String s) throws IOException {
        if (s.startsWith("ethereum:") || s.startsWith("ETHEREUM:"))
            return decodeERC(s);
        else if (s.startsWith("iban:XE") || s.startsWith("IBAN:XE"))
            return decodeICAP(s);
        else
            return decodeLegacyLunary(s);
    }

    public static AddressEncoder decodeERC(String s) throws IOException {
        if (!s.startsWith("ethereum:") && !s.startsWith("ETHEREUM:"))
            throw new IOException("Invalid data format, see ERC-67 https://github.com/ethereum/EIPs/issues/67");
        AddressEncoder re = new AddressEncoder(s.substring(9, 51));
        if(s.length() == 51) return re;
        String[] parsed = s.substring(51).split("\\?");
        for (String entry : parsed) {
            String[] entry_s = entry.split("=");
            if (entry_s.length != 2) continue;
            if (entry_s[0].equalsIgnoreCase("value")) re.amount = entry_s[1];
            if (entry_s[0].equalsIgnoreCase("gas")) re.gas = entry_s[1];
            if (entry_s[0].equalsIgnoreCase("data")) re.data = entry_s[1];
        }
        return re;
    }

    public static String encodeERC(AddressEncoder a) {
        String re = "ethereum:" + a.address;
        if (a.amount != null) re += "?value=" + a.amount;
        if (a.gas != null) re += "?gas=" + a.gas;
        if (a.data != null) re += "?data=" + a.data;
        return re;
    }

    public static AddressEncoder decodeICAP(String s) throws IOException {
        if (!s.startsWith("iban:XE") && !s.startsWith("IBAN:XE"))
            throw new IOException("Invalid data format, see ICAP https://github.com/ethereum/wiki/wiki/ICAP:-Inter-exchange-Client-Address-Protocol");
        // TODO: verify checksum and length
        String temp = s.substring(9);
        int index = temp.indexOf("?") > 0 ? temp.indexOf("?") : temp.length();
        String address = new BigInteger(temp.substring(0, index), 36).toString(16);
        while (address.length() < 40)
            address = "0" + address;
        AddressEncoder re = new AddressEncoder("0x" + address);
        String[] parsed = s.split("\\?");
        for (String entry : parsed) {
            String[] entry_s = entry.split("=");
            if (entry_s.length != 2) continue;
            if (entry_s[0].equalsIgnoreCase("amount")) re.amount = entry_s[1];
            if (entry_s[0].equalsIgnoreCase("gas")) re.gas = entry_s[1];
            if (entry_s[0].equalsIgnoreCase("data")) re.data = entry_s[1];
        }
        return re;
    }

    public static AddressEncoder decodeLegacyLunary(String s) throws IOException {
        if (!s.startsWith("iban:") && !s.startsWith("IBAN:")) return new AddressEncoder(s);
        String temp = s.substring(5);
        String amount = null;
        if (temp.indexOf("?") > 0) {
            if (temp.indexOf("amount=") > 0 && temp.indexOf("amount=") < temp.length())
                amount = temp.substring(temp.indexOf("amount=") + 7);
            temp = temp.substring(0, temp.indexOf("?"));
        }
        AddressEncoder re = new AddressEncoder(temp);
        re.setAmount(amount);
        return re;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
