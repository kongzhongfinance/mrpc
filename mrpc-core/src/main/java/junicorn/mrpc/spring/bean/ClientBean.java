package junicorn.mrpc.spring.bean;

public class ClientBean {

    private String id;
    private String interfaceName;

    public ClientBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public ClientBean(String id, String interfaceName) {
        this.id = id;
        this.interfaceName = interfaceName;
    }
}