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


    // Your constructor and methods here


    //Need PREPARE requests and ACCEPT requests

    //PREPARE:
    //  int n, unique number that is higher than any seen before
    
    //ACCEPT:
    //  int n, unique number that is higher than any seen before
    //  command v, either the command received from prepare_ok, or a command you choose yourself if no command received 

    //uniqueness of n: use (max_num_seen / num_paxos + 1) * num_paxos + paxos_id 
}
