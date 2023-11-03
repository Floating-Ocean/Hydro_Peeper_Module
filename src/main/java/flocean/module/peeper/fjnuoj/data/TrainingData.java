package flocean.module.peeper.fjnuoj.data;

public record TrainingData(UserData user, int progress, int generalRank) implements RankableRecord {
    public TrainingData(RankingData rankingData, int progress) {
        this(rankingData.packUser(), progress, rankingData.rank());
    }

    public TrainingData() {
        this(null, 0, 0);
    }

    @Override
    public UserData fetchWho() {
        return new UserData(user().name().split(" \\(")[0], user().id());
    }

    @Override
    public int fetchCount() {
        return progress();
    }
}
