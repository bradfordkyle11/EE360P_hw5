package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

  ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    int[] dones;

    class AgreementInstance
    {
      // Shared
      int reqID = 0;
      // Proposer
      Object v = null;
      // Acceptor
      int lastAcceptReqID = -1;
      Object lastAcceptV = null;
      // Learner
      boolean decided = false;

      AgreementInstance (Object v)
      {
        this.v = v;
      }
    }

    HashMap<Integer, AgreementInstance> instances;
    int latest_seq = -1;


    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

      this.me = me;
      this.peers = peers;
      this.ports = ports;
      this.mutex = new ReentrantLock();
      this.dead = new AtomicBoolean(false);
      this.unreliable = new AtomicBoolean(false);

      // Your initialization code here
      dones = new int[peers.length];
      for (int i=0; i<dones.length; i++)
        dones[i] = -1;
      instances = new HashMap<Integer, AgreementInstance> ();

      // register peers, do not modify this part
      try{
        System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
        registry = LocateRegistry.createRegistry(this.ports[this.me]);
        stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
        registry.rebind("Paxos", stub);
      } catch(Exception e){
        e.printStackTrace();
      }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
      Response callReply = null;

      PaxosRMI stub;
      try{
        Registry registry=LocateRegistry.getRegistry(this.ports[id]);
        stub=(PaxosRMI) registry.lookup("Paxos");
        if(rmi.equals("Prepare"))
          callReply = stub.Prepare(req);
        else if(rmi.equals("Accept"))
          callReply = stub.Accept(req);
        else if(rmi.equals("Decide"))
          callReply = stub.Decide(req);
        else
          System.out.println("Wrong parameters!");
      } catch(Exception e){
        return null;
      }
      return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
      instances.put (seq, new AgreementInstance (value));
      latest_seq = seq;
      new Thread (this).start ();
    }

    class Caller implements Runnable
    {
      String type;
      Request r;
      int serverID;
      Response retVal = null;

      Caller (String type, Request r, int serverID)
      {
        this.type = type;
        this.r = r;
        this.serverID = serverID;
      }

      public void run ()
      {
        retVal = Call (type, r, serverID);
      }
    }

    boolean getMajority (String type, Request request, Caller[] callers, Thread[] calls)
    {
      // Send each request in a new thread.
      for (int i=0; i<peers.length; i++)
      {
        callers[i] = new Caller (type, request, i);
        calls[i] = new Thread (callers[i]);
        calls[i].start ();
      }

      // Receive request responses, counting okays.
      int okays = 0;
      for (int i=0; i<peers.length; i++)
      {
        try
        {
          calls[i].join ();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        if (callers[i].retVal.accepted)
          okays++;
      }

      // (If majority found)
      return (okays > peers.length / 2);
    }

    @Override
    public void run(){
      int seq = latest_seq;
      AgreementInstance context = instances.get (seq);

      while (!context.decided)
      {
        // Get next logical reqID value.
        context.reqID = (context.reqID / peers.length + 1) * peers.length + me;

        // Prepare proposal.
        Request proposal = new Request (seq, context.reqID);

        // Track proposals sent in these arrays.
        Caller callers[] = new Caller[peers.length];
        Thread calls[] = new Thread[peers.length];

        // Continue next round if no majority acceptance.
        if (!getMajority ("Prepare", proposal, callers, calls))
          continue;

        // Match the value of any old accepted proposals.
        int maxAcceptReqID = -1;
        for (Caller caller : callers)
        {
          if (caller.retVal == null || !caller.retVal.accepted)
            continue;

          // Match just the most recent of previously accepted proposals.
          if (caller.retVal.lastAcceptReqID > maxAcceptReqID)
          {
            context.v = caller.retVal.lastAcceptV;
            maxAcceptReqID = caller.retVal.lastAcceptReqID;
          }

          // Update dones[] through piggy-backed data.
          dones[caller.retVal.me] = caller.retVal.done;
        }

        // Send Accept requests.
        Request accept = new Request (seq, context.reqID, context.v);
        if (!getMajority ("Accept", accept, callers, calls))
          continue;

        // Send decide messages.
        Request decide = new Request (seq, context.v, me, dones[me]);
        for (int i=0; i<peers.length; i++)
        {
          callers[i] = new Caller ("Decide", decide, i);
          calls[i] = new Thread (callers[i]);
          calls[i].start ();
        }
      }
    }

    // RMI handler
    public Response Prepare(Request req){
        // your code here

    }

    public Response Accept(Request req){
        // your code here

    }

    public Response Decide(Request req){
        // your code here

    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here

    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here

    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
      public State state;
      public Object v;

      public retStatus(State state, Object v){
        this.state = state;
        this.v = v;
      }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
      this.dead.getAndSet(true);
      if(this.registry != null){
        try {
          UnicastRemoteObject.unexportObject(this.registry, true);
        } catch(Exception e){
          System.out.println("None reference");
        }
      }
    }

    public boolean isDead(){
      return this.dead.get();
    }

    public void setUnreliable(){
      this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
      return this.unreliable.get();
    }


  }
