package mil.teng251.codesnippets.proxies.dynamic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxiesDynamicTest {
    @Test
    public void makeProxies() {
        User user = new User("Вася",25);

        InvocationHandler handler = (proxy, method, args) -> {
            if(method.getName().equals("getName")){
                return ((String)method.invoke(user, args)).toUpperCase();
            }
            return method.invoke(user, args);
        };

        Class<?>[] allIfs = User.class.getInterfaces();
        IUser userProxy = (IUser) Proxy.newProxyInstance(user.getClass().getClassLoader(), allIfs, handler);
        Assertions.assertEquals("ВАСЯ", userProxy.getName());
        xlog("ret=["+userProxy.getName()+"]");
    }

    private static void xlog(String msg) {
        System.out.println(msg);
    }
}
