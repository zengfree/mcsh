package MCSH.online.basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyLock {
    private Map<Integer, Object> keylock = new ConcurrentHashMap<>();

    /**
     * 尝试对key加一个锁，返回加锁成功或失败
     */
    public boolean lock(Integer key){
        Object my = new Object();
        Object o = keylock.computeIfAbsent(key,l->my);
        return my == o;
    }

    public void unlock(Integer key){
        keylock.remove(key);
    }
}