package lund.gradle.plugins;

import org.gradle.api.specs.Spec;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 22.07.14
 * Time: 21:54
 */
public class AcceptAllSpec<Dependency> implements Spec {
    @Override
    public boolean isSatisfiedBy(Object o) {
        return true;
    }
}
