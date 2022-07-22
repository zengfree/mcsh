package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class e2 {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

//        String queryfile = "C:\\Users\\DELL\\Desktop\\实验\\query\\sq1.txt";
        String queryfile = "C:\\zxj\\CSHDS\\query\\q1.txt";
//        int[] vertex = {0,1,0},edge = {0,0};
//        String queryfile = "C:\\Users\\DELL\\Desktop\\实验\\query\\dblp.txt";

        int[] vertex = {0,1,0},edge = {0,0};

        MetaPath queryMPath = new MetaPath(vertex, edge);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));

            int queryK = 5;
            int queryM = 2;
            //模拟参数输入 均值
            int textnum = (int)attribute.get(-1)[0];
            int contnum = (int)attribute.get(-1)[1];
            int[] main = {1,1};
            int[] text = new int[textnum];
            int[] cont = new int[contnum];
            for(int i = 0;i<textnum;i++){
                text[i] = 1;
            }
            for(int i = 0;i<contnum;i++){
                cont[i] = 1;
            }

            Gweight_float gweight = new Gweight_float(main, text, cont, 2);
            //        System.out.println(gweight.toString());
            Adistance_float adistance = new Adistance_float(attribute, gweight);
            adistance.setYu((float) 0.2);
            String line ;
            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }

            int num = 0;
            float avgpre = (float) 0;
            float avgrec = (float) 0;
            Map<Integer,Set<Integer>> exact = new HashMap<>();
            for (int queryid:querynodes){
                E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
                Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);
                if(res1!=null){
                    System.out.println(queryid);
                    exact.put(queryid,res1);
                }

            }

            File file = new File("C:\\zxj\\mcsh\\src\\corr.txt");
            stdin = new BufferedReader(new FileReader(file));
            Map<Integer,Integer> he2ho = new HashMap<>();
            Map<Integer,Integer> h02he = new HashMap<>();
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
//					ho2he.put(Integer.parseInt(s[1]),Integer.parseInt(s[0]));
                he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
            }
            stdin.close();
            String Res = "result.txt";
            file = new File("C:\\zxj\\论文\\实验代码\\Effective Community Search for Large Attributed Graphs\\VLDB 2015 Effective Community Search for Large Attributed Graphs [CODE]\\ACQ public codes\\"+Res);
            stdin = new BufferedReader(new FileReader(file));
            while((line = stdin.readLine()) != null) {
                num++;
                String s[] = line.split(" ");
                int queryid = he2ho.get(Integer.parseInt(s[0]));
                Set<Integer> res1 = new HashSet<>();
                for (int i = 1; i < s.length; i++) {
                    res1.add(he2ho.get(Integer.parseInt(s[i])));
                }
                Set<Integer> set = new HashSet<>(res1);
                set.retainAll(exact.get(queryid));
                avgpre += (float) set.size()/exact.get(queryid).size();
                avgrec += (float) set.size()/res1.size();
            }

            System.out.println("avgpre:"+avgpre/num);
            System.out.println("avgrec:"+avgrec/num);
            System.out.println(2*avgpre*avgrec/(avgpre+avgrec)/num);
        }catch (IOException ioException) {
            System.out.println(ioException);
        }

    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
