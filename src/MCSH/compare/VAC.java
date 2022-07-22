package MCSH.compare;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class VAC {
    private int queryId = -1;//查询点集
    private int queryK = -1;//k-truss
    private Map<Integer, Set<Integer>> G;//每个点的对应邻居
    private Map<Integer, Set<Integer>> Att;//每个点对应属性值

    public VAC(int queryId, int queryK, Map<Integer, Set<Integer>> G, Map<Integer, Set<Integer>> Att) {
        this.queryId = queryId;
        this.queryK = queryK;
        this.G = G;
        this.Att = Att;
    }

    private Set<Integer> findKCore(int queryId, int queryK, Map<Integer, Set<Integer>> pnbMap) {
        Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<Integer>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbMap.entrySet()) {
            int curId = entry.getKey();
            Set<Integer> pnbSet = entry.getValue();
            if (pnbSet.size() < queryK) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        //step 2: delete vertices whose degrees are less than k
        while (queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbMap.get(curId);//找到curID对应的邻居
            for (int pnb : pnbSet) {//update curId's pnb
                if (!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbMap.get(pnb);
                    tmpSet.remove(curId);
                    if (tmpSet.size() < queryK) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }
            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
        }

        //step 3: find the connected component containing q
        //找连通图
        if (pnbMap.get(queryId).size() < queryK) return null;
        Set<Integer> community = new HashSet<Integer>();//vertices which have been put into queue
        Queue<Integer> ccQueue = new LinkedList<Integer>();
        ccQueue.add(queryId);
        community.add(queryId);
        while (ccQueue.size() > 0) {
            int curId = ccQueue.poll();
            for (int pnb : pnbMap.get(curId)) {//enumerate curId's neighbors
                if (!community.contains(pnb)) {
                    ccQueue.add(pnb);
                    community.add(pnb);
                }
            }
        }
        return community;
    }

    public Map<Integer, Set<Integer>> K_Core(int queryID, int queryK, Map<Integer, Set<Integer>> G) {//在图g中寻找k_core
        Set<Integer> S = findKCore(queryID, queryK, G);
        if (S == null) return null;
        Map<Integer, Set<Integer>> res_G = new HashMap<Integer, Set<Integer>>();
        for (int x : S) {
            res_G.put(x, new HashSet<Integer>());
            for (int y : G.get(x))
                if (S.contains(y))
                    res_G.get(x).add(y);
        }
        return res_G;
    }

    public Map<Integer, Set<Integer>> stm(Set<Integer> S, Map<Integer, Set<Integer>> G) {//集合s转图,根据图G
        if(G==null||S==null) return null;
        Map<Integer,Set<Integer>> An = new HashMap<Integer,Set<Integer>>();
        for(int x : S){
            An.put(x,new HashSet<Integer>());
            for(int y : G.get(x))
                if(S.contains(y))
                    An.get(x).add(y);
        }
        return An;
    }


    public float Ascore(Map<Integer, Set<Integer>> M) {//计算图map的属性值
        float sum = -1, tmp = 0, s1, s2;
        if(M==null) return 0;
        for(int x:M.keySet()){
            for(int y:M.keySet()){
//                Set<Integer> A = new HashSet<Integer>();//交集
//                Set<Integer> B = new HashSet<Integer>();//并集
                if(x!=y){
//                    copy(B,Att.get(x));
//                    for(int m:Att.get(x)){
//                        if(Att.get(y).contains(m)) A.add(m);
//                        for(int n:Att.get(y)){
//                            if(!Att.get(x).contains(n)) B.add(n);
//                        }
//                    }
                    Set<Integer> A = new HashSet<Integer>(Att.get(x));
                    Set<Integer> B = new HashSet<Integer>(Att.get(x));//并集
                    A.retainAll(Att.get(y));
                    B.addAll(Att.get(y));
                    s1=A.size();
                    s2=B.size();
                    tmp=1-s1/s2;
                    if(tmp>sum) sum=tmp;
                }
            }
        }
        return sum;
    }

    public void showmap(Map<Integer,Set<Integer>> G){
        //System.out.println("map size:"+G.size());
        if(G==null){
            System.out.print("null");
            return;
        }

        /*Iterator entries = G.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry entry = (Map.Entry) entries.next();
            Integer i = (Integer) entry.getKey();
            System.out.print(i+" ");
        }*/
        for(int x : G.keySet()) {
            System.out.print(x+" "+G.get(x).size()+"\t");
        }
        System.out.println();
    }
    public void showset(Set<Integer> S){
        if(S == null){
            System.out.println("null");
            return;
        }
        System.out.println("set size:"+S.size());
        for(int x : S){
            System.out.print(x+" ");
        }
    }

    public void copy(Map<Integer,Set<Integer>>a,Map<Integer,Set<Integer>>b){//map赋值
        //a=new HashMap<Integer,Set<Integer>>();
        a.clear();
        if(b==null) return;
        for(int x:b.keySet()){
            a.put(x,new HashSet<>());
            for(int y:b.get(x))a.get(x).add(y);
        }
    }
    public void copy(Set<Integer>a,Set<Integer>b){// set赋值
        //a=new HashSet<Integer>();
        a.clear();
        if(b==null) return;
        a.addAll(b);
    }

    public Map<Integer, Set<Integer>> query(Map<Integer, Set<Integer>> Mt, int queryId, int queryK) {//只要满足k-core，且ascore（）最小

        if(!Mt.containsKey(queryId))return null;

        //1.把最大的k-core赋值给H
        Set<Integer> H;
        Set<Integer> H1 = new HashSet<Integer>();
        H = findKCore(queryId, queryK, Mt);
        if(H==null) return null;
        copy(H1,H);
        Queue<Set<Integer>> Q = new LinkedBlockingQueue<Set<Integer>>();
        Q.offer(H);
        while(Q.size()>0) {
            H = Q.peek();
            Map<Integer,Set<Integer>> M = new HashMap<Integer,Set<Integer>>();
            copy(M,stm(H,G));
            float sum = -1, tmp = 0, s1, s2;
            int u=-1,v=-1;
            for(int x:M.keySet()){
                for(int y:M.keySet()){
                    if(x!=y){
                        Set<Integer> A = new HashSet<Integer>(Att.get(x));
                        Set<Integer> B = new HashSet<Integer>(Att.get(x));//并集
                        A.retainAll(Att.get(y));
                        B.addAll(Att.get(y));
                        s1=A.size();
                        s2=B.size();
                        tmp=1-s1/s2;
                        if(tmp>sum)
                            {
                                sum = tmp;
                                u=x;
                                v=y;
                            }
                        }
                    }
                }
            Q.poll();
            Delete(u,H,H1,Q,queryId,G);
            Delete(v,H,H1,Q,queryId,G);
            }

        return stm(H1,G);
    }
    public void Delete(int v,Set<Integer> H,Set<Integer> H1,Queue<Set<Integer>> Q,int queryId,Map<Integer,Set<Integer>> G){
        if(v == queryId) return;
//        System.out.println("first");
        Set<Integer> Ht = new HashSet<Integer>();
        H.remove(v);
        copy(Ht,H);
        H.add(v);
        //showset(H);
        //cout(M,C,H,Ht);
        Map<Integer,Set<Integer>> mapHt = new HashMap<Integer,Set<Integer>>();
        copy(mapHt,stm(Ht,G));

        copy(mapHt,K_Core(queryId,queryK,mapHt));
        copy(Ht,mapHt.keySet());
        if(Ht.size()!=0&&Ht.contains(queryId)) {
            if ((Ascore(mapHt) == Ascore(stm(H1, G)) && H1.size() < Ht.size()) || (Ascore(mapHt) < Ascore(stm(H1, G))))
                copy(H1, Ht);
            Set<Integer> set=new HashSet<>();
            boolean flag = true;
            for (Set<Integer> s:Q) {
                if(Ht.size()!=s.size()){
                    set.addAll(Ht);
                    set.retainAll(s);
                    if(set.size()==Ht.size()){
                        flag = false;
                        break;
                    }
                    set.clear();
                }
            }
            if(flag){
                set.clear();
                set.addAll(Ht);
                Q.add(set);
            }
        }
    }
}
