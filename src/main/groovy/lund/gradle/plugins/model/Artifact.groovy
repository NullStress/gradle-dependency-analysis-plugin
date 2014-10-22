package lund.gradle.plugins.model

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 19.10.14
 * Time: 16:14
 */
class Artifact {

    private File absoluteFile;
    private String name;
    private Set<String> containedClasses;
    private boolean isUsed;

    Artifact(File absoluteFile, String name) {
        this.absoluteFile = absoluteFile
        this.name = name
        isUsed = false
    }

    File getAbsoluteFile() {
        return absoluteFile
    }

    String getName() {
        return name
    }

    Set<String> getContainedClasses() {
        return containedClasses
    }

    void setName(String name) {
        this.name = name
    }

    void setContainedClasses(Set<String> containedClasses) {
        this.containedClasses = containedClasses
    }

    boolean getIsUsed() {
        return isUsed
    }

    void setIsUsed(boolean used) {
        isUsed = used
    }
}
