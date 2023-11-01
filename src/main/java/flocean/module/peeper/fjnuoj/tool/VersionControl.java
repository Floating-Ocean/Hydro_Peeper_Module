package flocean.module.peeper.fjnuoj.tool;

import java.util.Locale;
import java.util.Properties;

public class VersionControl {

    private static final String moduleName = "FJNUACM OJ Peeper";


    /**
     * 获取版本信息
     *
     * @return 版本信息
     */
    public static String fetchVersionInfo(Class<?> caller) throws Throwable {
        Properties props = new Properties();
        props.load(caller.getResourceAsStream("/buildInfo.properties"));
        String fullName = props.getProperty("fullName").replace(".", "_");
        String moduleVersion = props.getProperty("moduleVersion");
        String buildTime = props.getProperty("buildTime");
        String buildBy = props.getProperty("buildBy");
        return String.format(Locale.ROOT, "[%s]\n\nFull Name: %s\nModule Version: %s\nBuild Time: %s\nBuild By: Gradle %s", moduleName, fullName, moduleVersion, buildTime, buildBy);
    }


    /**
     * 获取一行版本信息
     *
     * @return 一行版本信息
     */
    public static String fetchVersionInfoInline(Class<?> caller) throws Throwable{
        Properties props = new Properties();
        props.load(caller.getResourceAsStream("/buildInfo.properties"));
        String version = props.getProperty("moduleVersion");
        return String.format(Locale.ROOT, "%s v%s", moduleName, version);
    }
}
