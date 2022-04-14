package MCSH.index.kcore2;

import MCSH.Config;
import MCSH.util.BatchSearch;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;

public class KHindex_saveall {
    private int graph[][] = null;
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;
    private MetaPath buildmetaPath  =null;

    private IndexNode kcoreTreerootNode;
    private Map<Integer, List<Integer>> headmap;
//    private int maxlength;
//    private int hnsw_minnum = 5000;

    public KHindex_saveall(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer,float[]> attribute){
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attribute;
    }

    public void build(MetaPath metaPath, Gweight_float gweight,int M,int ef,String indexpath){
        this.buildmetaPath = metaPath;
        String fileMeta = Config.IndexRoot+"Index_saveall/"+indexpath +"/"+buildmetaPath.toString();
        newFolder(fileMeta);
        String rootnodepath = fileMeta+"/index";
        this.kcoreTreerootNode = new IndexNode(rootnodepath,gweight,M,ef);
        this.headmap = new HashMap<>();
        Map<Integer,Set<Integer>> pnbMap = buildGraph();
        for (int curId:pnbMap.keySet()) {
            headmap.put(curId,new ArrayList<>());
        }

        kcoreTreerootNode.build(pnbMap,this.attribute,1,pnbMap.keySet(),this.headmap);

        //保存索引信息
        saveIndex(fileMeta+"/KHindex");
        System.out.println("all build succeed!");
    }

    public Set<Integer> search(int queryId, int queryK ,Gweight_float gweight,float precent){

        IndexNode treeNode = this.kcoreTreerootNode;
        List<Integer> quicklist = this.headmap.get(queryId);
        if(quicklist.size()<queryK) return null;
        for (int i = 0; i < queryK; i++) {
            treeNode = treeNode.getChildren(quicklist.get(i));
        }

        Set<Integer> result = treeNode.search(this.attribute.get(queryId),gweight,precent);

        return result;
    }

    public Set<Integer> search2(int queryId, int queryK ,Gweight_float gweight,float precent){

        IndexNode treeNode = this.kcoreTreerootNode;
        List<Integer> quicklist = this.headmap.get(queryId);
        if(quicklist.size()<queryK) return null;
        for (int i = 0; i < queryK; i++) {
            treeNode = treeNode.getChildren(quicklist.get(i));
        }

        Set<Integer> result = treeNode.search2(this.attribute.get(queryId),gweight,precent,attribute);

        return result;
    }

    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: find all the vertices
        //遍历所有节点，找到与路径出发节点相同类型的节点
        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == this.buildmetaPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, this.buildmetaPath);
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == this.buildmetaPath.vertex[0]) {
                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
                pnbMap.put(curId, pnbSet);
            }
        }

        return pnbMap;
    }

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap) {
        Map<Integer, Set<Integer>> newMap = new WeakHashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(), newset);
        }
        return newMap;
    }

    private void newFolder(String folderPath){
        File filePath = new File(folderPath);
        if(filePath.isDirectory()){
            System.out.println("the directory isexists!");
        }else {
            filePath.mkdir();
            System.out.println("mkdir succeed!");
        }
    }

    private void saveIndex(String indexPath){
        try{
            File file = new File(indexPath);
            if(file.exists()){
                file.createNewFile();
            }
            System.out.println("save index");
            this.kcoreTreerootNode.savenode(file);

            File file1 = new File(indexPath+"map");
            FileWriter fw = new FileWriter(file1,false);
            for (int curId:this.headmap.keySet()) {
                fw.write(curId+trans(this.headmap.get(curId))+"\r\n");
            }
            fw.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

    private String trans(List<Integer> list){
        StringBuilder str = new StringBuilder();
        if(list!=null){
            for (int i : list) {
                str.append(" "+i);
            }
        }else {
            return " ";
        }
        return str.toString();
    }

    public void loadIndex(MetaPath metaPath,String dataset){
        try{
            String fileMeta = Config.IndexRoot+"Index_saveall/"+ dataset+"/"+metaPath.toString();
            String indexpath = fileMeta +"/KHindex";

            //build the treenode
            Map<String, Integer> integerMap = new HashMap<>();
            BufferedReader stdin = new BufferedReader(new FileReader(indexpath));
            String line = null;
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
                String indexfile = s[0];
                int childsize = Integer.parseInt(s[1]);
                integerMap.put(indexfile,childsize);
            }
            stdin.close();

            String rootnodepath = fileMeta+"/index";
            System.out.println("load index");
            this.kcoreTreerootNode = new IndexNode(rootnodepath);
            this.kcoreTreerootNode.loadNode(integerMap);

            //read the headmap
            this.headmap = new HashMap<>();
            String headmapFile = indexpath+"map";
            stdin = new BufferedReader(new FileReader(headmapFile));
            while((line = stdin.readLine()) != null){
                String[] s = line.split(" ");
                int id = Integer.parseInt(s[0]);
                List<Integer> list = new ArrayList<>();
                for(int z=1;z< s.length;z++){
                    list.add(Integer.parseInt(s[z]));
                }
                this.headmap.put(id,list);
            }
            stdin.close();


        }catch (IOException e){
            System.out.println(e);
        }
    }

//    public static void main(String[] args) {
////        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        int graph[][] = dataReader.readGraph();
//        int vertexType[] = dataReader.readVertexType();
//        int edgeType[] = dataReader.readEdgeType();
//        Map<Integer,float[]> attribute = dataReader.readattributed_float();
//
//        KHindex_saveall aKindex = new KHindex_saveall(graph,vertexType,edgeType,attribute);
////        int vertex[] = {0, 1, 0}, edge[] = {0, 0}; //APA
//        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
//        MetaPath queryMPath = new MetaPath(vertex, edge);
//
//        int textnum = (int)attribute.get(-1)[0];
//        int contnum = (int)attribute.get(-1)[1];
//        int[] main = {1,1};
//        int[] text = new int[textnum];
//        int[] cont = new int[contnum];
//        for(int i = 0;i<textnum;i++){
//            text[i] = 1;
//        }
//        for(int i = 0;i<contnum;i++){
//            cont[i] = 1;
//        }
//        Gweight_float gweight = new Gweight_float(main,text,cont,2);
////        Adistance adistance = new Adistance();
//        long t1 = System.nanoTime();
//        aKindex.build(queryMPath,gweight,50,600);
//        long t2 = System.nanoTime();
//        System.out.println("build time:"+(t2-t1)/1000000);
//
//        int queryid = 140528;
//        long t10 = System.nanoTime();
//        aKindex.loadIndex(queryMPath);
//        Set<Integer> result = aKindex.search(queryid,0,gweight, (float) 0.8);
//        long t20 = System.nanoTime();
//        System.out.println("hnsw time:"+(t20-t10)/1000000);
//        System.out.println("Search:");
//        System.out.println(result.size());
//        System.out.println(dataReader.trans(result));
//        System.out.println();
//
//        long t3= System.nanoTime();
//        Adistance_float adistance_float = new Adistance_float(attribute,gweight);
//        Map<Integer,Float> distancemap = new HashMap<>();
//        for(int nodeid:attribute.keySet()){
//            if(nodeid!=-1) {
////                System.out.println(nodeid);
//                distancemap.put(nodeid, adistance_float.cal_distance(queryid, nodeid));
//            }
//        }
//
//        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(distancemap.entrySet());
//        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
//            @Override
//            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
////                return o2.getValue().compareTo(o1.getValue());//降序排列
//            }
//        });
//
//        Set<Integer> keepSet2 = new HashSet<>();
//        int k = (int) (distancemap.size() * 0.8);
//        for (int i = 0; i < k; i++) {
//            int id = entryList.get(i).getKey();
//            keepSet2.add(id);
//            if(i== entryList.size()-1){
//                break;
//            }
//        }
//        long t4 = System.nanoTime();
//        System.out.println("time:"+(t4-t3)/1000000);
//        System.out.println("truth");
//        System.out.println(keepSet2.size());
//        System.out.println(dataReader.trans(keepSet2));
//        System.out.println();
//
//        Set<Integer> set = new HashSet<>(result);
//        set.retainAll(keepSet2);
//        System.out.println(set.size());
//        System.out.println(dataReader.trans(set));
//    }
}
