package flocean.module.peeper.fjnuoj.enums;

public enum VerdictType {
    ACCEPTED("Accepted", "AC"),
    WRONG_ANSWER("Wrong Answer", "WA"),
    COMPILE_ERROR("Compile Error", "CE"),
    TIME_LIMIT_EXCEED("Time Exceeded", "TLE"),
    MEMORY_LIMIT_EXCEED("Memory Exceeded", "MLE"),
    RUNTIME_ERROR("Runtime Error", "RE")
    ;

    private final String name;
    private final String alias;

    VerdictType(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public static VerdictType searchVerdict(String keyWords) {
        for (VerdictType verdictType : values()) {
            if (verdictType.alias.equalsIgnoreCase(keyWords)) return verdictType;
            if (verdictType.name.equalsIgnoreCase(keyWords)) return verdictType;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

}
