package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
  static final long serialVersionUID=1L;
    // Your data here
    public int seq;
    public int reqID; //must be unique and higher than any before
    public Object value;
    public int me;
    public int done;

    // Your constructor and methods here

    //for prepare requests, you only need the reqID
    public Request(int seq, int reqID)
    {
      this.seq = seq;
      this.reqID = reqID;
    }

    //for accept requests, you need the reqID and the value you want accepted
    public Request(int seq, int reqID, Object value)
    {
      this.seq = seq;
      this.reqID = reqID;
      this.value = value;
    }

    // Decide request
    public Request(int seq, Object value, int me, int done)
    {
      this.seq = seq;
      this.value = value;
      this.me = me;
      this.done = done;
    }

    //Need PREPARE requests and ACCEPT requests

    //PREPARE:
    //  int n, unique number that is higher than any seen before

    //ACCEPT:
    //  int n, unique number that is higher than any seen before
    //  value v, either the value received from prepare_ok, or a value you choose yourself if no value received

    //uniqueness of n: use (max_num_seen / num_paxos + 1) * num_paxos + paxos_id
  }
