package kvpaxos;
import java.io.Serializable;

/**
 * You may find this class useful, free to use it.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String type;
    String ClientID;
    String key;
    Integer value;

    public Op(String type, String ClientID, String key, Integer value){
        this.type = type;
        this.ClientID = ClientID;
        this.key = key;
        this.value = value;
    }
}
