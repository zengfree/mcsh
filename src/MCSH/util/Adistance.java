package MCSH.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Calculate attribute distance
 */
public class Adistance {
    private Map<Integer,double[]> attributemap;
    private Gweight preference_weights;
    private int text_num = 0;
    private int cont_num = 0;

    public Adistance(){
    }

    public double cal_distance(double[] attri1,double[] attri2){
//        double[] attri1 = this.attributemap.get(id1);
//        double[] attri2 = this.attributemap.get(id2);
        double[] attr_text1 = new double[this.text_num],attr_text2= new double[this.text_num],attr_cont1= new double[this.cont_num],attr_cont2= new double[this.cont_num];
//        System.out.println(id1+" "+id2);
        if(attri1!=null && attri2!=null){
            System.arraycopy(attri1,0,attr_text1,0,this.text_num);
            System.arraycopy(attri2,0,attr_text2,0,this.text_num);
            System.arraycopy(attri1,this.text_num,attr_cont1,0,this.cont_num);
            System.arraycopy(attri2,this.text_num,attr_cont2,0,this.cont_num);

//        System.out.println("text" + this.text_num);
            double jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attr_text1,attr_text2,this.preference_weights.getWeights_vector_text());
            double mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attr_cont1,attr_cont2,this.preference_weights.getWeights_vector_continuous());
            double result  = jdist+mdist;
//        System.out.println("jdist"+jdist);
//        System.out.println("mdist"+mdist);
            return result;
        }

        return 0;
    }

    public Adistance(Map<Integer, double[]> attributemap, Gweight preference_weights) {
        this.attributemap = attributemap;
        this.preference_weights = preference_weights;
        this.text_num = (int)this.attributemap.get(-1)[0];
        this.cont_num = (int)this.attributemap.get(-1)[1];
//        this.text_num = 10;
//        this.cont_num = 2;
    }

    public double cal_distance(int id1,int id2){
        double[] attri1 = this.attributemap.get(id1);
        double[] attri2 = this.attributemap.get(id2);
        double[] attr_text1 = new double[this.text_num],attr_text2= new double[this.text_num],attr_cont1= new double[this.cont_num],attr_cont2= new double[this.cont_num];
//        System.out.println(id1+" "+id2);
        if(attri1!=null && attri2!=null){
            System.arraycopy(attri1,0,attr_text1,0,this.text_num);
            System.arraycopy(attri2,0,attr_text2,0,this.text_num);
            System.arraycopy(attri1,this.text_num,attr_cont1,0,this.cont_num);
            System.arraycopy(attri2,this.text_num,attr_cont2,0,this.cont_num);

//        System.out.println("text" + this.text_num);
            double jdist = this.preference_weights.getWeights_vector_main()[0]*Jdistance(attr_text1,attr_text2,this.preference_weights.getWeights_vector_text());
            double mdist = this.preference_weights.getWeights_vector_main()[1]*Mdistance(attr_cont1,attr_cont2,this.preference_weights.getWeights_vector_continuous());
            double result  = jdist+mdist;
//        System.out.println("jdist"+jdist);
//        System.out.println("mdist"+mdist);
        return result;
        }

        return 0;
    }

    private double Mdistance(double[] attribute1,double[] attribute2, double[] weight){
        double result = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Mdist attribute error");
            return result;
        }

        for(int i=0;i<attribute1.length;i++){
            result += weight[i]*Math.abs(attribute1[i]-attribute2[i]);
        }
        return result;
    }

    private double Jdistance(double[] attribute1,double[] attribute2, double[] weight){
        double up=0,down = 0;
        if(attribute1.length!=attribute2.length){
            System.out.println("Jdist attribute error");
            return 0;
        }

        for(int i=0;i<attribute1.length;i++){
            up += weight[i]*attribute1[i]*attribute2[i];
            down += weight[i]*(Math.pow(attribute1[i],2)+Math.pow(attribute2[i],2)-attribute1[i]*attribute2[i]);
        }
        return 1-up/down;
    }

    //计算社区平均距离
    public double cal_subgraph_attribute_distance(Map<Integer, Set<Integer>> pnbmap){
        Set<Integer> keepSet = new HashSet<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            int key = entry.getKey();
            if(entry.getValue().size()>0){
                keepSet.add(key);
            }
        }
        double sum = 0;
        for(Integer i:keepSet){
            for(Integer j:keepSet){
                sum+=cal_distance(i,j);
            }
        }
        return sum/((keepSet.size()-1)* keepSet.size());
    }


    public double cal_subgraph_attr_dist(Set<Integer> set){
//        if(set.isEmpty()) return 1.0;
        Set<Integer> label = new HashSet<>();
        double sum = 0;
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


    public double cal_subgraph_attr_dist(Set<Integer> set,Map<Integer,double[]> distancemap){
        double sum = 0;
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

    public BigDecimal cal_subgraph_attr_dist2(Set<Integer> set,Map<Integer,double[]> distancemap){
        double sum = 0;
        Set<Integer> label = new HashSet<>();
        for(Integer i:set){
            label.add(i);
            for(Integer j:set){
                if(!label.contains(j)){
                    sum+=distancemap.get(i)[j];
                }
            }
        }
        double avg = sum*2/((set.size()-1)* set.size());
        BigDecimal result = new BigDecimal(avg);
        result.setScale(15, RoundingMode.HALF_UP);
        return result;
    }

//    public static void main(String[] args) {
//        Map<Integer,double[]> map = new HashMap<>();
//        double[] d1 = {10,2};
//        double[] d2 = {0,1,0,1,1,0,1,1,0,1,0.25,0.6};
//        double[] d3 = {1,1,1,1,0,1,0,1,1,1,0.25,0.8};
//        map.put(-1,d1);
//        map.put(0,d2);
//        map.put(1,d3);
//        map.put(2,d3);
//        Set<Integer> keepset = new HashSet<>();
//        keepset.add(0);
//        keepset.add(1);
//        keepset.add(2);
//
//        int[] main = {1,1};
//        int[] text = {1,1,1,1,1,1,1,1,1,1};
//        int[] cont = {1,1};
//        Gweight gweight = new Gweight(main,text,cont,2);
////        System.out.println(gweight.toString());
//        Adistance adistance = new Adistance(map,gweight);
//        long time1 = System.nanoTime();
////        System.out.println(adistance.cal_distance(0,1));
//        System.out.println(adistance.cal_subgraph_attr_dist(keepset));
//        long time2 = System.nanoTime();
//        System.out.println(time2-time1);
//    }

}
