package MCSH.compare;

import java.util.*;

public class ATC {
    Map<Integer,Set<Integer>>G;
    Map<Integer,Set<Integer>>Att;
    Map<Integer,Integer>cnt;
    public ATC(Map<Integer,Set<Integer>>G, Map<Integer,Set<Integer>>Att){
        this.G=G;
        this.Att=Att;
        this.cnt=new HashMap<>();
    }
    //给定查询节点和k值，返回节点集
    private Set<Integer> findKCore(int queryId,int queryK,Map<Integer, Set<Integer>> pnbMap) {
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
            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
        }

        //step 3: find the connected component containing q
        //找连通图
        if(queryId==-1) {
            Set<Integer> community = new HashSet<Integer>();
            for(int x:pnbMap.keySet())if(pnbMap.get(x).size()>=queryK)community.add(x);
            return community;
        }
        if(pnbMap.get(queryId).size() < queryK)   return null;
        Set<Integer> community = new HashSet<Integer>();//vertices which have been put into queue
        Queue<Integer> ccQueue = new LinkedList<Integer>();
        ccQueue.add(queryId);
        community.add(queryId);
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
    public Map<Integer,Set<Integer>> K_Core(int queryID,int queryK,Map<Integer,Set<Integer>> G){
        Set<Integer>S=findKCore(queryID,queryK,G);
        if(S==null)return null;
        Map<Integer,Set<Integer>> res_G=new HashMap<Integer,Set<Integer>>();
        for(int x:S){
            res_G.put(x,new HashSet<Integer>());
            for(int y:G.get(x)){
                if(S.contains(y))
                    res_G.get(x).add(y);
            }
        }
        return res_G;
    }
    //返回子图map
    public Map<Integer,Set<Integer>> KD_Core(int queryID,int queryK,int queryD,Map<Integer,Set<Integer>> G){
        return K_Core(queryID,queryK,G);
    }

    //计算集合H之间的属性得分距离
    /*

     */
    public double attribute_score(Set<Integer>H,Set<Integer>Wq){
        double res=0;
        for(int w:Wq){
            int tmp=0;
            for(int x:H)if(Att.get(x).contains(w))tmp++;
            res+=1.0*tmp*tmp/H.size();
        }
        return res;
    }

    //计算节点v和属性集合Wq的距离
    public int attribute_score(int v,Set<Integer>Wq){
        int res=0;
        for(int w:Att.get(v))
            if(Wq.contains(w))
                res+=cnt.get(w)*2-1;
        return res;
    }

    //计算节点集合H与属性Wq的距离
    public void calc_cnt(Set<Integer>H,Set<Integer>Wq){
        cnt.clear();
        for(int x:H){
            for(int w:Att.get(x)){
                if(Wq.contains(w)){
                    if(cnt.containsKey(w))cnt.put(w,cnt.get(w)+1);
                    else cnt.put(w,1);
                }
            }
        }
    }
    public Map<Integer,Set<Integer>> query(int queryK,int queryD,int queryID,Set<Integer>queryW){
        if(!G.containsKey(queryID))return null;

        Map<Integer,Set<Integer>> now_G=new HashMap<Integer,Set<Integer>>(G);
        now_G=KD_Core(queryID,queryK,queryD,now_G);
        if(now_G==null)return null;
        Set<Integer> S=new HashSet<>(now_G.keySet());
        double res_score=attribute_score(S,queryW);

        for(int it=1;;it++){
            calc_cnt(now_G.keySet(),queryW);
//            int del_cnt=now_G.size()/1000+1;
            int del_cnt=1;
            int del[]=new int[del_cnt+100];
            double min_f[]=new double[del_cnt+100];
            for(int i=1;i<=del_cnt;i++){
                del[i]=-1;
                min_f[i]=1000000000;
            }

            for(int x:now_G.keySet()){
                if(x==queryID)continue;
                int f=attribute_score(x,queryW);
                for(int i=1;i<=del_cnt;i++)
                    if(f<min_f[i]){
                        for(int j=del_cnt;j>i;j--){
                            del[j]=del[j-1];
                            min_f[j]=min_f[j-1];
                        }
                    min_f[i]=f;
                    del[i]=x;
                    break;
                    }
            }
            for(int i=1;i<=del_cnt;i++){
                if(del[i]==-1)break;
                now_G.remove(del[i]);
                for(int x:now_G.keySet())if(now_G.get(x).contains(del[i]))now_G.get(x).remove(del[i]);
            }

            now_G=K_Core(queryID,queryK,now_G);
            if(now_G==null)break;
            double now_score=attribute_score(now_G.keySet(),queryW);
            if(now_score>res_score){
                res_score=now_score;
                S=new HashSet<>(now_G.keySet());
            }
//            System.out.println(now_score);
        }

        Map<Integer,Set<Integer>> res_G=new HashMap<Integer,Set<Integer>>();
        for(int x:S){
            res_G.put(x,new HashSet<Integer>());
            for(int y:G.get(x))
                if(S.contains(y))
                    res_G.get(x).add(y);
        }
        return res_G;
    }
}
