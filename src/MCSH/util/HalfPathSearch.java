package MCSH.util;

import java.util.*;

public class HalfPathSearch {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private MetaPath queryPath = null;

    public HalfPathSearch(int graph[][],int vertexType[],int edgeType[],MetaPath  querypath){
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.queryPath = querypath;
    }

    public Set<Integer> collect(int q){
        int length = this.queryPath.vertex.length;
        int halflength = (length+1)/2;
        int halfpath[] = new int[halflength];
        System.arraycopy(this.queryPath.vertex,0,halfpath,0,halflength);

        Set<Integer> anchorSet = new HashSet<Integer>();
        anchorSet.add(q);
        for(int layer=1; layer < halflength; layer++){
            int targetType = halfpath[layer];
            Set<Integer> nextAnchorSet = new HashSet<Integer>();

            for(int anchornode:anchorSet){
                int nb[]=graph[anchornode];
                for(int i=0;i< nb.length; i = i+2){
                    int nbVertexID = nb[i];
                    //System.out.println(nbVertexID);
                    if(vertexType[nbVertexID]==targetType){
                        nextAnchorSet.add(nbVertexID);
                    }
                }
            }
            anchorSet = nextAnchorSet;
        }

        return anchorSet;
    }


}