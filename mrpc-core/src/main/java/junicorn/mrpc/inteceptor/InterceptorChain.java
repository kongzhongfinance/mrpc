package junicorn.mrpc.inteceptor;

import java.util.*;

/**
 * 拦截器链
 *
 * Created by biezhi on 2016/12/23.
 */
public class InterceptorChain {

    private List<Entry> interceptors;
    private Set<String> registeredNames;

    public InterceptorChain(){
        interceptors = new ArrayList<>();
        registeredNames = new HashSet<>();
    }

    public void addLast(String name, RpcInteceptor interceptor){
        synchronized(this){
            checkDuplicateName(name);
            Entry entry = new Entry(name,interceptor);
            register(interceptors.size(), entry);
        }
    }
    public void addFirst(String name, RpcInteceptor interceptor){
        synchronized(this){
            checkDuplicateName(name);
            Entry entry = new Entry(name,interceptor);
            register(0, entry);
        }
    }

    public void addBefore(String baseName, String name, RpcInteceptor interceptor){
        synchronized(this){
            checkDuplicateName(name);
            int index = getInterceptorIndex(baseName);
            if(index == -1)
                throw new NoSuchElementException(baseName);
            Entry entry = new Entry(name,interceptor);
            register(index, entry);
        }
    }

    public void addAfter(String baseName, String name, RpcInteceptor interceptor){
        synchronized(this){
            checkDuplicateName(name);
            int index = getInterceptorIndex(baseName);
            if(index == -1)
                throw new NoSuchElementException(baseName);
            Entry entry = new Entry(name,interceptor);
            register(index+1, entry);
        }
    }

    private int getInterceptorIndex(String name) {
        List<Entry> interceptors = this.interceptors;
        for(int i = 0; i < interceptors.size(); i++){
            if(interceptors.get(i).name.equals(name)){
                return i;
            }
        }
        return -1;
    }

    private void register(int index, Entry entry){
        interceptors.add(index, entry);
        registeredNames.add(entry.name);
    }

    private void checkDuplicateName(String name) {
        if (registeredNames.contains(name)) {
            throw new IllegalArgumentException("Duplicate interceptor name: " + name);
        }
    }

    public List<RpcInteceptor> getInterceptors() {
        if(null != interceptors && !interceptors.isEmpty()){
            List<RpcInteceptor> list = new ArrayList<>(this.interceptors.size());
            for(Entry entry : this.interceptors){
                list.add(entry.interceptor);
            }
            return Collections.unmodifiableList(list);
        }else{
            return new ArrayList<>(0);
        }
    }

    static class Entry {
        private String name;
        private RpcInteceptor interceptor;

        public Entry(String name, RpcInteceptor interceptor) {
            this.name = name;
            this.interceptor = interceptor;
        }
    }

}
