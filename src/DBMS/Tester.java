package DBMS;

import DBMS.DB.Database;
import DBMS.DB.InnerStructure.Argument;
import DBMS.DB.InnerStructure.Keys.PrimaryKey;
import DBMS.DB.InnerStructure.Types.Int;
import DBMS.DB.InnerStructure.Types.Type;
import DBMS.DB.InnerStructure.Types.VarChar;
import DBMS.Parser.Operator;
import DBMS.Parser.SqlParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Tester {
    public static void main(String[] args) throws IOException {
        Database database = Database.load("dmd_project");


//        for (int i = 31; i < 10000; i++) {
//            database.insert("publication",new Type[] {new Int(i), new VarChar("Photographs"), new Int(2015), new Int(46), new VarChar("http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=7123330"), new Int(34), new VarChar("This chapter comprises photographs showing the layout of both the whole machine and individual panels. These photographs are expressed far more clearly than verbal description. It includes old Robinson, specimen of old Robinson printing, super-Robinson, Colossus, insert machine, Miles, Garbo, decoding machines, Tunny machine, Dragon 1 and 2, Proteus, and Aquarius."), new Int(7123330), new VarChar("10.1002/9781119061601.ch34"), new Int(0), new VarChar("http://ieeexplore.ieee.org/xpl/articleDetails.jsp?tp=&arnumber=7123330&contentType=Books+%26+eBooks"), new Int(7123330), new Int(0)});
//        }
//        database.insert("publisher",new Type[]{new Int(46),new VarChar("Wiley-IEEE Press")});

//        database.insert("publication_type",new Type[]{new Int(34),new VarChar("Books&eBooks")});
//        operators=parser.parse("INSERT bla (VALUES (31, 'Photographs', 2015, 46, 'http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=7123330', 34, 'This chapter comprises photographs showing the layout of both the whole machine and individual panels. These photographs are expressed far more clearly than verbal description. It includes old Robinson, specimen of old Robinson printing, super-Robinson, Colossus, insert machine, Miles, Garbo, decoding machines, Tunny machine, Dragon 1 and 2, Proteus, and Aquarius.', 7123330, '10.1002/9781119061601.ch34', 0, 'http://ieeexplore.ieee.org/xpl/articleDetails.jsp?tp=&arnumber=7123330&contentType=Books+%26+eBooks', 7123330, 0))");

//        for (Operator operator : operators) {
//            System.out.println(operator.getType());
//            System.out.println("values:");
//            if (operator.getValues()!=null) {
//                for (Value value : operator.getValues()) {
//                    System.out.println(Arrays.toString(value.getValues()));
//                }
//            }
//            System.out.println("inner:");
//            if (operator.getInnerQuery()!=null) {
//                for (Operator inner : operator.getInnerQuery()) {
//                    System.out.println(inner.getType());
//                    for (Value value : inner.getValues()) {
//                        System.out.println(Arrays.toString(value.getValues()));
//                    }
//                }
//            }
//            System.out.println("arguments:");
//            if (operator.getArguments()!=null) {
//                for (QueryArgument argument : operator.getArguments()) {
//                    if (argument.getAlias()!=null) System.out.println(argument.getData()+" AS "+argument.getAlias());
//                    else if (argument.getCondition()!=null) System.out.println(argument.getData()+" "+argument.getCondition()+" "+argument.getCompare().getData());
//                    else System.out.println(argument.getData());
//                }
//            }
//            System.out.println("functions:");
//            if (operator.getFunc()!=null) {
//                System.out.println(operator.getFunc().getName());
//                System.out.println("Begin func");
//                operator.getFunc().getArguments().forEach(System.out::println);
//                System.out.println("End func");
//            }
            System.out.println("shalala");
//
//        }
    }
}
