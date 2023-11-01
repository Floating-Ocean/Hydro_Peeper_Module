package flocean.module.peeper.fjnuoj.cookie;

import com.alibaba.fastjson2.JSON;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.ModuleFile;
import org.jsoup.Connection;

import java.io.File;
import java.nio.file.Files;

public class SignedInCookie {

    public static CookieRecord cookie;

    static {
        //读取 cookie，需要一个管理员 session cookie
        try {
            File file = ModuleFile.fetchFile(ModuleFile.path + "/config/cookie.json");
            if (file != null) {
                String result = Files.readString(file.toPath());
                cookie = JSON.parseObject(result, CookieRecord.class);
            }
        } catch (Throwable e) {
            throw new RunModuleException(e);
        }
    }

    /**
     * 为连接包装上指定的cookie
     *
     * @param connection 连接
     * @return 附带cookie的连接
     */
    public static Connection wrapWithCookie(Connection connection) {
        return connection
                .cookie("sid", cookie.sid)
                .cookie("sid.sig", cookie.sid_sig);
    }

    public record CookieRecord(String sid, String sid_sig) {
    }
}
