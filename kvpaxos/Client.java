package kvpaxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client {
  String[] servers;
  int[] ports;
  String ID_base;
  int requestNum = 0;


  public Client(String[] servers, int[] ports){
    this.servers = servers;
    this.ports = ports;
    this.ID_base = Long.parseLong (System.nanoTime ()) + '_';
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
      KVPaxosRMI stub;
      try{
        Registry registry= LocateRegistry.getRegistry(this.ports[id]);
        stub=(KVPaxosRMI) registry.lookup("KVPaxos");
        if(rmi.equals("Get"))
          callReply = stub.Get(req);
        else if(rmi.equals("Put")){
          callReply = stub.Put(req);}
          else
            System.out.println("Wrong parameters!");
        } catch(Exception e){
          return null;
        }
        return callReply;
      }

      public Integer Get(String key){
        String ClientID = ID_base + Integer.parseInt (requestNum++);
        Op op = Op ("Get", ClientID, key, null);
        Request req = new Request (op);

        int serverID = 0;
        Response result = Call ("Get", req, serverID);
        while (result == null)
        {
          serverID = nextServerID (serverID);
          result = Call ("Get", req, serverID);
        }

        return result.value;
      }

      int nextServerID (int serverID)
      {
        return (serverID + 1) % servers.length;
      }

      public boolean Put(String key, Integer value){
        String ClientID = ID_base + Integer.parseInt (requestNum++);
        Op op = Op ("Put", ClientID, key, value);
        Request req = new Request (op);

        int serverID = 0;
        Response result = Call ("Put", req, serverID);
        while (result == null)
        {
          serverID = nextServerID (serverID);
          result = Call ("Put", req, serverID);
        }

        return true;
      }

    }
