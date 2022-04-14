package MCSH.online.exact;

import java.util.Iterator;
import java.util.Set;

public class Tries {
    boolean isEnd;
    Tries[] children;
    int childnum;

    public Tries(int childnum) {
        isEnd = false;
        children = new Tries[childnum];
        this.childnum = childnum;
    }

    public void insert(Set<Integer> set){
        Tries node = this;
        for(Iterator iterator = set.iterator();iterator.hasNext();){
            int id = (int)iterator.next();
//            System.out.println(id);
            if(node.children[id]==null){
                node.children[id]= new Tries(this.childnum);
            }
            node = node.children[id];
        }
        node.isEnd = true;
    }

    /** Returns if the word is in the trie. */
    //查找字符串，若字符串的任意字符所对应的节点为空，那么返回false
    //若字符串最后一个字符所对应的节点并非结尾，返回false
    //其他情况返回true
    //返回结果是否在树中，存在为true，不存在是false
    public boolean search(Set<Integer> set){
        Tries node = this;
        for(Iterator iterator = set.iterator();iterator.hasNext();){
            int id = (int)iterator.next();
            if(node.children[id]==null){
               return false;
            }
            node = node.children[id];
        }
        return node.isEnd;
    }

}
