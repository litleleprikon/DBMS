package DBMS.DB;

import DBMS.Argument;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by dmitriy on 11/17/2015.
 */
public class Metadata {
    Map<String, ArrayList<Argument>> tableStructure;
    Map<String, ArrayList<Integer>> tablePages;
    Map<String, ArrayList<Integer>> indexPages;
}
