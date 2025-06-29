package mil.teng251.ntfs.streams.inspector.proxies.dynamic;

public class User implements IUser,IAge {
    private final String name;
    private final int age;

    public User() {
        this(null,0);
    }

    public User(String name,int age) {
        this.name = name;
        this.age=age;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }
}