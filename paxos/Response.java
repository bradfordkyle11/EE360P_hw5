package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
  static final long serialVersionUID=2L;
  // piggy back
  public int me;
  public int done;

  // your data here
  public int seq;
  public boolean accepted;
  public int lastAcceptReqID;
  public Object lastAcceptV;
  public Object learnedV;

  //all variables needed for PREPARE_OK response
  public Response(int seq, boolean accepted, int lastAcceptReqID, Object lastAcceptV, int me, int done)
  {
    this.seq = seq;
    this.accepted = accepted;
    this.lastAcceptReqID = lastAcceptReqID;
    this.lastAcceptV = lastAcceptV;
    this.me = me;
    this.done = done;
  }

  public Response(int seq, boolean accepted, int me, int done)
  {
    this.seq = seq;
    this.accepted = accepted;
    this.me = me;
    this.done = done;
  }

  public Response ()
  {}

  // Need PREPARE_OK, PREPARE_REJECT, ACCEPT_OK, ACCEPT_REJECT

  //PREPARE_OK/PREPARE_REJECT:
  //  bool accept, true for accept, false for reject
  //  int n, the proposal number from the PREPARE request
  //  int n_a, the last ACCEPT proposal ok'd
  //  int v_a, the value corresponding to n_a
  //  int done, the last Done() value from the acceptor

  //ACCEPT_OK/ACCEPT_REJECT:
  //  bool accept, true for accept, false for reject
  //  int n, the proposal number of the ACCEPT request
  //  int done, the last Done() value from the acceptor


  // Your constructor and methods here

}
