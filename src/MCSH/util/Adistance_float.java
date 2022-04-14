package MCSH.util;

import java.math.BigDecimal;
import java.util.*;


/**
 * Calculate attribute distance
 */
public class Adistance_float {
    private Map<Integer,float[]> attributemap;
    private Gweight_float preference_weights;
    private float[] weights ;
    private int text_num = 0;
    private int cont_num = 0;
    public Gweight_float getPreference_weights() {
        return preference_weights;
    }


    public float cal_distance(float[] attri1,float[] attri2){
//        float[] attr_text1 = new float[this.text_num],attr_text2= new float[this.text_num],attr_cont1= new float[this.cont_num],attr_cont2= new float[this.cont_num];
//        if(attri1!=null && attri2!=null){
//            System.arraycopy(attri1,0,attr_text1,0,this.text_num);
//            System.arraycopy(attri2,0,attr_text2,0,this.text_num);
//            System.arraycopy(attri1,this.text_num,attr_cont1,0,this.cont_num);
//            System.arraycopy(attri2,this.text_num,attr_cont2,0,this.cont_num);
//
//            float jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attr_text1,attr_text2,this.preference_weights.getWeights_vector_text());
//            float mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attr_cont1,attr_cont2,this.preference_weights.getWeights_vector_continuous());
//            return jdist+mdist;
//        }
        if (attri1!=null && attri2!=null){
            float jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attri1,attri2,this.weights,text_num);
            float mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attri1,attri2,this.weights,cont_num);
            return jdist+mdist;
        }

        return 0;
    }

    public Adistance_float(Map<Integer, float[]> attributemap, Gweight_float preference_weights) {
        this.attributemap = attributemap;
        this.preference_weights = preference_weights;
        this.text_num = (int)this.attributemap.get(-1)[0];
        this.cont_num = (int)this.attributemap.get(-1)[1];
        this.weights = preference_weights.getWeights_vector();
//        this.text_num = 10;
//        this.cont_num = 2;
    }

    public Adistance_float() {
    }

    public float cal_distance(int id1,int id2){
        float[] attri1 = this.attributemap.get(id1);
        float[] attri2 = this.attributemap.get(id2);
//        float[] attr_text1 = new float[this.text_num],attr_text2= new float[this.text_num],attr_cont1= new float[this.cont_num],attr_cont2= new float[this.cont_num];
////        System.out.println(id1+" "+id2);
//        if(attri1!=null && attri2!=null){
//            System.arraycopy(attri1,0,attr_text1,0,this.text_num);
//            System.arraycopy(attri2,0,attr_text2,0,this.text_num);
//            System.arraycopy(attri1,this.text_num,attr_cont1,0,this.cont_num);
//            System.arraycopy(attri2,this.text_num,attr_cont2,0,this.cont_num);
//
//        float jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attr_text1,attr_text2,this.preference_weights.getWeights_vector_text());
//        float mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attr_cont1,attr_cont2,this.preference_weights.getWeights_vector_continuous());
//
//        return jdist+mdist;
//        }
        if(attri1!=null && attri2!=null){
            float jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attri1,attri2,this.weights,text_num);
            float mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attri1,attri2,this.weights,cont_num);

            return jdist+mdist;
        }

        return 0;
    }

    private float Mdistance(float[] attribute1,float[] attribute2, float[] weight){
        float result = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Mdist attribute error");
            return result;
        }

        for(int i=0;i<attribute1.length;i++){
            result += weight[i]*Math.abs(attribute1[i]-attribute2[i]);
        }
        return result;
    }

    private float Jdistance(float[] attribute1,float[] attribute2, float[] weight){
        float up=0,down = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Jdist attribute error");
            return 0;
        }

        for(int i=0;i<attribute1.length;i++){
            up += weight[i]*attribute1[i]*attribute2[i];
            down += weight[i]*(Math.pow(attribute1[i],2)+Math.pow(attribute2[i],2)-attribute1[i]*attribute2[i]);
        }
        if(down==0)
            return 0;
        return 1-up/down;
    }


    private float Mdistance(float[] attribute1,float[] attribute2, float[] weight,int num){
        float result = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Mdist attribute error");
            return result;
        }

        for(int i=text_num;i<text_num+num;i++){
            result += weight[i]*Math.abs(attribute1[i]-attribute2[i]);
        }
        return result;
    }

    private float Jdistance(float[] attribute1,float[] attribute2, float[] weight,int num){
        float up=0,down = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Jdist attribute error");
            return 0;
        }

        for(int i=0;i<num;i++){
            up += weight[i]*attribute1[i]*attribute2[i];
            down += weight[i]*(Math.pow(attribute1[i],2)+Math.pow(attribute2[i],2)-attribute1[i]*attribute2[i]);
        }
        if (down==0)
            return 0;

        return 1-up/down;
    }

    //计算社区平均距离
    public float cal_subgraph_attr_dist(Set<Integer> set){
//        if(set.isEmpty()) return 1.0;
        Set<Integer> label = new HashSet<>();
        float sum = 0;
        for(Integer i:set){
            label.add(i);
            for(Integer j:set){
                if(!label.contains(j)){
                    sum+=cal_distance(i,j);
                }
            }
        }
        return sum*2/((set.size()-1)* set.size());
    }

    private float cal_subgraph_attr_dist(Set<Integer> set,Map<Integer,float[]> distancemap){
        float sum = 0;
        Set<Integer> label = new HashSet<>();
        for(Integer i:set){
            label.add(i);
            for(Integer j:set){
                if(!label.contains(j)){
                    sum+=distancemap.get(i)[j];
                }
            }
        }
        return sum*2/((set.size()-1)* set.size());
    }

    public float cal_subgraph_attr_dist_new(Set<Integer> set,Map<Integer,Map<Integer,Float>> distancemap){
//        float sum = 0;
        BigDecimal sum = new BigDecimal(0);
        Set<Integer> label = new HashSet<>();
        for(Integer i:set){
            label.add(i);
            Map<Integer,Float> distmap = distancemap.get(i);
            for(Integer j:set){
                if(!label.contains(j)){
//                    float dist = distmap.get(j);
//                    BigDecimal b1 = BigDecimal.valueOf(distmap.get(j));
                    sum = sum.add(BigDecimal.valueOf(distmap.get(j)));
//                    sum += distmap.get(j);
                }
            }
        }

        return sum.floatValue()*2/((set.size()-1)* set.size());
    }

    public float cal_subgraph_attr_dist_new2(Set<Integer> set,Map<Integer,Map<Integer,Float>> distancemap,float[] distance){
        BigDecimal sum = new BigDecimal(0);
        Set<Integer> label = new HashSet<>();
        for(Integer i:set){
            label.add(i);
            Map<Integer,Float> distmap = distancemap.get(i);
            for(Integer j:set){
                if(!label.contains(j)){
//                    System.out.println(i+" :"+j);
//                    float dist = distmap.get(j);
//                    BigDecimal b1 = new BigDecimal(dist);
                    sum = sum.add(BigDecimal.valueOf(distmap.get(j)));
//                    sum+=dist;
                }
            }
        }
        distance[1] = sum.floatValue();
        distance[2] = sum.floatValue();
        return distance[1]*2/((set.size()-1)* set.size());
    }

    public float cal_graphdist(Set<Integer> set,Set<Integer> last,Map<Integer,Map<Integer,Float>> distancemap,float[] distance){
//        float sum = distance[1];
//        double sum = distance[1];
        BigDecimal sum = new BigDecimal(Float.toString(distance[1]));
        Set<Integer> set1 = new HashSet<>(last);
        set1.removeAll(set);
        Set<Integer> label = new HashSet<>();
        for(Integer i:set1){
            label.add(i);
            Map<Integer,Float> distmap = distancemap.get(i);
            for(Integer j:set1){
                if(!label.contains(j)){
//                    BigDecimal b1 = BigDecimal.valueOf(distmap.get(j));
                    sum = sum.subtract(BigDecimal.valueOf(distmap.get(j)));
//                    sum -= distmap.get(j);
                }
            }
        }

        for(int i:set1){
            Map<Integer,Float> distmap = distancemap.get(i);
            for (int j:set){
//                BigDecimal b1 = BigDecimal.valueOf(distmap.get(j));
                sum = sum.subtract(BigDecimal.valueOf(distmap.get(j)));
//                System.out.println("sum :"+sum+" - "+distmap.get(j));
//                sum -= distmap.get(j);
            }
        }

        distance[1] = sum.floatValue();
        return distance[1]*2/((set.size()-1)* set.size());
    }

    public float cal_subgraph_attr_dist_compare(Set<Integer> set,Map<Integer,Map<Integer,Float>> distancemap){
        float sum = 0;
        for(Integer i:set){
            for(Integer j:set){
//                Map<Integer,Float> dist = distancemap.get(i);
                if(i>j){
                    sum+=distancemap.get(i).get(j);
//                    sum+=dist.get(j);
                }
            }
        }

        return sum*2/((set.size()-1)* set.size());
    }

    public float cal_graphavgdist(Set<Integer> set,Map<Integer,Map<Integer,Float>> distancemap){
        long t1 = System.nanoTime();
        List<Integer> sortedlist = new ArrayList<>(set);
        sortedlist.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        long t2 = System.nanoTime();
        System.out.println("sort :"+(t2-t1));

        float sum = 0;
        int cont = 0;
        int num = sortedlist.size();
        for(int i=0;i<num;i++){
            int i_id = sortedlist.get(i);
            Map<Integer,Float> distmap = distancemap.get(i_id);
            for(int j=i+1;j<num;j++){
                int j_id = sortedlist.get(j);
//                sum+=distancemap.get(i_id).get(j_id);
                cont++;
                sum+= distmap.get(j_id);
            }
        }
        System.out.println("cont:"+cont);

        return sum*2/((set.size()-1)* set.size());
    }


    public Set<Integer> cal_find_smallest(Set<Integer> set,float[] queryvec,int M){
        Map<Integer, Float> sumdistance = new HashMap<>();
        for(int i:set){
            sumdistance.put(i,cal_distance(queryvec,this.attributemap.get(i)));
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());//升序
//                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> closeNodes = new HashSet<>();
        for (int i = 0; i < M; i++) {
            closeNodes.add(entryList.get(i).getKey());
        }
        return closeNodes;
    }

    public float[] avgdist(Set<Integer> set){
        float[] avg = new float[text_num+cont_num];
        for(int id:set){
            for(int i =0;i<avg.length;i++){
                avg[i] += this.attributemap.get(id)[i];
            }
        }
        for(int i =0;i<avg.length;i++){
            avg[i] = avg[i]/set.size();
        }
        return avg;
    }

    public Map<Integer,Float> cal_attribute_contribute(Set<Integer> set){
        Map<Integer,Float> map = new HashMap<>();
        float[] avg = avgdist(set);
        for(int id:set){
            map.put(id,cal_distance(attributemap.get(id),avg));
        }
        return map;
    }

//    public static void main(String[] args) {
//        Map<Integer,Map<Integer,Float>> map = new HashMap<>();
//        Map<Integer,Float> floatMap = new HashMap<>();
//        floatMap.put(1,(float)1.0);
//        floatMap.put(2,(float)0.8);
//        floatMap.put(3,(float)1.0);
//        map.put(0,floatMap);
//
//        Map<Integer,Float> floatMap2 = new HashMap<>();
//        floatMap2.put(0,(float)1.0);
//        floatMap2.put(2,(float)0.5);
//        floatMap2.put(3,(float)1.0);
//        map.put(1,floatMap2);
//
//        Map<Integer,Float> floatMap3= new HashMap<>();
//        floatMap3.put(1,(float)0.8);
//        floatMap3.put(0,(float)0.5);
//        floatMap3.put(3,(float)0.7);
//        map.put(2,floatMap3);
//
//        Map<Integer,Float> floatMap4 = new HashMap<>();
//        floatMap4.put(0,(float)1.0);
//        floatMap4.put(1,(float)1.0);
//        floatMap4.put(2,(float)0.7);
//        map.put(3,floatMap4);
//        Set<Integer> set = new HashSet<>();
//        set.add(0);
//        set.add(1);
//        set.add(2);
//        set.add(3);
//        Adistance_float adistance_float = new Adistance_float();
//        float[] dis = new float[2];
//        float dist = adistance_float.cal_subgraph_attr_dist_new2(set,map,dis);
//        System.out.println(dist);
//        Set<Integer> set2 = new HashSet<>();
//        set2.add(0);
//        set2.add(1);
//        float dist2 = adistance_float.cal_subgraph_attr_dist_new(set2,map);
//
//        float dist3 = adistance_float.cal_graphdist(set2,set,map,dis);
//        System.out.println("dist2:"+dist2);
//        System.out.println("dist3:"+dist3);
//
//    }

}
