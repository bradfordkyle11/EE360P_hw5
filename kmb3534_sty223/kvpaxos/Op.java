package kvpaxos;
import java.io.Serializable;

/**
 * You may find this class useful, free to use it.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String type;
    String clientID;
    String key;
    Integer value;

    public Op(String type, String clientID, String key, Integer value){
        this.type = type;
        this.clientID = clientID;
        this.key = key;
        this.value = value;
    }

    public String toString ()
    {
      if (type.equals ("Put"))
        return "(" + clientID + ", " + Integer.toString (value) + ")";
      else
        return "(" + clientID + ",)";
    }
}
