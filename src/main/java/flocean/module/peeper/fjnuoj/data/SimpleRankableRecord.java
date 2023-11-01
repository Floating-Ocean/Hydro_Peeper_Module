package flocean.module.peeper.fjnuoj.data;

public record SimpleRankableRecord(String user, int count) implements RankableRecord {
    @Override
    public String fetchName() {
        return user().split(" \\(")[0];
    }

    @Override
    public int fetchCount() {
        return count();
    }
}
