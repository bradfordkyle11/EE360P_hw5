package kvpaxos;
import paxos.Paxos;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import static org.junit.Assert.assertNotNull;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;

public class Server implements KVPaxosRMI {

  ReentrantLock mutex;
  Registry registry;
  Paxos px;
  int me;

  String[] servers;
  int[] ports;
  KVPaxosRMI stub;

    // Your definitions here
  ConcurrentHashMap <String, Integer> kvStore;

  public Server(String[] servers, int[] ports, int me){
    this.me = me;
    this.servers = servers;
    this.ports = ports;
    this.mutex = new ReentrantLock();
    this.px = new Paxos(me, servers, ports);
    this.kvStore = new ConcurrentHashMap <String, Integer> ();


    try{
      System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
      registry = LocateRegistry.getRegistry(this.ports[this.me]);
      stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
      registry.rebind("KVPaxos", stub);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public Op wait(int seq) {
    int to = 10;
    while (true) {
      Paxos.retStatus ret = this.px.Status (seq);
      if (ret.state == State.Decided) {
        return Op.class.cast (ret.v);
      }
      try{
        Thread.sleep (to);
      }catch (Exception e){
        e.printStackTrace ();
      }
      if (to < 1000){
        to = to * 2;
      }
    }
  }

  public int getFulfilledSeq (String clientID)
  {
    // Loop through seen seq's looking for if the request was already fulfilled
    for (int seq=px.Min (); seq<=px.Max (); seq++)
    {
      Paxos.retStatus status = px.Status (seq);
      State state = status.state;
      Op op = Op.class.cast (status.v);
      if (state == State.Decided && op.clientID.equals (clientID))
        return seq;
    }

    return -1;
  }

    // RMI handlers
  public Response Get(Request req) {
    // Return old result if request already fulfilled
    int seq = getFulfilledSeq (req.op.clientID);
    if (seq >= 0)
      return new Response (getSeqValue(seq));

    // Find new valid seq and start Paxos instance
    seq = px.Max () + 1;
    px.Start (seq, req.op);

    // Wait for decision.
    Op result = wait (seq);

    // while request failed (Paxos decided on a different proposal)
    while (!result.clientID.equals (req.op.clientID))
    {
      // Return old result if request already fulfilled
      seq = getFulfilledSeq (req.op.clientID);
      if (seq >= 0)
        return new Response (getSeqValue(seq));

      // Find new valid seq and start Paxos instance
      seq = px.Max () + 1;
      result = wait (seq);
    }

    return new Response (getSeqValue(seq));
  }



  private int getSeqValue(int seq)
  {
    Op op = Op.class.cast(px.Status(seq).v);
    String key = op.key;
    
    Integer result = null;
    
    for (int i = px.Min(); i < seq; i++)
    {
      op = wait(i);
      if (op.key.equals(key))
      {
        result = op.value;
      }
    }

    if (result == null)
    {
      return kvStore.get(key);
    }
    else
    {
      return result;
    }
  }

  public Response Put(Request req){
    // Noop if request already fulfilled
    int seq = getFulfilledSeq (req.op.clientID);
    if (seq >= 0)
      return new Response ();

    // Find new valid seq and start Paxos instance
    seq = px.Max () + 1;
    px.Start (seq, req.op);

    // Wait for decision.
    Op result = wait (seq);

    // while request failed (Paxos decided on a different proposal)
    while (!result.clientID.equals (req.op.clientID))
    {
      // Return old result if request already fulfilled
      seq = getFulfilledSeq (req.op.clientID);
      if (seq >= 0)
        return new Response ();

      // Find new valid seq and start Paxos instance
      seq = px.Max () + 1;
      result = wait (seq);
    }

    return new Response ();
  }


}
