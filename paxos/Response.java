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


    // Your constructor and methods here

    // Need PREPARE_OK, PREPARE_REJECT, ACCEPT_OK, ACCEPT_REJECT

    //PREPARE_OK:
    //  int n, the proposal number from the PREPARE request
    //  int n_a, the highest proposal accepted
    //  int v_a, the value corresponding to n_a

    //PREPARE_REJECT:
    // ?

    //ACCEPT_OK:
    //  int n, the proposal number of the ACCEPT request

    //ACCEPT_REJECT:
    // ?
}
