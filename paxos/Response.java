package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    public boolean accepted;
    public int reqRespondingTo;
    public int highestAccept;
    public int done;
    public int acceptorID;
    public String command;

    //all variables needed for PREPARE_OK response
    public Response(boolean accepted, int acceptorID, int reqRespondingTo, int highestAccept, String command, int done)
    {
        this.accepted = accepted;
        this.acceptorID = acceptorID;
        this.reqRespondingTo = reqRespondingTo;
        this.highestAccept = highestAccept;
        this.command = command;
        this.done = done;
    }

    public Response(boolean accepted, int acceptorID, int reqRespondingTo, int done)
    {
        this.accepted = accepted;
        this.acceptorID = acceptorID;
        this.reqRespondingTo = reqRespondingTo;
        this.done = done;
    }

    // Need PREPARE_OK, PREPARE_REJECT, ACCEPT_OK, ACCEPT_REJECT

    //PREPARE_OK/PREPARE_REJECT:
    //  bool accept, true for accept, false for reject
    //  int n, the proposal number from the PREPARE request
    //  int n_a, the highest ACCEPT proposal ok'd
    //  int v_a, the value corresponding to n_a
    //  int done, the highest Done() value from the acceptor
    
    //ACCEPT_OK/ACCEPT_REJECT:
    //  bool accept, true for accept, false for reject
    //  int n, the proposal number of the ACCEPT request
    //  int done, the highest Done() value from the acceptor
    

    // Your constructor and methods here

}
