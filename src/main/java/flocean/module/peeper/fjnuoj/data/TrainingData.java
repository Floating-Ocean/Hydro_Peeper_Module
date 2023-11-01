package flocean.module.peeper.fjnuoj.data;

public record TrainingData(String user, int progress, int id, int generalRank) implements RankableRecord {
    public TrainingData(RankingData rankingData, int progress) {
        this(rankingData.user(), progress, rankingData.id(), rankingData.rank());
    }

    public TrainingData() {
        this(null, 0, 0, 0);
    }

    @Override
    public String fetchName() {
        return user();
    }

    @Override
    public int fetchCount() {
        return progress();
    }
}
