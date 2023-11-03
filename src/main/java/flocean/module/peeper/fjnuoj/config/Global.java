package flocean.module.peeper.fjnuoj.config;

import com.alibaba.fastjson2.JSON;
import flocean.module.peeper.fjnuoj.Main;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

public class Global {
    private static final String moduleName = "FJNUACM OJ Peeper";

    public static final ConfigRecord config;
    public static final String buildInfo, buildInfoInline;

    static {
        try {
            File file = QuickUtils.fetchFile(QuickUtils.getModulePath() + "/config.json");
            if (file == null || !file.exists()) throw new RunModuleException("Please create a \"config.json\" in working path: " + QuickUtils.getModulePath());

            String result = Files.readString(file.toPath());
            config = JSON.parseObject(result, ConfigRecord.class);
            if (config == null) throw new RunModuleException("Please create a \"config.json\" in working path: " + QuickUtils.getModulePath());

            Properties props = new Properties();
            props.load(Main.class.getResourceAsStream("/buildInfo.properties"));
            String fullName = props.getProperty("fullName").replace(".", "_"),
                    moduleVersion = props.getProperty("moduleVersion"),
                    buildTime = props.getProperty("buildTime"),
                    buildBy = props.getProperty("buildBy");

            buildInfo = String.format(Locale.ROOT, "[%s]\n\nFull Name: %s\nModule Version: %s\nBuild Time: %s\nBuild By: Gradle %s", moduleName, fullName, moduleVersion, buildTime, buildBy);
            buildInfoInline = String.format(Locale.ROOT, "%s v%s", moduleName, moduleVersion);
        } catch (Throwable e) {
            throw new RunModuleException("working path: " + QuickUtils.getModulePath(), e);
        }
    }

}
