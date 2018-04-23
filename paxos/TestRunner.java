package paxos;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class TestRunner {
    public static void main(String... args) throws ClassNotFoundException {
      boolean all_successful = true;
      for (String arg : args)
      {
        String[] classAndMethod = arg.split("#");
        Request request = Request.method(Class.forName(classAndMethod[0]),
                classAndMethod[1]);

        Result result = new JUnitCore().run(request);
        all_successful = all_successful && result.wasSuccessful();
      }
      System.exit(all_successful ? 0 : 1);
    }
}
