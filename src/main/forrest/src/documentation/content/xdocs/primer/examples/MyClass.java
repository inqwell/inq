import com.inqwell.any.*;

public class MyClass
{
  static Map     args;
  static StringI argName = new ConstString("a");

  static LocateNode fromJava = new LocateNode("$catalog.demo.exprs.fromJava");
  static Call       c;

  static
  {
    args = AbstractComposite.simpleMap();
    args.add(argName, AnyNull.instance());

    c = new Call(fromJava, null, args);
  }

  static public void foo(Any a)
  {
    System.out.println("foo called: " + a);

    // Now go back to Inq script
    args.replaceItem(argName, a);
    Call.call(c, args);
  }
}

