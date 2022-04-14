package MCSH.index.kcore2;

import MCSH.index.hnsw.hnswindex;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;

import java.io.*;
import java.util.*;

public class IndexNode {
    private String indexfile;//构建的索引文件
    private String indexdatafile;//数据文件
    private Set<Integer> nodes;
    private List<IndexNode> childrenlist;
//    private int Coreness ;
    private Map<Integer,Integer> correspond;

    private int M0;
    private int ef;
    private Gweight_float gweight;

    public IndexNode(String indexfile) {
        this.indexfile = indexfile + ".n2";
        this.indexdatafile = indexfile +"data.txt";
    }

    public void loadNode(Map<String,Integer> integerMap){
        this.childrenlist = new ArrayList<>();
        for (int i = 0; i < integerMap.get(this.indexfile); i++) {
            String childindexfile =this.indexfile.replace(".n2","")+"_"+i;
            IndexNode indexNode = new IndexNode(childindexfile);
            indexNode.loadNode(integerMap);
            this.childrenlist.add(indexNode);
        }
    }

    public IndexNode(String indexfile, Gweight_float gweight, int m0, int ef) {
        this.indexfile = indexfile + ".n2";
        this.indexdatafile = indexfile + "data.txt";
        this.gweight = gweight;
        this.M0 = m0;
        this.ef = ef;
        this.childrenlist = new ArrayList<>();
//        this.nodes = new HashSet<>(nodes);
    }

    public void build(Map<Integer,Set<Integer>> pnbMap,Map<Integer,float[]> integerMap,int coreness,Set<Integer> keepSet,Map<Integer, List<Integer>> headMap){
        Map<Integer,Set<Integer>> copyMap =copyMap(pnbMap);
//        List<Set<Integer>> childnodes = new ArrayList<>();

        this.nodes = new HashSet<>(keepSet);
        boolean flag = findDeleteNodes(copyMap,keepSet,coreness);
        this.buildIndex(this.gweight,this.M0,this.ef,integerMap,this.nodes);

        if(flag){
            //划分连通图，给子节点分配候选节点
            List<Set<Integer>> connectedBlocks = findConnectedBlocks(copyMap);
            int i=0;
            for (Set<Integer> set:connectedBlocks){
                for(int nodeid:set){
                    headMap.get(nodeid).add(i);
                }
                String childindexfile =this.indexfile.replace(".n2","")+"_"+i;
                IndexNode indexNode = new IndexNode(childindexfile,this.gweight,this.M0,this.ef);
                indexNode.build(copyMap,integerMap,coreness+1,set,headMap);
                this.childrenlist.add(indexNode);
                i++;
            }
        }

    }

    public IndexNode getChildren(int i) {
        return childrenlist.get(i);
    }

    public Set<Integer> search(float[] queryvec,Gweight_float gweight,float precent){
        readCorrespond();
        int M = (int) Math.ceil(this.correspond.size()*precent);
        int ef = (int) Math.ceil(M*1.25);
        Set<Integer> result = this.searchIndex(queryvec,gweight,M, ef);
        System.out.println("M:"+M+",ef:"+ef);

        return result;
    }

    public Set<Integer> search2(float[] queryvec,Gweight_float gweight,float precent,Map<Integer,float[]> integerMap){
        readCorrespond();
        int M = (int) Math.ceil(this.correspond.size()*precent);
        int ef = (int) Math.ceil(M*1.25);
        Set<Integer> result = this.searchIndex2(queryvec,gweight,M, ef,integerMap);
        System.out.println("M:"+M+",ef:"+ef);

        return result;
    }

    //返回待删节点
    private boolean findDeleteNodes(Map<Integer,Set<Integer>> pnbMap,Set<Integer> keepSet,int coreness){
        Queue<Integer> queue = new LinkedList<>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<>();
        for(int curId:keepSet) {
//            int curId = entry.getKey();
            Set<Integer> pnbSet = pnbMap.get(curId);
            if(pnbSet.size() < coreness) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        Set<Integer> set = new HashSet<>(pnbMap.keySet());
        set.removeAll(keepSet);
        for (int i:set) {
            pnbMap.remove(i);
        }

        //step 2: delete vertices whose degrees are less than k
        while(queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbMap.get(curId);//找到curID对应的邻居
            for(int pnb:pnbSet) {//update curId's pnb
                if(!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbMap.get(pnb);
                    tmpSet.remove(curId);
                    if(tmpSet.size() < coreness) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }
//            pnbMap.put(curId, new HashSet<>());//clean all the pnbs of curId
            pnbMap.remove(curId);
        }

        boolean flag = false;
        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
            if(entry.getValue().size()>=coreness){
                flag = true;
                break;
            }
        }

        return flag;
    }

    public Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap) {
        Map<Integer, Set<Integer>> newMap = new WeakHashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(), newset);
        }
        return newMap;
    }

    //找到图中的连通块
    private List<Set<Integer>> findConnectedBlocks(Map<Integer,Set<Integer>> pnbMap){
        List<Set<Integer>> list = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();//是否访问过，若出现在集合中，则已经找到了连通块
        for(int id:pnbMap.keySet()){
            if(!visited.contains(id)){
                System.out.println("id:"+id);
                Set<Integer> block = findBlock(id,pnbMap);
                if(block.size()>0){
                    visited.addAll(block);
                    list.add(block);
                }
            }
            visited.add(id);
        }
        return list;
    }

    private Set<Integer> findBlock(int nodeid,Map<Integer,Set<Integer>> pnbMap){
        Set<Integer> community = new HashSet<>();
        Queue<Integer> ccQueue = new LinkedList<>();
        ccQueue.add(nodeid);
        community.add(nodeid);
        while(ccQueue.size() > 0) {
            int curId = ccQueue.poll();
            for(int pnb:pnbMap.get(curId)) {//enumerate curId's neighbors
                if(!community.contains(pnb)) {
                    ccQueue.add(pnb);
                    community.add(pnb);
                }
            }
        }
        return community;
    }

    public void setIndexfile(String indexfile) {
        this.indexfile = indexfile;
    }

    //构建hnsw索引
    public void buildIndex (Gweight_float gweight, int M0 ,int ef,Map<Integer,float[]> attribute,Set<Integer> nodes){
        //输出节点属性到datafile中
        this.writeTofile(attribute);

        //构建该节点下的hnsw索引
        hnswindex n2 = new hnswindex();
        n2.build(indexdatafile,indexfile, gweight.getbuildweight(), gweight.getTextnum(),gweight.getContnum(),M0,ef);
        System.out.println("build index:"+this.nodes.size());
    }

    //从hnsw索引中搜索出结果
    public Set<Integer> searchIndex(float[] queryvec,Gweight_float gweight,int M,int ef){
            hnswindex n2 = new hnswindex();
            System.out.println(this.indexfile);
            int[] m =  n2.search(queryvec,M,ef,this.indexfile,gweight.getbuildweight(),gweight.getTextnum(),gweight.getContnum());
            Set<Integer> keepset = new HashSet<>();
//            this.readCorrespond();
            for (int j : m) {
                keepset.add(this.correspond.get(j));
            }
            return keepset;
    }

    //暴力筛选节点
    public Set<Integer> searchIndex2(float[] queryvec,Gweight_float gweight,int M,int ef,Map<Integer,float[]> integerMap){
        System.out.println(this.indexdatafile);
        Adistance_float adistance = new Adistance_float(integerMap,gweight);
        Set<Integer> result = adistance.cal_find_smallest(new HashSet<>(this.correspond.values()) ,queryvec,M);
        return result;
    }

    private void writeTofile(Map<Integer,float[]> attribute){
        try{
            File file = new File(this.indexdatafile);
            if(file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(this.indexdatafile, false);
            System.out.println("write to "+this.indexdatafile);
            for(int nodeid:this.nodes){
                fw.write(nodeid+trans(attribute.get(nodeid))+"\r\n");
            }
            fw.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

    private void readCorrespond(){
        this.correspond = new HashMap<>();
        try{
            BufferedReader stdin = new BufferedReader(new FileReader(this.indexdatafile));
            String line;
            int i = 0;
            while((line = stdin.readLine()) != null){
                String[] s = line.split(" ");
                int attid = Integer.parseInt(s[0]);
                this.correspond.put(i,attid);
                i++;
            }
            stdin.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

    private String trans(float[] floats){
        StringBuilder str5 = new StringBuilder();
        if(floats!=null){
            for (float i : floats) {
                str5.append(" "+i);
            }
        }else {
            return " there is no floats";
        }

        return str5.toString();
    }

    public void savenode(File file){
        try{
            FileWriter fw = new FileWriter(file,true);
            fw.write(this.indexfile+" "+this.childrenlist.size()+"\n");
            fw.close();
        }catch (IOException e){
            System.out.println(e);
        }

        for (IndexNode node:this.childrenlist) {
            node.savenode(file);
        }
    }


}
