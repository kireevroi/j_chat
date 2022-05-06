/*kireevroi 2022*/
/*Simple Hash class*/
package client;

public class Hashing {
    // HashCode generator, currently simple
    public static int hash(String password) {
        int hash = password.hashCode();
        return hash;
    }
}
