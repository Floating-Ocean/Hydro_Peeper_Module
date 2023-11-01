package flocean.module.peeper.fjnuoj.data;

public record RankingData(String user, int ac, int id, int rank) implements RankableRecord {
    @Override
    public String fetchName() {
        return user();
    }

    @Override
    public int fetchCount() {
        return ac();
    }
}
