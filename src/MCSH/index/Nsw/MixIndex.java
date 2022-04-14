package MCSH.index.Nsw;

import MCSH.util.Adistance_float;
import MCSH.util.BatchSearch;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MixIndex {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;
    private Adistance_float adistance = null;
    private Map<Integer,Integer> correspond = null;

    private MetaPath queryMPath = null;//the query meta-path

    public MixIndex(int[][] graph, int[] vertexType, int[] edgeType,Map<Integer,float[]> attribute, Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attribute;
        this.adistance = adistance;
    }

    public void build(MetaPath queryMPath,String indexfile,String datafile,int M, int ef) throws IOException {
        this.queryMPath = queryMPath;

        //step 1: build the connected homogeneous graph
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        int queryK = 1;
        Set<Integer> kcc = new HashSet<>(pnbMap.keySet());
        FileWriter fw = new FileWriter(datafile,false);
        //step 2: compute the connected k-core
        while (!pnbMap.isEmpty()){
            findKCore(pnbMap,queryK);
            kcc.removeAll(pnbMap.keySet());
            int pcoreness = queryK-1;
            for (int i:kcc) {
//                fw.write(i);
                fw.write(trans(attribute.get(i),i)+"\n");
                fw.write(pcoreness+" "+"\n");
            }
            System.out.println(queryK+"finished");
            queryK++;
            kcc.clear();
            kcc.addAll(pnbMap.keySet());
        }
        fw.close();
        Nsw index = new Nsw();
        Gweight_float gweight = adistance.getPreference_weights();
        index.build(datafile,indexfile,gweight.getbuildweight(), gweight.getTextnum(), gweight.getContnum(), M,ef);
    }

    public Set<Integer> search(int queryid,int queryK,int queryN,String datafile,String indexfile){
        Nsw index = new Nsw();
        readCorrespond(datafile);
        int ef = queryN*5;
        Gweight_float gweight = adistance.getPreference_weights();
        int[] m =  index.search(attribute.get(queryid),queryK,queryN,ef,indexfile, gweight.getbuildweight(), gweight.getTextnum(), gweight.getContnum());
        Set<Integer> approximate = new HashSet<>();
        for (int j : m) {
            approximate.add(this.correspond.get(j));
        }
        return approximate;
    }

    public Set<Integer> findK(MetaPath queryMPath,int queryK){
        this.queryMPath = queryMPath;
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        findKCore(pnbMap,queryK);
        return pnbMap.keySet();
    }

    private void readCorrespond(String datafile){
        this.correspond = new HashMap<>();
        try{
            BufferedReader stdin = new BufferedReader(new FileReader(datafile));
            String line;

            int attnum = 0;
            while((line = stdin.readLine()) != null){
                int id = Integer.parseInt(line.substring(0,line.indexOf(" ")));
                this.correspond.put(attnum++,id);
//                attnum++;
                stdin.readLine();
            }
            stdin.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String trans(float[] V,int id){
        StringBuilder str5 = new StringBuilder();
        str5.append(id);
        for (float i : V) {
            str5.append(" ").append(i);
        }
        return str5.toString();
    }

    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: find all the vertices
        //遍历所有节点，找到与路径出发节点相同类型的节点
        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
                pnbMap.put(curId, pnbSet);
            }
        }

        return pnbMap;
    }

    private void findKCore(Map<Integer, Set<Integer>> pnbMap,int queryK) {
        Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<Integer>();
        for(Map.Entry<Integer, Set<Integer>> entry : pnbMap.entrySet()) {
            int curId = entry.getKey();
            Set<Integer> pnbSet = entry.getValue();
            if(pnbSet.size() < queryK) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        //step 2: delete vertices whose degrees are less than k
        while(queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbMap.get(curId);//找到curID对应的邻居
            for(int pnb:pnbSet) {//update curId's pnb
                if(!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbMap.get(pnb);
                    tmpSet.remove(curId);
                    if(tmpSet.size() < queryK) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }
//            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
            pnbMap.remove(curId);
        }

    }


//    public static void main(String[] args) throws IOException {
////        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge,null,Config.dblpattributed);
//        int[][] graph = dataReader.readGraph();
//        int[] vertexType = dataReader.readVertexType();
//        int[] edgeType = dataReader.readEdgeType();
//        Map<Integer,float[]> attribute = dataReader.readattributed_float();
//
//        int[] vertex = {1, 0, 1};
//        int[] edge = {3, 0};
////        int[] vertex = {0, 1, 0};
////        int[] edge = {0, 0};
////        int queryid = 511;
////        int queryk = Integer.parseInt(args[0]);
//        int queryk = 25;
//        MetaPath Path = new MetaPath(vertex, edge);
////        String datafile1 = "/home/hadoop/newindex/smalldata.txt";
////        String idnexfile1 = "/home/hadoop/newindex/smallindex.n2";
//        String datafile1 = "/home/hadoop/newindex/dblpdata.txt";
//        String idnexfile1 = "/home/hadoop/newindex/dblpindex.n2";
////        String datafile1 = "/home/star/zxj/code2/dblpdata.txt";
////        String idnexfile1 = "/home/star/zxj/code2/dblpindex.n2";
//        int textnum = (int)attribute.get(-1)[0];
//        int contnum = (int)attribute.get(-1)[1];
//        System.out.println(textnum);
//        System.out.println(contnum);
//        int[] main = {1,1};
//        int[] text = new int[textnum];
//        int[] cont = {1,1};
//        for(int i=0;i<textnum;i++){
//            text[i] = 1;
//        }
//        Gweight_float gweight = new Gweight_float(main,text,cont,2);
//        Adistance_float adistance= new Adistance_float(attribute,gweight);
//        MixIndex mix = new MixIndex(graph,vertexType,edgeType,attribute,adistance);
////        mix.build(Path,idnexfile1,datafile1,50,500);
//        float sumpre = 0,sumrec=0;
//        long sumt1 = 0,sumt2 = 0;
//        int flag =0;
//        while(flag<100){
////            Object[] list = attribute.keySet().toArray();
//            Object[] list = mix.findK(Path,queryk).toArray();
//            int queryid = (int)list[(int)(Math.random()*list.length)];
//            System.out.println("queryid"+queryid);
//            if(queryid==-1) continue;
//            int queryN = 300;
//            long t1 = System.nanoTime();
//            MixIndex mixIndex = new MixIndex(graph,vertexType,edgeType,attribute,adistance);
//            long t13 = System.nanoTime();
////            mixIndex.build(Path,idnexfile1,datafile1,50,600);
//            Set<Integer> set = mixIndex.search(queryid,queryk,queryN,datafile1,idnexfile1);
//            long t2 = System.nanoTime();
//            System.out.println("nsw:"+(t2-t1));
//            System.out.println("nsw s:"+(t2-t13));
//            sumt1 +=t2 -t1;
//
//            long t3 = System.nanoTime();
//            Map<Integer,Float> distancemap = new HashMap<>();
//            Set<Integer> candiset = mixIndex.findK(Path,queryk);
//            for (int nodeid:candiset){
//                distancemap.put(nodeid, adistance.cal_distance(queryid, nodeid));
//            }
//
//            List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(distancemap.entrySet());
//            entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
//                @Override
//                public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                    return o1.getValue().compareTo(o2.getValue());//升序
////                return o2.getValue().compareTo(o1.getValue());//降序排列
//                }
//            });
//
//            Set<Integer> keepSet2 = new HashSet<>();
//            for (int i = 0; i < queryN; i++) {
//                int id = entryList.get(i).getKey();
//                keepSet2.add(id);
//                if(i== entryList.size()-1){
//                    break;
//                }
//            }
//            long t4 = System.nanoTime();
//            sumt2 += t4-t3;
//            System.out.println("暴力:"+(t4-t3));
//
//
//            System.out.println("set:"+set.size());
//            System.out.println(dataReader.trans(set));
//            System.out.println("truth:"+keepSet2.size());
//            System.out.println(dataReader.trans(keepSet2));
//            Set<Integer> keepset3 = new HashSet<>(set);
//            keepset3.retainAll(keepSet2);
//            System.out.println("retain size:"+keepset3.size());
//
//            float pre = (float) keepset3.size()/set.size();
//            float rec = (float) keepset3.size()/queryN;
//            sumpre +=pre;
//            sumrec +=rec;
//            System.out.println(rec);
//            System.out.println(pre);
//            flag++;
//        }
//        System.out.println(sumpre/100);
//        System.out.println(sumrec/100);
//        System.out.println("sumt1"+sumt1/100);
//        System.out.println("sumt2"+sumt2/100);
//    }

}
