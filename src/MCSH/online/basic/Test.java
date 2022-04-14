package MCSH.online.basic;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.Adistance_float;
import MCSH.util.MetaPath;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Test {

    public static void main(String[] args) throws IOException {
//        DataReader dataReader = new DataReader(Config.smallDBLPGraph, Config.smallDBLPVertex, Config.smallDBLPEdge);
		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge,null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
//        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);

        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
//        Map<Integer,double[]> attribute = dataReader.readattributed();
//        Map<Integer,float[]> integerMap = dataReader.readattributed_float();
//        int textnum = (int)integerMap.get(-1)[0];
//        int contnum = (int)integerMap.get(-1)[1];


//        int queryK = 45;
//        int queryM = 10;
        int[] vertex = {1, 0, 1}; //MPM
        int[] edge = {3, 0};
        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);


        //模拟参数输入 均值
//        int[] main = {1,1};
//        int[] text = new int[textnum];
//        int[] cont = new int[contnum];
//        for(int i = 0;i<textnum;i++){
//            text[i] = 1;
//        }
//        for(int i = 0;i<contnum;i++){
//            cont[i] = 1;
//        }


//        Gweight_float gweight_float = new Gweight_float(main,text,cont,2);
        Adistance_float adistance_float = new Adistance_float();

//        int count = 0;
//        long time1 = 0, time2 = 0;
//        for (int queryK = 35;queryK>=25;queryK--){
                int queryId = 140528;
                long t1 = System.nanoTime();
                Build b = new Build(graph,vertexType,edgeType,new HashMap<>(),adistance_float);
                Map<Integer, Set<Integer>> map1 = b.build1(queryMPath,queryId);
                long t2 = System.nanoTime();
                System.out.println((t2-t1)/1000000);

//                long t3 = System.nanoTime();
//                Build b1 = new Build(graph,vertexType,edgeType,new HashMap<>(),adistance_float);
//                Map<Integer, Set<Integer>> map3 = b1.build3(queryMPath,queryId);
//                long t4 = System.nanoTime();
//                System.out.println((t4-t3)/1000000);

                long t5 = System.nanoTime();
                Build b3 = new Build(graph,vertexType,edgeType,new HashMap<>(),adistance_float);
                Map<Integer, Set<Integer>> map2 = b3.bm2(queryMPath,queryId);
                long t6 = System.nanoTime();
                System.out.println((t6-t5)/1000000);

//
                int flag1=0;
                for (Map.Entry<Integer,Set<Integer>> entry:map1.entrySet()){
                    int i = entry.getKey();
                    Set<Integer> set1 = entry.getValue();
                    if(set1.size()>0){
                        Set<Integer> set2 = new HashSet<>(set1);
                        Set<Integer> set = map2.get(i);
                        set2.retainAll(set);
                        if(set2.size()!=set1.size()){
                            System.out.println(set1.size()+":"+set.size());
                            flag1=1;
                            break;
                        }
                    }
                }
                if (flag1==1){
                    System.out.println("map3 error!");
                }else {
                    System.out.println("map3 fine!");
                }

//                int queryId = i;
//                System.out.println("queryId=" + queryId + " queryK=" + queryK);
//                long t3 = System.nanoTime();
//                Base_logmap2 baseline1 = new Base_logmap2(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> result2 = baseline1.query(queryId,queryK,queryMPath);
//                long t4 = System.nanoTime();
//                System.out.println("base:"+result2.size());
////                System.out.println(dataReader.trans(result2));
//                System.out.println("base query time:"+(t4 - t3));
//                System.out.println(adistance_float.cal_subgraph_attr_dist(result2));
//                System.out.println();

//                long t5 = System.nanoTime();
//                Base_logmap2 ad2 = new Base_logmap2(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> result3 = ad2.queryM(queryId,queryK,queryMPath,queryM);
//                long t6 = System.nanoTime();
//                System.out.println("base copy :"+result3.size());
//                System.out.println("base copy query time:"+(t6 - t5));
//                System.out.println(adistance_float.cal_subgraph_attr_dist(result3));
//                System.out.println();
//
//                long t13 = System.nanoTime();
//                Base_logmap2 b1 = new Base_logmap2(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> result5 = b1.queryM_protect(queryId,queryK,queryMPath,queryM);
//                long t14 = System.nanoTime();
//                System.out.println("basemp :"+result5.size());
//                System.out.println("basemp query time:"+(t14 - t13));
//                System.out.println(adistance_float.cal_subgraph_attr_dist(result5));
//                System.out.println();
//
//                long t11 = System.nanoTime();
//                Base_logmap b2 = new Base_logmap(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> re = b2.query(queryId,queryK,queryMPath);
//                long t12 = System.nanoTime();
//                System.out.println("new try :"+re.size());
//                System.out.println("new try query time:"+(t12 - t11));
//                System.out.println(adistance_float.cal_subgraph_attr_dist(re));
//                System.out.println();
//                re.retainAll(result2);
//                System.out.println(re.size());
////
//                long t9 = System.nanoTime();
//                Base_logmap bl = new Base_logmap(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> result44 = bl.queryM(queryId,queryK,queryMPath,queryM);
//                long t10 = System.nanoTime();
//                System.out.println("new try m query time2:"+(t10 - t9));
//                System.out.println("new try m:"+result44.size());
//                System.out.println(adistance_float.cal_subgraph_attr_dist(result44));
//                System.out.println();
//////
//                long t8 = System.nanoTime();
//                Base_logmap bl1 = new Base_logmap(graph,vertexType,edgeType,integerMap,adistance_float);
//                Set<Integer> result4 = bl1.queryM_protect(queryId,queryK,queryMPath,queryM);
//                long t7 = System.nanoTime();
//                System.out.println("new try mp query time2:"+(t7 - t8));
//                System.out.println("new try new mp:"+result4.size());
//                System.out.println(adistance_float.cal_subgraph_attr_dist(result4));
//
//                result4.retainAll(result5);
//                System.out.println(result4.size());

//                System.out.println("base one");
//                result2.removeAll(re);
//                System.out.println(result2.size());
//
//                System.out.println("base m");
//                result3.removeAll(result44);
//                System.out.println(result3.size());
//
//                System.out.println("base mp");
//                result5.removeAll(result4);
//                System.out.println(result5.size());

//            }

//        }
//        System.out.println("Finished " + count + " queries.\ntime1=" + time1 + "\ntime2=" + time2);
    }

}
